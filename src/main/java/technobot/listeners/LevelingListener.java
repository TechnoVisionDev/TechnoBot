package technobot.listeners;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Leveling;
import technobot.util.placeholders.Placeholder;
import technobot.util.placeholders.PlaceholderFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener that handles leveling system backend.
 *
 * @author TechnoVision
 */
public class LevelingListener extends ListenerAdapter {

    public static final String DEFAULT_LEVEL_MESSAGE = "Congrats {user_mention}, you just advanced to **Level {level}**! :tada:";

    private final TechnoBot bot;

    /**
     * Constructor to get TechnoBot shard instance.
     *
     * @param bot instance of TechnoBot.
     */
    public LevelingListener(TechnoBot bot) {
        this.bot = bot;
    }

    /**
     * Members earn XP for each message sent on a cooldown.
     *
     * @param event Each message sent in the guild.
     */
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        // Check if user and message is valid
        if (event.getAuthor().isBot()) { return; }
        GuildData data = GuildData.get(event.getGuild());

        // Access MongoDB data file
        long userID = event.getAuthor().getIdLong();
        long guildID = event.getGuild().getIdLong();
        Bson filter = Filters.and(Filters.eq("guild", guildID), Filters.eq("user", userID));
        Leveling profile = bot.database.leveling.find(filter).first();

        if (profile == null) {
            profile = new Leveling(guildID, userID);
            bot.database.leveling.insertOne(profile);
            data.levelingHandler.addProfile(profile);
        }

        // Calculate and add XP
        long exactMilli = event.getMessage().getTimeCreated().toInstant().toEpochMilli();
        if (exactMilli - 60000 >= profile.getTimestamp()) {
            // Calculate XP
            int xpIncrease = ThreadLocalRandom.current().nextInt(10) + 15;
            long xp = profile.getXp() + xpIncrease;
            long totalXP = profile.getTotalXP() + xpIncrease;
            int level = profile.getLevel();

            // Check for Level Up
            boolean levelUp = false;
            List<Bson> updates = new ArrayList<>();
            int result = data.levelingHandler.calculateLevelGoal(level);
            if (xp >= result) {
                xp -= result;
                level++;
                levelUp = true;
                updates.add(Updates.set("level", level));
            }

            // Update MongoDB data file
            updates.add(Updates.set("timestamp", exactMilli));
            updates.add(Updates.set("xp", xp));
            updates.add(Updates.set("total_xp", totalXP));
            bot.database.leveling.updateOne(filter, updates);

            // Update leaderboard
            profile.setXp(xp);
            profile.setTotalXP(totalXP);
            profile.setLevel(level);
            data.levelingHandler.updateLeaderboard(profile);

            if (levelUp) {
                // Check for mute
                if (data.config.isLevelingMute()) return;

                // Check for leveling modulus
                if (profile.getLevel() % data.config.getLevelingMod() != 0) return;

                // Parse level-up message for placeholders
                Placeholder placeholder = PlaceholderFactory.fromLevelingEvent(event, profile).get();
                String levelingMessage = (data.config.getLevelingMessage() != null) ? data.config.getLevelingMessage() : DEFAULT_LEVEL_MESSAGE;
                placeholder.parse(levelingMessage);

                // Send level-up message in DMs
                if (data.config.isLevelingDM()) {
                    event.getAuthor().openPrivateChannel().queue(dm -> dm.sendMessage(levelingMessage).queue());
                    return;
                }

                // Send level-up message in channel
                if (data.config.getLevelingChannel() != null) {
                    TextChannel channel = event.getGuild().getTextChannelById(data.config.getLevelingChannel());
                    if (channel == null) { channel = event.getTextChannel(); }
                    channel.sendMessage(levelingMessage).queue();
                } else {
                    event.getChannel().sendMessage(levelingMessage).queue();
                }
            }
        }
    }
}
