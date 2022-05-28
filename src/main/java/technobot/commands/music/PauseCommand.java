package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.cache.MusicPlayer;
import technobot.util.EmbedUtils;

/**
 * Command that pauses music player.
 *
 * @author TechnoVision
 */
public class PauseCommand extends Command {

    public PauseCommand(TechnoBot bot) {
        super(bot);
        this.name = "pause";
        this.description = "Pause the current playing track.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        MusicPlayer music = bot.musicHandler.getMusic(event, false);
        if (music == null) return;

        if (music.isPaused()) {
            String text = "The player is already paused!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
        } else {
            String text = ":pause_button: Paused the music player!";
            music.pause();
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
        }
    }
}
