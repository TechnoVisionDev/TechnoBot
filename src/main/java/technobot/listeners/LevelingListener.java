package technobot.listeners;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Leveling;
import technobot.util.CommandUtils;
import technobot.util.placeholders.Placeholder;
import technobot.util.placeholders.PlaceholderFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener that handles leveling system backend.
 *
 * @author TechnoVision
 */
public class LevelingListener extends ListenerAdapter {

    public static final String DEFAULT_LEVEL_MESSAGE = "Congrats {user_mention}, you just advanced to **Level {level}**! :tada:";

    private final TechnoBot bot;
    private final HashMap<Long, Long> timestamps;

    /**
     * Constructor to get TechnoBot shard instance.
     *
     * @param bot instance of TechnoBot.
     */
    public LevelingListener(TechnoBot bot) {
        this.bot = bot;
        this.timestamps = new HashMap<>();
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
        if (event.getChannelType() == ChannelType.PRIVATE) { return; }
        GuildData data = GuildData.get(event.getGuild());
        long userID = event.getAuthor().getIdLong();
        long guildID = event.getGuild().getIdLong();

        // Check if user is on cooldown
        long exactMilli = event.getMessage().getTimeCreated().toInstant().toEpochMilli();
        Long timestamp = timestamps.get(userID);
        if (timestamp != null && exactMilli - 60000 < timestamp) return;
        timestamps.put(userID, exactMilli);

        // Get profile from MongoDB
        Bson filter = Filters.and(Filters.eq("guild", guildID), Filters.eq("user", userID));
        Leveling profile = bot.database.leveling.find(filter).first();
        if (profile == null) {
            profile = new Leveling(guildID, userID);
            bot.database.leveling.insertOne(profile);
        }

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
            profile.setLevel(level);
        }

        // Update MongoDB data file
        updates.add(Updates.set("xp", xp));
        updates.add(Updates.set("total_xp", totalXP));
        profile.setXp(xp);
        profile.setTotalXP(totalXP);
        bot.database.leveling.updateOne(filter, updates);

        if (levelUp) {
            // Give reward roles
            Member member = event.getMember();
            List<Role> memberRoles = member.getRoles();
            for (Map.Entry<String,Integer> reward : data.config.getRewards().entrySet()) {
                // Check for required level
                int rewardLevel = reward.getValue();
                if (level >= rewardLevel) {
                    // Only give role if user doesn't already have it
                    Role role = event.getGuild().getRoleById(reward.getKey());
                    Role botRole = event.getGuild().getBotRole();
                    if (role != null && !memberRoles.contains(role) && !role.isManaged()) {
                        if (role.getPosition() < botRole.getPosition() && CommandUtils.hasPermission(event.getGuild().getBotRole(), Permission.MANAGE_ROLES)) {
                            event.getGuild().addRoleToMember(event.getAuthor(), role).queue();
                        }
                    }
                }
            }

            // Check for mute and modulus
            if (data.config.isLevelingMute()) return;
            if (profile.getLevel() % data.config.getLevelingMod() != 0) return;

            // Parse level-up message for placeholders
            Placeholder placeholder = PlaceholderFactory.fromLevelingEvent(event, profile).get();
            String levelingMessage = (data.config.getLevelingMessage() != null) ? data.config.getLevelingMessage() : DEFAULT_LEVEL_MESSAGE;
            String parsedMessage = placeholder.parse(levelingMessage);

            // Send level-up message in DMs
            if (data.config.isLevelingDM()) {
                event.getAuthor().openPrivateChannel().queue(dm -> dm.sendMessage(parsedMessage).queue());
                return;
            }

            // Send level-up message in channel
            if (data.config.getLevelingChannel() != null) {
                TextChannel channel = event.getGuild().getTextChannelById(data.config.getLevelingChannel());
                if (channel == null) { channel = event.getTextChannel(); }
                channel.sendMessage(parsedMessage).queue();
            } else {
                event.getChannel().sendMessage(parsedMessage).queue();
            }
        }
    }
}
