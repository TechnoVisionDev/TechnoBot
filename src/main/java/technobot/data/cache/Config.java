package technobot.data.cache;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * POJO object that stores config data for a guild.
 *
 * @author TechnoVision
 */
public class Config {

    private long guild;

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

    public Config() { }

    public Config(long guild) {
        this.guild = guild;
        levelingChannel = null;
        levelingMessage = null;
        levelingDM = false;
        levelingMod = 1;
        levelingMute = false;
        levelingBackground = null;
        rewards = new HashMap<>();
    }

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
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
}
