package technobot.handlers;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Config;
import technobot.data.cache.Leveling;

import java.util.LinkedList;

/**
 * Handles leveling and leaderboard.
 *
 * @author TechnoVision
 */
public class LevelingHandler {

    private final Guild guild;
    private final TechnoBot bot;
    private final LinkedList<Leveling> leaderboard;

    public LevelingHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
        this.leaderboard = new LinkedList<>();

        // Populate leaderboard from database
        Bson filter = Filters.eq("guild", guild.getIdLong());
        Bson sort = Filters.eq("total_xp", -1);
        for (Leveling profile : bot.database.leveling.find(filter).sort(sort)) {
            leaderboard.add(profile);
        }
    }

    /**
     * Calculates the XP needed to reach next level goal.
     * Uses an algorithm that dynamically scales in difficulty.
     *
     * @param level The member's current level
     * @return The total xp needed to reap the next level.
     */
    public int calculateLevelGoal(int level) {
        return (int) (5 * Math.pow(level, 2) + 50 * level + 100);
    }

    /**
     * Retrieves a leveling profile from MongoDB.
     *
     * @param userID ID of the user.
     * @return MongoDB data document for that member.
     */
    public Leveling getProfile(long userID) {
        Bson filter = Filters.and(Filters.eq("guild", guild.getIdLong()), Filters.eq("user", userID));
        return bot.database.leveling.find(filter).first();
    }

    /**
     * Deletes a profile and removes it from leaderboard.
     *
     * @param userID ID of the user to reset.
     */
    public void resetProfile(long userID) {
        Bson query = Filters.and(Filters.eq("guild", guild.getIdLong()), Filters.eq("user", userID));
        int index = getIndex(userID);
        if (index >= 0) {
            leaderboard.remove(index);
            bot.database.leveling.deleteOne(query);
        }
    }

    /**
     * Deletes all leveling data and config settings.
     */
    public void resetAll() {
        leaderboard.clear();
        Bson query = Filters.and(Filters.eq("guild", guild.getIdLong()));
        bot.database.leveling.deleteMany(query);
        Config config = new Config(guild.getIdLong());
        bot.database.config.replaceOne(query, config);
        GuildData.get(guild).config = config;
    }

    /**
     * Calculates user rank in guild leaderboard.
     *
     * @param userID ID of the user.
     * @return integer rank for user in this guild.
     */
    public int getRank(long userID) {
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getUser() == userID) {
                return i + 1;
            }
        }
        return leaderboard.size();
    }

    /**
     * Calculates the exact index of this user in the leaderboard.
     *
     * @param userID ID of the user.
     * @return index of user in leaderboard, or -1 if not present.
     */
    private int getIndex(long userID) {
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getUser() == userID) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Updates the leaderboard based on the XP increase of a single profile.
     *
     * @param profile MongoDB data document for a particular user.
     */
    public void updateLeaderboard(Leveling profile) {
        int originalIndex = getIndex(profile.getUser());
        int index = originalIndex;
        if (index <= 0) {
            leaderboard.remove(originalIndex);
            leaderboard.add(index, profile);
            return;
        }

        Leveling ahead = leaderboard.get(index - 1);
        long aheadTotalXP = ahead.getTotalXP();
        long totalXP = profile.getTotalXP();

        while (totalXP > aheadTotalXP) {
            index--;
            if (index <= 0) {
                break;
            }
            ahead = leaderboard.get(index - 1);
            aheadTotalXP = ahead.getTotalXP();
        }
        leaderboard.remove(originalIndex);
        leaderboard.add(index, profile);
    }

    /**
     * Adds a leveling profile to the leaderboard.
     *
     * @param profile the profile to add.
     */
    public void addProfile(Leveling profile) {
        leaderboard.add(profile);
    }

    /**
     * Simple getter for leaderboard.
     *
     * @return the leaderboard.
     */
    public LinkedList<Leveling> getLeaderboard() { return leaderboard; }
}
