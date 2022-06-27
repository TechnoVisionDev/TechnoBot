package technobot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.listeners.ButtonListener;
import technobot.listeners.MusicListener;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static technobot.util.Localization.get;

/**
 * Command that displays an embed to showcases the music queue.
 *
 * @author TechnoVision
 */
public class QueueCommand extends Command {

    public static final int SONGS_PER_PAGE = 7;

    public QueueCommand(TechnoBot bot) {
        super(bot);
        this.name = "queue";
        this.description = "Display the current queue of songs.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        MusicHandler music = GuildData.get(event.getGuild()).musicHandler;

        // Check if queue is null or empty
        if (music == null || music.getQueue().isEmpty()) {
            String text = get(s -> s.music.queue.empty);
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }
        // Create embeds and send to channel
        List<MessageEmbed> embeds = buildQueueEmbeds(music.getQueue(), music.getQueue().size());
        ReplyCallbackAction action = event.replyEmbeds(embeds.get(0));
        if (embeds.size() > 1) {
            ButtonListener.sendPaginatedMenu(event.getUser().getId(), action, embeds);
        } else {
            action.queue();
        }
    }

    /**
     * Builds a beautiful paginated embed out of the music queue.
     *
     * @param queue     Iterator of the music queue.
     * @param queueSize Number of elements in queue.
     * @return MessageEmbed of the music queue.
     */
    private @NotNull List<MessageEmbed> buildQueueEmbeds(@NotNull LinkedList<AudioTrack> queue, int queueSize) {
        int count = 0;
        StringBuilder description = new StringBuilder();
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle(get(s -> s.music.queue.title));

        // Calculate total playlist length
        long queueTime = 0;
        for (AudioTrack track : queue) {
            queueTime += track.getInfo().length;
        }
        String total = MusicListener.formatTrackLength(queueTime);
        String song = queueSize < 3
                ? get(s -> s.music.queue.song)
                : get(s -> s.music.queue.songPlural);

        String footer = queueSize > 1 ? get(
                s -> s.music.queue.footer,
                queueSize - 1, song, total
        ) : "";

        for (AudioTrack track : queue) {
            AudioTrackInfo trackInfo = track.getInfo();
            if (count == 0) { //Current playing track
                description.append(get(s -> s.music.queue.nowPlaying) + "\n");
                description.append(String.format("[%s](%s) | ", trackInfo.title, trackInfo.uri));
                description.append(String.format("`%s`\n\n", MusicListener.formatTrackLength(trackInfo.length)));
                count++;
                continue;
            } else if (count == 1) { //Header for queue
                description.append(get(s -> s.music.queue.upNext) + "\n");
            }
            //Rest of the queue
            description.append(String.format("`%s.` [%s](%s) | ", count, trackInfo.title, trackInfo.uri));
            description.append(String.format("`%s`\n\n", MusicListener.formatTrackLength(trackInfo.length)));
            count++;
            if (count % SONGS_PER_PAGE == 0) {
                // Add embed as new page
                description.append(footer);
                embed.setDescription(description.toString());
                embeds.add(embed.build());
                description = new StringBuilder();
            }
        }
        if (count % SONGS_PER_PAGE != 0) {
            description.append(footer);
            embed.setDescription(description.toString());
            embeds.add(embed.build());
        }
        return embeds;
    }
}
