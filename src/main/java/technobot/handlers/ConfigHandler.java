package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.Config;
import technobot.data.cache.Item;

import java.util.LinkedHashMap;

/**
 * Handles config data for the guild and various modules.
 *
 * @author TechnoVision
 */
public class ConfigHandler {

    private final Guild guild;
    private final TechnoBot bot;
    private final Bson filter;
    private Config config;
    private LinkedHashMap<String, Item> shopUUIDs;

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

        // Map shop UUIDs to items
        shopUUIDs = new LinkedHashMap<>();
        for (Item item : config.getShop().values()) {
            shopUUIDs.put(item.getUuid(), item);
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
     * Adds an existing item to the economy shop.
     * Adds it to local cache and database config file.
     *
     * @param item the item object to be added.
     */
    public Item addItem(Item item) {
        config.addItem(item);
        shopUUIDs.put(item.getUuid(), item);
        bot.database.config.updateOne(filter, Updates.set("shop."+item.getName().toLowerCase(), item));
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
        shopUUIDs.remove(item.getUuid());
        bot.database.config.updateOne(filter, Updates.unset("shop."+name.toLowerCase()));
    }

    /**
     * Updates an item with new data in the local cache and database.
     *
     * @param item the item object to replace the existing item of the same name.
     */
    public void updateItem(Item item) {
        config.addItem(item);
        bot.database.config.updateOne(filter, Updates.set("shop."+item.getName().toLowerCase(), item));
    }

    /**
     * Retrieves a shop item by name from the local cache.
     *
     * @param name the name of the item to retrieve.
     * @return an Item object.
     */
    public Item getItem(String name) {
        return config.getShop().get(name.toLowerCase());
    }

    /**
     * Retrieves a shop item by UUID from the local cache.
     *
     * @param uuid the UUID of the item to retrieve.
     * @return an Item object.
     */
    public Item getItemByID(String uuid) {
        return shopUUIDs.get(uuid);
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
