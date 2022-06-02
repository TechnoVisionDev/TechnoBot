package technobot.data.cache;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.*;

/**
 * POJO object that stores data for a guild's Starboard.
 *
 * @author TechnoVision
 */
public class Starboard {

    /** The guild ID. */
    private long guild;

    /** The starboard text channel ID. */
    private Long channel;

    /** A map of message IDs to corresponding starboard post IDs. */
    private Map<String, String> posts;

    /** Set of text channel IDs that ignore star reactions. */
    public Set<Long> blacklist;

    @BsonProperty("star_limit")
    private int starLimit;

    @BsonProperty("locked")
    private boolean isLocked;

    @BsonProperty("jump_link")
    private boolean jumpLink;

    @BsonProperty("self_star")
    private boolean selfStar;

    @BsonProperty("nsfw")
    private boolean isNSFW;

    /**
     * Required for POJO
     */
    public Starboard() { }

    /**
     * Creates a brand-new Starboard without existing
     * data and with a default star limit of 3.
     *
     * @param guild The long ID of the guild.
     */
    public Starboard(long guild) {
        this.guild = guild;
        this.channel = null;
        this.posts = new HashMap<>();
        this.blacklist = new HashSet<>();
        this.starLimit = 3;
        this.isLocked = false;
        this.jumpLink = true;
        this.selfStar = false;
        this.isNSFW = false;
    }

    /** Getter and Setter methods */

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public Long getChannel() {
        return channel;
    }

    public void setChannel(Long channel) {
        this.channel = channel;
    }

    public Map<String, String> getPosts() {
        return posts;
    }

    public void setPosts(Map<String, String> posts) {
        this.posts = posts;
    }

    public Set<Long> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(Set<Long> blacklist) {
        this.blacklist = blacklist;
    }

    public int getStarLimit() {
        return starLimit;
    }

    public void setStarLimit(int starLimit) {
        this.starLimit = starLimit;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isJumpLink() {
        return jumpLink;
    }

    public void setJumpLink(boolean jumpLink) {
        this.jumpLink = jumpLink;
    }

    public boolean isSelfStar() {
        return selfStar;
    }

    public void setSelfStar(boolean selfStar) {
        this.selfStar = selfStar;
    }

    public boolean isNSFW() {
        return isNSFW;
    }

    public void setNSFW(boolean NSFW) {
        isNSFW = NSFW;
    }
}
