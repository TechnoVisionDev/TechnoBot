package technobot.handlers.economy;

/**
 * Represents timeout timestamps for economy backend.
 *
 * @author TechnoVision
 */
public class UserTimeout {

    private final long user;
    private Long workTimeout;
    private Long slutTimeout;
    private Long crimeTimeout;
    private Long robTimeout;

    public UserTimeout(long user) {
        this.user = user;
    }

    public long getUser() {
        return user;
    }

    public Long getWorkTimeout() {
        return workTimeout;
    }

    public void setWorkTimeout(Long workTimeout) {
        this.workTimeout = workTimeout;
    }

    public Long getSlutTimeout() {
        return slutTimeout;
    }

    public void setSlutTimeout(Long slutTimeout) {
        this.slutTimeout = slutTimeout;
    }

    public Long getCrimeTimeout() {
        return crimeTimeout;
    }

    public void setCrimeTimeout(Long crimeTimeout) {
        this.crimeTimeout = crimeTimeout;
    }

    public Long getRobTimeout() {
        return robTimeout;
    }

    public void setRobTimeout(Long robTimeout) {
        this.robTimeout = robTimeout;
    }
}
