package technobot.handlers;

import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.moderation.Ban;
import technobot.data.cache.moderation.Moderation;
import technobot.data.cache.moderation.Warning;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles moderation and warnings. Interfaces with POJO objects.
 *
 * @author TechnoVision
 */
public class ModerationHandler {

    private Moderation moderation;
    private final ScheduledExecutorService unbanScheduler;

    private final Guild guild;
    private final TechnoBot bot;

    /**
     * Sets up POJO objects in database.
     *
     * @param bot instance of TechnoBot.
     * @param guild ID of the guild this data is for.
     */
    public ModerationHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
        this.unbanScheduler = Executors.newScheduledThreadPool(10);

        // Get POJO objects from database
        Bson filter = Filters.eq("guild", guild.getIdLong());
        moderation = bot.database.moderation.find(filter).first();
        if (moderation == null) {
            moderation = new Moderation(guild.getIdLong());
            bot.database.moderation.insertOne(moderation);
        }

        // Schedule and manage unbans
        for (Ban ban : moderation.getBans().values()) {
            bot.shardManager.retrieveUserById(ban.getUser()).queue(target -> {
                if (ban.getTimestamp() + TimeUnit.DAYS.toMillis(ban.getDays()) <= System.currentTimeMillis()) {
                    removeBan(target);
                } else {
                    unbanScheduler.schedule(() -> removeBan(target), ban.getDays(), TimeUnit.DAYS);
                }
            });
        }
    }

    /**
     * Schedules a user to be unbanned after a set number of days.
     * Also adds this information to the database.
     *
     * @param guild the guild this user was banned from.
     * @param target the user to unban.
     * @param days the days to wait before unbanning.
     */
    public void scheduleUnban(Guild guild, User target, int days) {
        // Add banned user and timestamp to database
        Ban ban = new Ban(target.getIdLong(), System.currentTimeMillis(), days);
        moderation.addBan(target.getId(), ban);
        Bson filter = Filters.eq("guild", guild.getIdLong());
        Bson addBan = Filters.eq("$set", Filters.eq("bans."+target.getId(), ban));
        bot.database.moderation.updateOne(filter, addBan);

        // Start scheduled task
        unbanScheduler.schedule(() -> removeBan(target), days, TimeUnit.DAYS);
    }

    /**
     * Check if a user currently has a timed ban.
     *
     * @param userID the string ID of the user to check.
     * @return true if user has timed ban, otherwise false.
     */
    public boolean hasTimedBan(String userID) {
        return moderation.getBans().containsKey(userID);
    }

    /**
     * Lifts a ban from local cache, server, and database.
     *
     * @param target the user to unban.
     */
    public void removeBan(User target) {
        // Unban user
        String userID = target.getId();
        guild.unban(target).queue();
        moderation.removeBan(userID);

        // Remove ban from database
        Bson filter = Filters.eq("guild", guild.getIdLong());
        Bson removeBan = Filters.eq("$unset", Filters.eq("bans."+userID, ""));
        bot.database.moderation.updateOne(filter, removeBan);
    }

    /**
     * Adds a warning to the local cache and database.
     *
     * @param reason the reason for this warning.
     * @param target the ID of the user receiving this warning.
     * @param staff the ID of the staff giving this warning.
     */
    public void addWarning(String reason, long target, long staff) {
        // Update local cache
        int total = moderation.getTotal() + 1;
        int count = moderation.getCount() + 1;
        Warning warning = new Warning(total, System.currentTimeMillis(), reason, target, staff);
        moderation.setTotal(total);
        moderation.setCount(count);
        moderation.addWarning(target, warning);

        // Update MongoDB database
        Bson filter = Filters.eq("guild", guild);
        Bson update = Filters.eq("$push", Filters.eq("warnings."+target, warning));
        Bson update2 = Filters.eq("$inc", Filters.eq("count", 1));
        Bson update3 = Filters.eq("$inc", Filters.eq("total", 1));
        bot.database.moderation.updateOne(filter, Filters.and(update, update2, update3));
    }

    /**
     * Retrieves the list of warnings for a specified user.
     *
     * @param target the user to get warning for.
     * @return a list of warning objects for this user.
     */
    public List<Warning> getWarnings(String target) {
        return moderation.getWarnings().get(target);
    }

    /**
     * Clear all warnings for a specified user.
     *
     * @param target the ID of the user to target.
     * @return the number of warnings cleared.
     */
    public int clearWarnings(long target) {
        int count = moderation.clearWarnings(target);
        if (count > 0) {
            Bson filter = Filters.eq("guild", guild);
            Bson update = Filters.eq("$unset", Filters.eq("warnings."+target, ""));
            Bson update2 = Filters.eq("$set", Filters.eq("count", moderation.getCount()));
            bot.database.moderation.updateOne(filter, Filters.and(update, update2));
        }
        return count;
    }

    /**
     * Removes a warning with a specific ID.
     *
     * @param id the ID of the warning to target.
     * @return the number of warnings cleared.
     */
    public int removeWarning(int id) {
        return moderation.removeWarning(id);
    }
}
