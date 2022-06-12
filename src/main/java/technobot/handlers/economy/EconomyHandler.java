package technobot.handlers.economy;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.lang.Nullable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the server economy backend.
 *
 * @author TechnoVision
 */
public class EconomyHandler {

    public static final long DEFAULT_TIMEOUT = 14400000;
    public static final String DEFAULT_CURRENCY = "\uD83E\uDE99";
    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);
    private static final EconomyLocalization responses = new EconomyLocalization();

    private final Guild guild;
    private final TechnoBot bot;
    private final Bson guildFilter;
    private final Map<Long, UserTimeout> timeouts;

    public EconomyHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
        this.guildFilter = Filters.eq("guild", guild.getIdLong());
        this.timeouts = new HashMap<>();
    }

    /**
     * Adds a random amount between 20-250 to the user's balance and get a reply back.
     *
     * @param userID the ID of user whose balance to add to.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply work(long userID) {
        int amount = ThreadLocalRandom.current().nextInt(230) + 20;
        addMoney(userID, amount);
        setTimeout(userID, TIMEOUT_TYPE.WORK);
        return responses.getWorkResponse(amount);
    }

    /**
     * Add money to this user's account
     *
     * @param amount the amount of money to add.
     */
    private void addMoney(long userID, long amount) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        bot.database.economy.updateOne(filter, Updates.inc("balance", amount), UPSERT);
    }

    /**
     * Remove money to this user's account
     *
     * @param amount the amount of money to remove.
     */
    private void removeMoney(long userID, long amount) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        bot.database.economy.updateOne(filter, Updates.inc("balance", -1 * amount), UPSERT);
    }

    /**
     * Set a user timeout for a specific economy command.
     *
     * @param userID the user to set timeout for.
     * @param type the economy command to timeout.
     */
    private void setTimeout(long userID, TIMEOUT_TYPE type) {
        long time = System.currentTimeMillis() + DEFAULT_TIMEOUT;
        UserTimeout userTimeout = timeouts.get(userID);
        if (userTimeout == null) {
            userTimeout = new UserTimeout(userID);
        }
        switch(type) {
            case WORK -> userTimeout.setWorkTimeout(time);
            case SLUT -> userTimeout.setSlutTimeout(time);
            case CRIME -> userTimeout.setCrimeTimeout(time);
            case ROB -> userTimeout.setRobTimeout(time);
        }
        timeouts.put(userID, userTimeout);
    }

    /**
     * Get a user's timeout for a specific econony command.
     *
     * @param userID the user to get the timeout from.
     * @param type the economy command that is timed out.
     * @return time in millis till timeout is up. Null if not set.
     */
    public @Nullable Long getTimeout(long userID, TIMEOUT_TYPE type) {
        UserTimeout userTimeout = timeouts.get(userID);
        if (userTimeout == null) return null;
        Long timeout = null;
        switch(type) {
            case WORK -> timeout = userTimeout.getWorkTimeout();
            case SLUT -> timeout = userTimeout.getSlutTimeout();
            case CRIME -> timeout = userTimeout.getCrimeTimeout();
            case ROB -> timeout = userTimeout.getRobTimeout();
        }
        return timeout;
    }

    /**
     * Gets the time remaining on user timeout in string form.
     * Formatted as timestamp for embeds.
     *
     * @param userID the ID of the user.
     * @return String timeout formatted for embeds.
     */
    public @Nullable String getTimeoutFormatted(long userID, TIMEOUT_TYPE type) {
        Long timeout = getTimeout(userID, type);
        if (timeout == null) return null;
        return TimeFormat.RELATIVE.format(timeout);
    }

    /**
     * The different types of timeouts
     */
    public enum TIMEOUT_TYPE {
        WORK, SLUT, CRIME, ROB
    }
}
