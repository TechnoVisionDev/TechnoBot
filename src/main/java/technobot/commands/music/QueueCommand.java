package technobot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.MusicPlayer;
import technobot.handlers.MusicHandler;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

import java.util.ListIterator;

/**
 * Command that displays an embed to showcases the music queue.
 *
 * @author TechnoVision
 */
public class QueueCommand extends Command {

    public QueueCommand(TechnoBot bot) {
        super(bot);
        this.name = "queue";
        this.description = "Display the current queue of songs.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        MusicPlayer music = GuildData.get(event.getGuild()).music;

        // Check if queue is null or empty
        if (music == null || music.getQueue().isEmpty()) {
            String text = ":sound: There are no songs in the queue!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }
        // Create embed and send to channel
        MessageEmbed embed = buildQueueEmbed(music.getQueue().listIterator(), music.getQueue().size());
        event.getHook().sendMessageEmbeds(embed).queue();
    }

    /**
     * Builds a beautiful embed out of a music queue.
     *
     * @param queue     Iterator of the music queue.
     * @param queueSize Number of elements in queue.
     * @return MessageEmbed of the music queue.
     */
    private @NotNull MessageEmbed buildQueueEmbed(@NotNull ListIterator<AudioTrack> queue, int queueSize) {
        int count = 0;
        long queueTime = 0;
        StringBuilder description = new StringBuilder();

        while (queue.hasNext()) {
            AudioTrack track = queue.next();
            AudioTrackInfo trackInfo = track.getInfo();
            queueTime += trackInfo.length;

            if (count == 0) { //Current playing track
                description.append("__Now Playing:__\n");
                description.append(String.format("[%s](%s) | ", trackInfo.title, trackInfo.uri));
                description.append(String.format("`%s`\n\n", MusicHandler.formatTrackLength(trackInfo.length)));
                count++;
                continue;
            } else if (count == 1) { //Header for queue
                description.append("__Up Next:__\n");
            }
            //Rest of the queue
            description.append(String.format("`%s.` [%s](%s) | ", count, trackInfo.title, trackInfo.uri));
            description.append(String.format("`%s`\n\n", MusicHandler.formatTrackLength(trackInfo.length)));

            //Footer information
            if (count == 10 || count + 1 == queueSize) {
                String total = MusicHandler.formatTrackLength(queueTime);
                String song = "Song";
                if (queueSize >= 3) { // Make "Song" plural if necessary
                    song += "s";
                }
                description.append(String.format("**%s %s in Queue | %s Total Length**", queueSize - 1, song, total));
                break;
            }
            count++;
        }

        //Add to embed and send message
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle("Music Queue :musical_note:")
                .setDescription(description.toString());
        return embed.build();
    }
}
