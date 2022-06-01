package technobot.data.cache;

/**
 * POJO object that represents a warning given by a moderator.
 *
 * @author TechnoVision
 */
public class Warning {

    /** ID of this warning **/
    private int id;

    /** Timestamp in milliseconds when this warning was given **/
    private long timestamp;

    /** The reason for this warning **/
    private String reason;

    /** The target who received this warning **/
    private long user;

    /** ID of the staff who made this warning **/
    private long staff;

    public Warning() { }

    public Warning(int id, long timestamp, String reason, long user, long staff) {
        this.id = id;
        this.timestamp = timestamp;
        this.reason = reason;
        this.user = user;
        this.staff = staff;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getStaff() {
        return staff;
    }

    public void setStaff(long staff) {
        this.staff = staff;
    }
}
