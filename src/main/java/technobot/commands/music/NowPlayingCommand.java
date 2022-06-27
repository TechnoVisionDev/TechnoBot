package technobot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.listeners.MusicListener;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that displays the currently playing song.
 *
 * @author TechnoVision
 */
public class NowPlayingCommand extends Command {

    public NowPlayingCommand(TechnoBot bot) {
        super(bot);
        this.name = "np";
        this.description = "Display the current playing song.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        MusicHandler music = GuildData.get(event.getGuild()).musicHandler;

        // Verify the Music Manager isn't null.
        if (music == null) {
            String text = get(s -> s.music.nowPlaying.notPlaying);
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }

        // Get currently playing track
        AudioTrack nowPlaying = music.getQueue().size() > 0 ? music.getQueue().getFirst() : null;
        if (nowPlaying == null) {
            String text = get(s -> s.music.nowPlaying.notPlaying);
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }

        // Create progress bar
        int barLength = 17;
        String[] progressBarArray = new String[barLength];
        for (int i = 0; i < barLength; i++) {
            progressBarArray[i] = "⎯";
        }
        progressBarArray[(int) Math.floor((float) nowPlaying.getPosition() / nowPlaying.getDuration() * barLength)] = "◉";
        String progressBar = String.join("", progressBarArray);

        long pos = nowPlaying.getPosition();
        String trackStart = MusicListener.formatTrackLength(pos);
        String trackEnd = MusicListener.formatTrackLength(nowPlaying.getInfo().length);

        // Create and send embed message
        event.replyEmbeds(
                new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setTitle(get(s -> s.music.nowPlaying.title))
                        .setDescription("[" + nowPlaying.getInfo().title + "](" + nowPlaying.getInfo().uri + ")")
                        .addField(get(s -> s.music.nowPlaying.positionTitle), progressBar, false)
                        .addField(
                                get(s -> s.music.nowPlaying.progressTitle),
                                get(s -> s.music.nowPlaying.progress, trackStart, trackEnd),
                                false
                        )
                        .build()
        ).queue();
    }
}
