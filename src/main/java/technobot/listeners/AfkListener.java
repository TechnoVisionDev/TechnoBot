package technobot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
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
    public static HashMap<User, Pair<String, Instant>> AFK_MESSAGES = new HashMap<>();

    /**
     * Sends AFK messages if necessary.
     * Also removes users from AFK list if they sent a message.
     * @param event executes whenever a message in sent in chat.
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // Check if AFK user has returned
        User messageAuthor = event.getMember().getUser();
        if (AFK_MESSAGES.containsKey(messageAuthor)) {
            AFK_MESSAGES.remove(messageAuthor);
            event.getMessage().addReaction("\uD83D\uDC4B").queue();
            return;
        }

        // Check mentions against AFK users
        for (User user : event.getMessage().getMentions().getUsers()) {
            Pair<String, Instant> message = AFK_MESSAGES.get(user);
            if (message != null) {
                // Mentioned user was AFK -- send message response
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setAuthor(user.getName()+" is currently AFK", null, user.getEffectiveAvatarUrl())
                        .setDescription(message.getLeft())
                        .setTimestamp(message.getRight());
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
                return;
            }
        }
    }
}
