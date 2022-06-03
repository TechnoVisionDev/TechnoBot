package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.EmbedUtils;

/**
 * Command that un-pauses music player.
 *
 * @author TechnoVision
 */
public class ResumeCommand extends Command {

    public ResumeCommand(TechnoBot bot) {
        super(bot);
        this.name = "resume";
        this.description = "Resumes the current paused track.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        MusicHandler music = bot.musicListener.getMusic(event, false);
        if (music == null) return;

        if (music.isPaused()) {
            music.unpause();
            String text = ":play_pause: Resuming the music player!";
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
        } else {
            String text = "The player is not paused!";
            event.replyEmbeds(EmbedUtils.createError(text)).queue();
        }
    }
}
