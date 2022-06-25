package technobot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that displays the currently playing song.
 *
 * @author TechnoVision
 */
public class NowPlayingCommand extends Command {

    public NowPlayingCommand(TechnoBot bot) {
        super(bot);
        this.name = "playing";
        this.description = "Check what song is currently playing.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Verify the Music Manager isn't null.
        MusicHandler music = GuildData.get(event.getGuild()).musicHandler;
        if (music == null) {
            String text = ":sound: Not currently playing any music!";
            event.replyEmbeds(EmbedUtils.createDefault(text)).setEphemeral(true).queue();
            return;
        }

        // Get currently playing track
        AudioTrack nowPlaying = music.getQueue().size() > 0 ? music.getQueue().getFirst() : null;
        if (nowPlaying == null) {
            String text = ":sound: Not currently playing any music!";
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }
        event.replyEmbeds(MusicHandler.displayTrack(nowPlaying, music)).queue();
    }
}
