package technobot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import technobot.util.embeds.EmbedColor;

import java.time.Instant;
import java.util.HashMap;

/**
 * Listens for message mentions and handles afk message responses.
 *
 * @author TechnoVision
 */
public class AfkListener extends ListenerAdapter {


    /** Map of User objects to a pair containing the AFK message to be sent and the time created */
    public static HashMap<Member, AfkStatus> AFK_MESSAGES = new HashMap<>();

    /**
     * Sends AFK messages if necessary.
     * Also removes users from AFK list if they sent a message.
     * @param event executes whenever a message in sent in chat.
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Check if AFK user has returned
        if (AFK_MESSAGES.containsKey(event.getMember())) {
            AFK_MESSAGES.remove(event.getMember());
            event.getMessage().addReaction("\uD83D\uDC4B").queue();
            return;
        }

        // Check mentions against AFK users
        for (Member member : event.getMessage().getMentions().getMembers()) {
            AfkStatus status = AFK_MESSAGES.get(member);
            if (status != null) {
                // Mentioned user was AFK -- send message response
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setAuthor(member.getEffectiveName()+" is currently AFK", null, member.getEffectiveAvatarUrl())
                        .setDescription(status.message())
                        .setTimestamp(status.timestamp());
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
                return;
            }
        }
    }

    /**
     * represents an AFK status message and timestamp.
     *
     * @param message the afk message to be sent.
     * @param timestamp the time the user when afk.
     */
    public record AfkStatus(String message, Instant timestamp) {}
}
