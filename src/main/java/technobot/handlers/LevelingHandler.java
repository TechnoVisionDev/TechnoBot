package technobot.handlers;

import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Config;
import technobot.data.cache.Leveling;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Handles leveling and leaderboard.
 *
 * @author TechnoVision
 */
public class LevelingHandler {

    private final Guild guild;
    private final TechnoBot bot;
    private final Bson guildFilter;

    public LevelingHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
        this.guildFilter = Filters.eq("guild", guild.getIdLong());
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
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        return bot.database.leveling.find(filter).first();
    }

    /**
     * Deletes a profile and removes it from leaderboard.
     *
     * @param userID ID of the user to reset.
     */
    public void resetProfile(long userID) {
        Bson query = Filters.and(guildFilter, Filters.eq("user", userID));
        bot.database.leveling.deleteOne(query);
    }

    /**
     * Deletes all leveling data and config settings.
     */
    public void resetAll() {
        bot.database.leveling.deleteMany(guildFilter);
        Config config = bot.database.config.find(guildFilter).first();
        if (config != null) {
            config.setLevelingDM(false);
            config.setLevelingMute(false);
            config.setLevelingMod(1);
            config.setLevelingBackground(null);
            config.setLevelingMessage(null);
            config.setLevelingChannel(null);
            config.getRewards().clear();
            bot.database.config.replaceOne(guildFilter, config);
            GuildData.get(guild).config = config;
        }
    }

    /**
     * Calculates user rank in guild leaderboard.
     *
     * @param userID ID of the user.
     * @return integer rank for user in this guild.
     */
    public int getRank(long userID) {
        int rank = 1;
        for (Leveling profile : getLeaderboard()) {
            if (profile.getUser() == userID) return rank;
            rank++;
        }
        return guild.getMemberCount();
    }

    /**
     * Get a sorted list of user leveling data sorted by networth.
     *
     * @return iterable of user leveling data sorted in descending order.
     */
    public FindIterable<Leveling> getLeaderboard() {
        return bot.database.leveling.find(guildFilter).sort(Sorts.descending("total_xp"));
    }
}
