package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.Starboard;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles starboard command backend.
 *
 * @author TechnoVision
 */
public class StarboardHandler extends ListenerAdapter {

    private final TechnoBot bot;
    private final Guild guild;
    private Starboard starboard;
    private final Bson filter;

    /**
     * Sets up POJO objects in database.
     *
     * @param bot instance of TechnoBot.
     * @param guild ID of the guild this data is for.
     */
    public StarboardHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;

        // Get POJO objects from database
        filter = Filters.eq("guild", guild.getIdLong());
        this.starboard = bot.database.starboard.find(filter).first();
        if (starboard == null) {
            starboard = new Starboard(guild.getIdLong());
            bot.database.starboard.insertOne(starboard);
        }
    }

    /**
     * Sets the minimum required stars to get on the starboard in local cache and database.
     *
     * @param limit the new star limit.
     */
    public void setStarLimit(int limit) {
        starboard.setStarLimit(limit);
        bot.database.starboard.updateOne(filter, Updates.set("star_limit", limit));
    }

    /**
     * Sets the starboard channel and clears all previous posts from cache and database.
     *
     * @param channelID the ID of the channel to set as starboard.
     */
    public void setChannel(long channelID) {
        starboard.setChannel(channelID);
        starboard.getPosts().clear();
        Bson update = Filters.and(Updates.set("channel", channelID), Updates.set("posts", new HashMap<>()));
        bot.database.starboard.updateOne(filter, update);
    }

    /**
     * Adds a channel ID to the blacklist, disabling it for starring.
     *
     * @param channelID the ID of the channel to blacklist
     */
    public void blacklistChannel(long channelID) {
        starboard.getBlacklist().add(channelID);
        bot.database.starboard.updateOne(filter, Updates.push("blacklist", channelID));
    }

    /**
     * Removes a channel ID from the blacklist, enabling it for starring.
     *
     * @param channelID the ID of the channel to blacklist
     */
    public void unBlacklistChannel(long channelID) {
        starboard.getBlacklist().remove(channelID);
        bot.database.starboard.updateOne(filter, Updates.pull("blacklist", channelID));
    }

    /**
     * Clears the entire blacklist in cache and database back to default.
     */
    public void clearBlacklist() {
        starboard.getBlacklist().clear();
        bot.database.starboard.updateOne(filter, Updates.set("blacklist", new ArrayList<>()));
    }

    /**
     * Checks if starboard is ready to receive a post from a specified channel.
     *
     * @param channel the channel the post was starred in.
     * @return true if valid, otherwise false.
     */
    public boolean isValid(TextChannel channel) {
        if (starboard == null) return false;
        else if (starboard.getChannel() == null) return false;
        else if (starboard.isLocked()) return false;
        else if (starboard.getChannel() == channel.getIdLong()) return false;
        else if (channel.isNSFW() && !starboard.isNSFW()) return false;
        else return !starboard.blacklist.contains(channel.getIdLong());
    }

    /**
     * Gets the ID of the starboard post referring to a specified message.
     *
     * @param messageID the ID this post refers to.
     * @return the String ID of the starboard post.
     */
    public String getPost(String messageID) {
        return starboard.getPosts().get(messageID);
    }

    /**
     * Checks if starboard contains post for a specified message.
     *
     * @param messageID the ID this post refers to.
     * @return true if starboard contains post, otherwise false.
     */
    public boolean containsPost(String messageID) {
        return starboard.getPosts().containsKey(messageID);
    }

    /**
     * Adds a new post to the starboard local cache and MongoDB.
     *
     * @param messageID the ID this post refers to.
     * @param postID the ID of the post itself.
     */
    public void addPost(String messageID, String postID) {
        starboard.getPosts().put(messageID, postID);
        bot.database.starboard.updateOne(filter, Updates.set("posts."+messageID, postID));
    }

    /**
     * Removes a new post from the starboard local cache and MongoDB.
     *
     * @param messageID the ID this post refers to.
     */
    public String removePost(String messageID) {
        String id = starboard.getPosts().remove(messageID);
        if (id != null) bot.database.starboard.updateOne(filter, Updates.unset("posts."+messageID));
        return id;
    }

    /**
     * Flips the value for 'isLocked' in local cache and database.
     */
    public boolean toggleLock() {
        boolean isLocked = !starboard.isLocked();
        starboard.setLocked(isLocked);
        bot.database.starboard.updateOne(filter, Updates.set("locked", isLocked));
        return isLocked;
    }

    /**
     * Flips the value for 'hasJumpLink' in local cache and database.
     */
    public boolean toggleJump() {
        boolean hasJumpLink = !starboard.isJumpLink();
        starboard.setJumpLink(hasJumpLink);
        bot.database.starboard.updateOne(filter, Updates.set("jump_link", hasJumpLink));
        return hasJumpLink;
    }

    /**
     * Flips the value for 'canSelfStar' in local cache and database.
     */
    public boolean toggleSelfStar() {
        boolean canSelfStar = !starboard.isSelfStar();
        starboard.setSelfStar(canSelfStar);
        bot.database.starboard.updateOne(filter, Updates.set("self_star", canSelfStar));
        return canSelfStar;
    }

    /**
     * Flips the value for 'isNSFW' in local cache and database.
     */
    public boolean toggleNSFW() {
        boolean isNSFW = !starboard.isNSFW();
        starboard.setNSFW(isNSFW);
        bot.database.starboard.updateOne(filter, Updates.set("nsfw", isNSFW));
        return isNSFW;
    }

    /** Getter and setter methods */

    public Long getChannel() { return starboard.getChannel(); }

    public int getStarLimit() { return starboard.getStarLimit(); }

    public boolean isLocked() { return starboard.isLocked(); }

    public boolean hasJumpLink() { return starboard.isJumpLink(); }

    public boolean canSelfStar() { return starboard.isSelfStar(); }

    public boolean isNSFW() { return starboard.isNSFW(); }
}
