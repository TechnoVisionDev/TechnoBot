package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

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
            String text = get(s -> s.music.resume.success);
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
        } else {
            String text = get(s -> s.music.resume.failure);
            event.replyEmbeds(EmbedUtils.createError(text)).queue();
        }
    }
}
