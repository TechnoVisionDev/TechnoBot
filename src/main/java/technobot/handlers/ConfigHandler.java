package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.Config;
import technobot.data.cache.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles config data for the guild and various modules.
 *
 * @author TechnoVision
 */
public class ConfigHandler {

    private static final ScheduledExecutorService expireScheduler = Executors.newScheduledThreadPool(10);
    private static final Map<String, ScheduledFuture> expireTimers = new HashMap<>();

    private final Guild guild;
    private final TechnoBot bot;
    private final Bson filter;
    private Config config;

    public ConfigHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;

        // Get POJO object from database
        this.filter = Filters.eq("guild", guild.getIdLong());
        this.config = bot.database.config.find(filter).first();
        if (this.config == null) {
            this.config = new Config(guild.getIdLong());
            bot.database.config.insertOne(config);
        }

        // Start any item expiration timers
        for (Item item : this.getConfig().getItems().values()) {
            if (item.getExpireTimestamp() != null) {
                long hours = 1 + ((item.getExpireTimestamp() - System.currentTimeMillis()) / 3600000);
                expireTimers.put(item.getUuid(), expireScheduler.schedule(() -> {
                    removeItem(item.getName());
                    expireTimers.remove(item.getUuid());
                }, hours, TimeUnit.HOURS));
            }
        }
    }

    /**
     * Access the config cache.
     *
     * @return a cache instance of the Config from database.
     */
    public Config getConfig() { return config; }

    /**
     * Check if guild has premium or if the premium has expired.
     *
     * @return true if guild is premium, otherwise false.
     */
    public boolean isPremium() {
        Long premiumTimestamp = bot.database.config.find(filter).first().getPremium();
        return premiumTimestamp != null && premiumTimestamp >= System.currentTimeMillis();
    }

    /**
     * Adds an auto role to local cache and database.
     *
     * @param roleID the ID of the role to be given on join.
     */
    public void addAutoRole(long roleID) {
        config.addAutoRole(roleID);
        bot.database.config.updateOne(filter, Updates.addToSet("auto_roles", roleID));
    }

    /**
     * Removes an auto role from the local cache and database.
     *
     * @param roleID the ID of the role to be removed from auto-role list.
     */
    public void removeAutoRole(long roleID) {
        config.removeAutoRole(roleID);
        bot.database.config.updateOne(filter, Updates.pull("auto_roles", roleID));
    }

    /**
     * Removes all auto roles from the server.
     */
    public void clearAutoRoles() {
        config.getAutoRoles().clear();
        bot.database.config.updateOne(filter, Updates.unset("auto_roles"));
    }

    /**
     * Adds an item to the economy shop.
     * Adds it to local cache and database config file.
     *
     * @param item the item object to be added.
     */
    public Item addItem(Item item) {
        config.addItem(item);
        bot.database.config.updateOne(filter, Updates.set("shop."+item.getName().toLowerCase(), item.getUuid()));
        bot.database.config.updateOne(filter, Updates.set("items."+item.getUuid(), item));
        updateExpireTimer(item);
        return item;
    }

    /**
     * Checks if the guild shop contains an item with the same name.
     * All item keys are stored as lowercase, even if the actual name is not.
     *
     * @param name the name of the item to search for.
     * @return true if item name exists, otherwise false.
     */
    public boolean containsItem(String name) {
        try {
            return config.getShop().containsKey(name.toLowerCase());
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Removes a shop item from the local cache and database.
     *
     * @param name the name of the object to remove.
     */
    public void removeItem(String name) {
        Item item = config.removeItem(name);
        cancelExpireTimer(item.getUuid());
        bot.database.config.updateOne(filter, Updates.unset("shop."+name.toLowerCase()));
    }

    /**
     * Removes an item from the database entirely.
     * Item will no longer be usable or purchasable.
     *
     * @param name the name of the object to erase.
     */
    public void eraseItem(String name) {
        Item item = config.eraseItem(name);
        cancelExpireTimer(item.getUuid());
        bot.database.config.updateOne(filter, Updates.unset("shop."+name.toLowerCase()));
        bot.database.config.updateOne(filter, Updates.unset("items."+item.getUuid()));
    }

    /**
     * Updates an item with new data in the local cache and database.
     *
     * @param item the item object to replace the existing item of the same name.
     */
    public void updateItem(Item item) {
        config.addItem(item);
        bot.database.config.updateOne(filter, Updates.set("items."+item.getUuid(), item));
        updateExpireTimer(item);
    }

    /**
     * Updates the expiration timer for an item. This may include canceling or rescheduling.
     *
     * @param item the item whose expiration timer to modify.
     */
    private void updateExpireTimer(Item item) {
        if (item.getExpireTimestamp() != null) {
            // Check for existing timer and cancel
            String id = item.getUuid();
            cancelExpireTimer(id);

            // Set new timer
            long hours = 1 + ((item.getExpireTimestamp() - System.currentTimeMillis()) / 3600000);
            expireTimers.put(id, expireScheduler.schedule(() -> {
                removeItem(item.getName());
                expireTimers.remove(id);
            }, hours, TimeUnit.HOURS));
        } else {
            ScheduledFuture task = expireTimers.get(item.getUuid());
            if (task != null) task.cancel(true);
        }
    }

    /**
     * Cancels an item's expiration timer.
     *
     * @param itemID the UUID of the item whose expiration timer to cancel.
     */
    private void cancelExpireTimer(String itemID) {
        ScheduledFuture task = expireTimers.remove(itemID);
        if (task != null) task.cancel(true);
    }

    /**
     * Retrieves a shop item by name from the local cache.
     *
     * @param name the name of the item to retrieve.
     * @return an Item object.
     */
    public Item getItem(String name) {
        String uuid = config.getShop().get(name.toLowerCase());
        return config.getItems().get(uuid);
    }

    /**
     * Retrieves a shop item by UUID from the local cache.
     *
     * @param uuid the UUID of the item to retrieve.
     * @return an Item object.
     */
    public Item getItemByID(String uuid) {
        return config.getItems().get(uuid);
    }

    /**
     * Reset all leveling config settings to their default.
     */
    public void resetLevelingConfig() {
        config.setLevelingDM(false);
        config.setLevelingMute(false);
        config.setLevelingMod(1);
        config.setLevelingBackground(null);
        config.setLevelingMessage(null);
        config.setLevelingChannel(null);
        config.getRewards().clear();
        bot.database.config.replaceOne(filter, config);
    }
}
