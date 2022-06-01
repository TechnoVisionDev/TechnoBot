package technobot.handlers;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.Moderation;
import technobot.data.cache.Warning;

import javax.print.Doc;
import java.util.List;

/**
 * Handles moderation and warnings. Interfaces with POJO objects.
 *
 * @author TechnoVision
 */
public class ModerationHandler {

    private Moderation moderation;

    private final long guild;
    private final TechnoBot bot;

    /**
     * Sets up POJO objects in database.
     *
     * @param bot instance of TechnoBot.
     * @param guild ID of the guild this data is for.
     */
    public ModerationHandler(TechnoBot bot, long guild) {
        this.bot = bot;
        this.guild = guild;

        Bson filter = Filters.eq("guild", guild);
        moderation = bot.database.moderation.find(filter).first();
        if (moderation == null) {
            moderation = new Moderation(guild);
            bot.database.moderation.insertOne(moderation);
        }
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
