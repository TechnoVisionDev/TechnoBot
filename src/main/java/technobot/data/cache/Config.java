package technobot.data.cache;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * POJO object that stores config data for a guild.
 *
 * @author TechnoVision
 */
public class Config {

    private long guild;

    private Long premium;

    @BsonProperty("leveling_channel")
    private Long levelingChannel;

    @BsonProperty("leveling_message")
    private String levelingMessage;

    @BsonProperty("leveling_dm")
    private boolean levelingDM;

    @BsonProperty("leveling_mod")
    private int levelingMod;

    @BsonProperty("leveling_mute")
    private boolean levelingMute;

    @BsonProperty("leveling_background")
    private String levelingBackground;

    private Map<String,Integer> rewards;

    @BsonProperty("auto_roles")
    private Set<Long> autoRoles;

    private String currency;

    private Map<String, Item> shop;

    public Config() {
        autoRoles = new HashSet<>();
        shop = new HashMap<>();
    }

    public Config(long guild) {
        this.guild = guild;
        this.premium = null;
        this.levelingChannel = null;
        this.levelingMessage = null;
        this.levelingDM = false;
        this.levelingMod = 1;
        this.levelingMute = false;
        this.levelingBackground = null;
        this.rewards = new HashMap<>();
        this.autoRoles = new HashSet<>();
    }

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public Long getPremium() {
        return premium;
    }

    public void setPremium(Long premium) {
        this.premium = premium;
    }

    public Long getLevelingChannel() {
        return levelingChannel;
    }

    public void setLevelingChannel(Long levelingChannel) {
        this.levelingChannel = levelingChannel;
    }

    public String getLevelingMessage() {
        return levelingMessage;
    }

    public void setLevelingMessage(String levelingMessage) {
        this.levelingMessage = levelingMessage;
    }

    public boolean isLevelingDM() {
        return levelingDM;
    }

    public void setLevelingDM(boolean levelingDM) {
        this.levelingDM = levelingDM;
    }

    public int getLevelingMod() {
        return levelingMod;
    }

    public void setLevelingMod(int levelingMod) {
        this.levelingMod = levelingMod;
    }

    public boolean isLevelingMute() {
        return levelingMute;
    }

    public void setLevelingMute(boolean levelingMute) {
        this.levelingMute = levelingMute;
    }

    public String getLevelingBackground() {
        return levelingBackground;
    }

    public void setLevelingBackground(String levelingBackground) {
        this.levelingBackground = levelingBackground;
    }

    public Map<String,Integer> getRewards() {
        return rewards;
    }

    public void setRewards(Map<String, Integer> rewards) {
        this.rewards = rewards;
    }

    public void addReward(int level, String roleID) {
        this.rewards.put(roleID, level);
    }

    public void removeReward(String roleID) { this.rewards.remove(roleID); }

    public Set<Long> getAutoRoles() {
        return autoRoles;
    }

    public void setAutoRoles(Set<Long> autoRoles) {
        this.autoRoles = autoRoles;
    }

    public void addAutoRole(long roleID) {
        this.autoRoles.add(roleID);
    }

    public void removeAutoRole(long roleID) { this.autoRoles.remove(roleID); }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Map<String, Item> getShop() {
        return shop;
    }

    public void setShop(Map<String, Item> shop) {
        this.shop = shop;
    }

    public void addItem(Item item) {
        this.shop.put(item.getName().toLowerCase(), item);
    }
}
