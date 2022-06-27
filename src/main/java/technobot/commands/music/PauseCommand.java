package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

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
        MusicHandler music = bot.musicListener.getMusic(event, false);
        if (music == null) return;

        if (music.isPaused()) {
            String text = get(s -> s.music.pause.failure);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
        } else {
            String text = get(s -> s.music.pause.success);
            music.pause();
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
        }
    }
}
