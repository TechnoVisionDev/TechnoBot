package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.Guild;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.Greetings;

/**
 * Handles server messages (greeting, farewell, joinDM, etc).
 *
 * @author TechnoVision
 */
public class GreetingHandler {

    private final Guild guild;
    private final TechnoBot bot;
    private final Bson filter;
    private Greetings greetings;

    public GreetingHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;

        // Get POJO object from database
        filter = Filters.eq("guild", guild.getIdLong());
        this.greetings = bot.database.greetings.find(filter).first();
        if (greetings == null) {
            greetings = new Greetings(guild.getIdLong());
            bot.database.greetings.insertOne(greetings);
        }
    }

    /**
     * Set a greeting message for this server.
     *
     * @param msg the message to send on join.
     */
    public void setGreet(String msg) {
        greetings.setGreeting(msg);
        bot.database.greetings.updateOne(filter, Updates.set("greeting", msg));
    }

    /**
     * Remove greeting message from this server.
     */
    public void removeGreet() {
        greetings.setGreeting(null);
        bot.database.greetings.updateOne(filter, Updates.unset("greeting"));
    }

    /**
     * Gets the greet message.
     *
     * @return string greet message.
     */
    public String getGreet() {
        return greetings.getGreeting();
    }

    /**
     * Set a farewell message for this server.
     *
     * @param msg the message to send on member leave.
     */
    public void setFarewell(String msg) {
        greetings.setFarewell(msg);
        bot.database.greetings.updateOne(filter, Updates.set("farewell", msg));
    }

    /**
     * Remove farewell message from this server.
     */
    public void removeFarewell() {
        greetings.setGreeting(null);
        bot.database.greetings.updateOne(filter, Updates.unset("farewell"));
    }

    /**
     * Gets the farewell message.
     *
     * @return string farewell message.
     */
    public String getFarewell() {
        return greetings.getFarewell();
    }

    /**
     * Set a join DM message for this server.
     *
     * @param msg the message to send on member leave.
     */
    public void setJoinDM(String msg) {
        greetings.setJoinDM(msg);
        bot.database.greetings.updateOne(filter, Updates.set("join_dm", msg));
    }

    /**
     * Remove join DM message from this server.
     */
    public void removeJoinDM() {
        greetings.setJoinDM(null);
        bot.database.greetings.updateOne(filter, Updates.unset("join_dm"));
    }

    /**
     * Gets the join DM message.
     *
     * @return string farewell message.
     */
    public String getJoinDM() {
        return greetings.getJoinDM();
    }

    /**
     * Set the welcome channel.
     *
     * @param channelID the ID of the channel to set.
     */
    public void setChannel(Long channelID) {
        greetings.setWelcomeChannel(channelID);
        bot.database.greetings.updateOne(filter, Updates.set("welcome_channel", channelID));
    }

    /**
     * Remove the welcome channel.
     */
    public void removeChannel() {
        greetings.setWelcomeChannel(null);
        bot.database.greetings.updateOne(filter, Updates.unset("welcome_channel"));
    }

    /**
     * Gets the ID of the welcome channel.
     *
     * @return long id of the welcome channel.
     */
    public Long getChannel() {
        return greetings.getWelcomeChannel();
    }

    /**
     * Getter for greetings config POJO object.
     *
     * @return instance of greetings config POJO.
     */
    public Greetings getConfig() {
        return greetings;
    }
}
