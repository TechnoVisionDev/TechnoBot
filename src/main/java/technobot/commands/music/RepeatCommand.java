package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.music.MusicPlayer;
import technobot.util.EmbedUtils;

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
        MusicPlayer music = bot.musicHandler.getMusic(event, false);
        if (music == null) return;

        music.loop();
        String text;
        if (music.isLoop()) {
            text = ":repeat_one: Loop Enabled!";
        } else {
            text = ":repeat_one: Loop Disabled!";
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
