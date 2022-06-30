package technobot.handlers.economy;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.*;
import com.mongodb.lang.Nullable;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Economy;
import technobot.data.cache.Item;
import technobot.handlers.ConfigHandler;
import technobot.util.embeds.EmbedUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the server economy backend.
 *
 * @author TechnoVision
 */
public class EconomyHandler {

    public static final String DEFAULT_CURRENCY = "\uD83E\uDE99";
    public static final long WORK_TIMEOUT = 14400000;
    public static final long ROB_TIMEOUT = 86400000;
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);
    private static final EconomyLocalization responses = new EconomyLocalization();

    private final Guild guild;
    private final TechnoBot bot;
    private final Bson guildFilter;
    private final Map<Long, UserTimeout> timeouts;
    private String currency;

    public EconomyHandler(TechnoBot bot, Guild guild, ConfigHandler configHandler) {
        this.bot = bot;
        this.guild = guild;
        this.guildFilter = Filters.eq("guild", guild.getIdLong());
        this.timeouts = new HashMap<>();

        // Set currency symbol
        this.currency = configHandler.getConfig().getCurrency();
        if (currency == null) {
            this.currency = DEFAULT_CURRENCY;
        }
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
        return responses.getWorkResponse(amount, getCurrency());
    }

    /**
     * 40% chance to add 250-700 to user's balance.
     * 60% chance to lose 20%-40% of user's balance.
     *
     * @param userID the ID of user whose balance to add to.
     * @return an EconomyReply object with response, ID number, and success boolean.
     */
    public EconomyReply crime(long userID) {
        long amount;
        EconomyReply reply;
        if (ThreadLocalRandom.current().nextInt(100) <= 40) {
            // Crime successful
            amount = ThreadLocalRandom.current().nextInt(450) + 250;
            addMoney(userID, amount);
            reply = responses.getCrimeSuccessResponse(amount, getCurrency());
        } else {
            // Crime failed
            amount = calculateFine(userID);
            if (amount > 0) removeMoney(userID, amount);
            reply = responses.getCrimeFailResponse(amount, getCurrency());
        }
        setTimeout(userID, TIMEOUT_TYPE.CRIME);
        return reply;
    }

    /**
     * Attempt to steal another user's cash.
     *
     * @param userID the user attempting the robbery.
     * @param targetID the target being robbed.
     * @return an EconomyReply object with a response and success boolean.
     */
    public EconomyReply rob(long userID, long targetID) {
        // Calculate probability of failure (your networth / (their cash + your networth))
        long userNetworth = getNetworth(userID);
        long targetCash = getBalance(targetID);
        double failChance = (double) userNetworth / (targetCash + userNetworth);
        if (failChance < 0.20) {
            failChance = 0.20;
        } else if (failChance > 0.80) {
            failChance = 0.80;
        }

        // Calculate mount stolen (success probability * their cash)
        long amountStolen = (long) ((1 - failChance) * targetCash);
        if (amountStolen < 0) amountStolen = 0;

        // Attempt robbery
        setTimeout(userID, TIMEOUT_TYPE.ROB);
        if (ThreadLocalRandom.current().nextDouble() > failChance) {
            // Rob successful
            pay(targetID, userID, amountStolen);
            String value = getCurrency() + " " + EconomyHandler.FORMATTER.format(amountStolen);
            String response = EmbedUtils.GREEN_TICK + " You robbed " + value + " from <@" + targetID + ">";
            return new EconomyReply(response, 1, true);
        }
        // Rob failed (20-40% fine of net worth)
        long fine = calculateFine(userID);
        removeMoney(userID, fine);
        String value = getCurrency() + " " + EconomyHandler.FORMATTER.format(fine);
        String response = "You were caught attempting to rob <@"+targetID+">, and have been fined " + value + ".";
        return new EconomyReply(response, 1, false);
    }

    /**
     * Calculate fine for commands like /crime and /rob
     * Default fine is 20-40% of user's networth.
     *
     * @param userID the user to calculate fine for.
     * @return the calculated fine amount.
     */
    private long calculateFine(long userID) {
        long networth = getNetworth(userID);
        long fine = 0;
        if (networth > 0) {
            double percent = (ThreadLocalRandom.current().nextInt(20) + 20) * 0.01;
            fine = (long) (networth * percent);
        }
        return fine;
    }

    /**
     * Deposit money from balance into bank.
     *
     * @param userID the ID of user to deposit for.
     * @param amount the amount to deposit.
     */
    public void deposit(long userID, long amount) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        Bson update = Updates.inc("balance", -1 * amount);
        Bson update2 = Updates.inc("bank", amount);
        bot.database.economy.updateOne(filter, Filters.and(update, update2));
    }

    /**
     * Withdraw money from bank into balance.
     *
     * @param userID the ID of user to withdraw for.
     * @param amount the amount to withdraw.
     */
    public void withdraw(long userID, long amount) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        Bson update = Updates.inc("balance", amount);
        Bson update2 = Updates.inc("bank", -1 * amount);
        bot.database.economy.updateOne(filter, Filters.and(update, update2));
    }

    /**
     * Transfer money from one user to another.
     *
     * @param userID the user to transfer money from.
     * @param targetID the user to transfer money to.
     * @param amount the amount of money to transfer.
     */
    public void pay(long userID, long targetID, long amount) {
        removeMoney(userID, amount);
        addMoney(targetID, amount);
    }

    /**
     * Get a user's current cash balance.
     *
     * @param userID the ID of the user to get cash balance from.
     * @return the integer value of user's cash balance.
     */
    public long getBalance(long userID) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        Economy profile = bot.database.economy.find(filter).first();
        if (profile == null) return 0;
        Long balance = profile.getBalance();
        return (balance != null) ? balance : 0;
    }

    /**
     * Get a user's current bank balance.
     *
     * @param userID the ID of the user to get bank balance from.
     * @return the integer value of user's bank balance.
     */
    public long getBank(long userID) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        Economy profile = bot.database.economy.find(filter).first();
        if (profile == null) return 0;
        Long bank = profile.getBank();
        return (bank != null) ? bank : 0;
    }

    /**
     * Get a user's current networth (balance and bank added together).
     *
     * @param userID the ID of the user to get networth from.
     * @return the integer value of user's networth.
     */
    public long getNetworth(long userID) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        Economy profile = bot.database.economy.find(filter).first();
        if (profile == null) return 0;
        long balance = profile.getBalance() != null ? profile.getBalance() : 0;
        long bank = profile.getBank() != null ? profile.getBank() : 0;
        return balance + bank;
    }

    /**
     * Gets the rank of the specified user in their guild based on balance and bank.
     *
     * @param userID the ID of the user to get rank for.
     * @return integer ranking on this server.
     */
    public int getRank(long userID) {
        int rank = 1;
        for (Economy profile : getLeaderboard()) {
            if (profile.getUser() == userID) return rank;
            rank++;
        }
        return guild.getMemberCount();
    }

    /**
     * Get a sorted list of user economy data sorted by networth.
     *
     * @return iterable of user economy data sorted in descending order.
     */
    public AggregateIterable<Economy> getLeaderboard() {
        return bot.database.economy.aggregate(
                Arrays.asList(
                        Aggregates.match(Filters.eq("guild", guild.getIdLong())),
                        Aggregates.addFields(new Field("sum", Filters.eq("$add", Arrays.asList("$balance", Filters.eq("$ifNull", Arrays.asList("$bank", 0)))))),
                        Aggregates.sort(Sorts.descending("sum"))
                )
        );
    }

    /**
     * Get a user's current bank balance.
     *
     * @param userID the ID of the user to get bank balance from.
     * @return the integer value of user's bank balance.
     */
    public Economy getProfile(long userID) {
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        return bot.database.economy.find(filter).first();
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
        long time = System.currentTimeMillis() + WORK_TIMEOUT;
        UserTimeout userTimeout = timeouts.get(userID);
        if (userTimeout == null) {
            userTimeout = new UserTimeout(userID);
        }
        switch(type) {
            case WORK -> userTimeout.setWorkTimeout(time);
            case CRIME -> userTimeout.setCrimeTimeout(time);
            case ROB -> userTimeout.setRobTimeout(System.currentTimeMillis() + ROB_TIMEOUT);
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
        if (userTimeout == null) {
            return null;
        }
        Long timeout = null;
        switch(type) {
            case WORK -> timeout = userTimeout.getWorkTimeout();
            case CRIME -> timeout = userTimeout.getCrimeTimeout();
            case ROB -> timeout = userTimeout.getRobTimeout();
        }
        return timeout;
    }

    /**
     * Formats timeout timestamp into a string timestamp for embeds.
     *
     * @param timeout the timestamp in millis.
     * @return a string timeout formatted for embeds.
     */
    public @Nullable String formatTimeout(long timeout) {
        return TimeFormat.RELATIVE.format(timeout);
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
     * Sets the currency symbol to a custom emoji or string.
     *
     * @param symbol the string or emoji symbol.
     */
    public void setCurrency(String symbol) {
        bot.database.config.updateOne(guildFilter, Updates.set("currency", symbol));
        this.currency = symbol;
    }

    /**
     * Resets the currency symbol to the default emoji.
     */
    public void resetCurrency() {
        bot.database.config.updateOne(guildFilter, Updates.unset("currency"));
        this.currency = DEFAULT_CURRENCY;
    }

    /**
     * Get the currency symbol for this guild.
     *
     * @return string emoji for currency symbol.
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Add an item from the store to a user's inventory.
     *
     * @param userID the user buying this item.
     * @param item the item being purchased.
     */
    public void buyItem(long userID, Item item) {
        removeMoney(userID, item.getPrice());
        // Update user inventory in database
        Bson filter = Filters.and(guildFilter, Filters.eq("user", userID));
        if (item.getShowInInventory()) {
            bot.database.economy.updateOne(filter, Updates.inc("inventory." + item.getUuid(), 1));
        }
        // Decrement stock in shop
        if (item.getStock() != null) {
            item.setStock(item.getStock()-1);
            GuildData.get(guild).configHandler.updateItem(item);
        }
    }

    /**
     * Retrieves a user's inventory, which is a map of UUID counts.
     *
     * @param userID the ID of the user whose inventory is being retrieved.
     * @return a map of item UUIDS to integer counts.
     */
    public LinkedHashMap<String,Long> getInventory(long userID) {
        LinkedHashMap<String,Long> inv = getProfile(userID).getInventory();
        if (inv == null) return new LinkedHashMap<>();
        return inv;
    }

    /**
     * Counts the number of times this item appears in a user's inventory
     *
     * @param userID the ID of the user whose inventory to check.
     * @param itemID the UUID of the item to look for.
     * @return the amount of this item in user's inventory.
     */
    public long countItemInInventory(long userID, String itemID) {
        Long count = getInventory(userID).get(itemID);
        if (count == null) return 0;
        return count;
    }

    /**
     * Checks if a guild member meets the requirements to use an object.
     *
     * @param member a guild member instance for the user.
     * @param item the item to check against.
     * @return true if member can use item, otherwise false.
     */
    public boolean canUseItem(Member member, Item item) {
        // Check for required role
        if (item.getRequiredRole() != null) {
            Role requiredRole = guild.getRoleById(item.getRequiredRole());
            if (requiredRole != null && !member.getRoles().contains(requiredRole)) {
                return false;
            }
        }
        // Check for required balance
        if (item.getRequiredBalance() != null) {
            Long requiredBalance = item.getRequiredBalance();
            if (requiredBalance != null && getNetworth(member.getIdLong()) < requiredBalance) {
                return false;
            }
        }
        return true;
    }

    /**
     * Uses an item and gains/removes specified roles.
     *
     * @param member The guild member using this item.
     * @param item the item being used.
     * @param itemCount the amount of this item in member's inventory.
     */
    public void useItem(Member member, Item item, long itemCount) {
        // Give roles
        if (item.getGivenRole() != null) {
            Role givenRole = guild.getRoleById(item.getGivenRole());
            if (givenRole != null) {
                guild.addRoleToMember(member, givenRole).queue();
            }
        }
        // Remove roles
        if (item.getRemovedRole() != null) {
            Role removedRole = guild.getRoleById(item.getRemovedRole());
            if (removedRole != null) {
                guild.removeRoleFromMember(member, removedRole).queue();
            }
        }
        // Remove item from inventory
        if (itemCount > 0) {
            Bson filter = Filters.and(guildFilter, Filters.eq("user", member.getIdLong()));
            if (itemCount == 1) {
                bot.database.economy.updateOne(filter, Updates.unset("inventory." + item.getUuid()));
            } else {
                bot.database.economy.updateOne(filter, Updates.inc("inventory." + item.getUuid(), -1));
            }
        }
    }

    /**
     * The different types of timeouts
     */
    public enum TIMEOUT_TYPE {
        WORK, CRIME, ROB
    }
}
