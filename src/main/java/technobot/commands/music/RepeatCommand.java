package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;
import technobot.util.localization.Repeat;

import static technobot.util.Localization.get;

/**
 * Command that toggles repeat mode for music queue.
 *
 * @author TechnoVision
 */
public class RepeatCommand extends Command {

    public RepeatCommand(TechnoBot bot) {
        super(bot);
        this.name = "repeat";
        this.description = "Toggles the repeat mode.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        MusicHandler music = bot.musicListener.getMusic(event, false);
        if (music == null) return;

        music.loop();

        Repeat repeatText = get(s -> s.music.repeat);
        String text = music.isLoop() ? repeatText.enabled : repeatText.disabled;

        event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
