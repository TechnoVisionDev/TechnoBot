package technobot.data.cache.moderation;

import kotlin.Pair;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.*;

/**
 * POJO object that stores warnings and ban timers.
 *
 * @author TechnoVision
 */
public class Moderation {

    private long guild;
    private int total;
    private int count;
    @BsonProperty("mute_role")
    private Long muteRole;
    private HashMap<String, Ban> bans;
    private HashMap<String, List<Warning>> warnings;
    private Set<Long> mutes;

    public Moderation() {
        mutes = new HashSet<>();
    }

    public Moderation(long guild) {
        this.guild = guild;
        this.bans = new HashMap<>();
        this.warnings = new HashMap<>();
        this.mutes = new HashSet<>();
        this.total = 0;
        this.count = 0;
    }

    /**
     * Add a user ID to the mute list
     *
     * @param userID the ID of the user to mute.
     */
    public void addMute(long userID) {
        mutes.add(userID);
    }

    /**
     * Remove a user ID from the mute list
     *
     * @param userID the ID of the user to unmute.
     */
    public void removeMute(long userID) {
        mutes.remove(userID);
    }

    /**
     * Adds a warning to the database.
     *
     * @param target the ID of the user receiving this warning.
     * @param warning The warning to add to the database.
     */
    public void addWarning(long target, Warning warning) {
        String targetString = String.valueOf(target);
        if (warnings.containsKey(targetString)) {
            warnings.get(targetString).add(warning);
        } else {
            List<Warning> list = new ArrayList<>();
            list.add(warning);
            warnings.put(targetString, list);
        }
    }

    /**
     * Clear all warnings from the local cache and database.
     *
     * @param target the ID of the user to target.
     * @return the number of warnings cleared.
     */
    public int clearWarnings(long target) {
        try {
            String targetString = String.valueOf(target);
            int count = warnings.get(targetString).size();
            warnings.remove(targetString);
            this.count -= count;
            return count;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    /**
     * Removes a warning with a specific ID from the local cache and database.
     *
     * @param id the ID of the warning to target.
     * @return a pair containing the user's ID and the index of the warning cleared.
     */
    public Pair<String, Integer> removeWarning(int id) {
        for (Map.Entry<String, List<Warning>> entry : warnings.entrySet()) {
            List<Warning> warnings = entry.getValue();
            for (int i = 0; i < warnings.size(); i++) {
                Warning w = warnings.get(i);
                if (w.getId() == id) {
                    this.warnings.get(entry.getKey()).remove(w);
                    this.count--;
                    return new Pair<>(entry.getKey(), i);
                }
            }
        }
        return new Pair<>("null", -1);
    }

    /**
     * Adds a timed ban to the local cache.
     *
     * @param target the string ID of the user to check.
     * @param ban an instance of the user ban.
     */
    public void addBan(String target, Ban ban) {
        bans.put(target, ban);
    }

    /**
     * Removes a timed ban from the local cache.
     *
     * @param target the string ID of the user to check.
     */
    public void removeBan(String target) {
        bans.remove(target);
    }

    /** Getters and Setters for POJO **/

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Long getMuteRole() {
        return muteRole;
    }

    public void setMuteRole(Long muteRole) {
        this.muteRole = muteRole;
    }

    public HashMap<String, Ban> getBans() {
        return bans;
    }

    public void setBans(HashMap<String, Ban> bans) {
        this.bans = bans;
    }

    public HashMap<String, List<Warning>> getWarnings() {
        return warnings;
    }

    public void setWarnings(HashMap<String, List<Warning>> warnings) {
        this.warnings = warnings;
    }

    public Set<Long> getMutes() {
        return mutes;
    }

    public void setMutes(Set<Long> mutes) {
        this.mutes = mutes;
    }
}
