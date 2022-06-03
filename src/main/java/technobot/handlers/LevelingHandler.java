package technobot.handlers;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Leveling;

/**
 * Handles leveling and leaderboard.
 *
 * @author TechnoVision
 */
public class LevelingHandler {

    private final Guild guild;
    private final TechnoBot bot;

    public LevelingHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
    }

    /**
     * Calculates the XP needed to reach next level goal.
     * Uses an algorithm that dynamically scales in difficulty.
     *
     * @param level The member's current level
     * @return The total xp needed to reap the next level.
     */
    public static int calculateLevelGoal(int level) {
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
     * Calculates user rank in guild leaderboard.
     *
     * @param userID ID of the user.
     * @return integer rank for user in this guild.
     */
    public int getRank(long userID) {
        return 1;
        /**
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getLong("id") == userID) {
                return i + 1;
            }
        }
        return leaderboard.size();
         */
    }
}
