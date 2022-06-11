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
}
