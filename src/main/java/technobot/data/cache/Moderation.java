package technobot.data.cache;

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
            warnings.put(targetString, List.of(warning));
        }
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
