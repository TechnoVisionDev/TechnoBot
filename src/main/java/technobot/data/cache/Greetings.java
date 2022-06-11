package technobot.data.cache;

import org.bson.codecs.pojo.annotations.BsonProperty;

/**
 * POJO object that stores server greeting/farewell data.
 *
 * @author TechnoVision
 */
public class Greetings {

    private long guild;

    @BsonProperty("welcome_channel")
    private Long welcomeChannel;

    private String greeting;

    private String farewell;

    @BsonProperty("join_dm")
    private String joinDM;

    public Greetings() { }

    public Greetings(long guild) {
        this.guild = guild;
    }

    public long getGuild() {
        return guild;
    }

    public void setGuild(long guild) {
        this.guild = guild;
    }

    public Long getWelcomeChannel() {
        return welcomeChannel;
    }

    public void setWelcomeChannel(Long welcomeChannel) {
        this.welcomeChannel = welcomeChannel;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getFarewell() {
        return farewell;
    }

    public void setFarewell(String farewell) {
        this.farewell = farewell;
    }

    public String getJoinDM() {
        return joinDM;
    }

    public void setJoinDM(String joinDM) {
        this.joinDM = joinDM;
    }
}
