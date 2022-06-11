package technobot.listeners;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import technobot.data.GuildData;
import technobot.handlers.GreetingHandler;
import technobot.util.placeholders.Placeholder;
import technobot.util.placeholders.PlaceholderFactory;

/**
 * Listens for member join and leave to manage greetings.
 *
 * @author TechnoVision
 */
public class GreetingListener extends ListenerAdapter {

    /**
     * Triggers every time a user joins a guild.
     * Sends a greeting message to the guild's welcome channel.
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GreetingHandler greetingHandler = GuildData.get(event.getGuild()).greetingHandler;
        // Get welcome channel
        Long channelID = greetingHandler.getChannel();
        if (channelID == null) return;

        // Get greet message
        String message = greetingHandler.getGreet();
        if (message == null || message.isEmpty()) return;

        // Send greet message
        Placeholder placeholder = PlaceholderFactory.fromEvent(event).get();
        MessageChannel channel = event.getGuild().getChannelById(MessageChannel.class, channelID);
        if (channel != null) {
            channel.sendMessage(placeholder.parse(message)).queue();
        }

        // Send join DM message
        String joinDM = greetingHandler.getJoinDM();
        if (joinDM != null) {
            event.getUser().openPrivateChannel().flatMap(dm -> dm.sendMessage(placeholder.parse(joinDM))).queue(null, fail -> { });
        }
    }

    /**
     * Triggers every time a user leaves a guild.
     * Sends a farewell to the guild's welcome channel.
     */
    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        GreetingHandler greetingHandler = GuildData.get(event.getGuild()).greetingHandler;
        if (greetingHandler.getChannel() == null || greetingHandler.getFarewell() == null) return;

        // send farewell message
        MessageChannel channel = event.getGuild().getChannelById(MessageChannel.class, greetingHandler.getChannel());
        if (channel != null) {
            Placeholder placeholder = PlaceholderFactory.fromEvent(event).get();
            channel.sendMessage(placeholder.parse(greetingHandler.getFarewell())).queue();
        }
    }
}
