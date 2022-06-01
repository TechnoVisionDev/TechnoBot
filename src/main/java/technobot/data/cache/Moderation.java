package technobot.data.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * POJO object that stores warnings and ban timers.
 *
 * @author TechnoVision
 */
public class Moderation {

    private long guild;
    private int total;
    private int count;
    private HashMap<String, List<Warning>> warnings;

    public Moderation() { }

    public Moderation(long guild) {
        this.guild = guild;
        this.warnings = new HashMap<>();
        this.total = 0;
        this.count = 0;
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
     * @return the number of warnings cleared.
     */
    public int removeWarning(int id) {
        for (List<Warning> warnings : warnings.values()) {
            for (Warning w : warnings) {
                if (w.getId() == id) {
                    warnings.remove(w);
                    this.count--;
                    return 1;
                }
            }
        }
        return 0;
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

    public HashMap<String, List<Warning>> getWarnings() {
        return warnings;
    }

    public void setWarnings(HashMap<String, List<Warning>> warnings) {
        this.warnings = warnings;
    }
}
