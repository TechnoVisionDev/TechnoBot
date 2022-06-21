package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.Config;

import java.util.Set;

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
     * @param roleID the ID of the role to be reset from auto-role list.
     */
    public void removeAutoRole(long roleID) {
        config.removeAutoRole(roleID);
        bot.database.config.updateOne(filter, Updates.pull("auto_roles", roleID));
    }

    public void clearAutoRoles() {
        config.getAutoRoles().clear();
        bot.database.config.updateOne(filter, Updates.unset("auto_roles"));
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
