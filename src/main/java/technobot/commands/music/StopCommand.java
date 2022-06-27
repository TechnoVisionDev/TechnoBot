package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that clears the music queue and stops music
 *
 * @author TechnoVision
 */
public class StopCommand extends Command {

    public StopCommand(TechnoBot bot) {
        super(bot);
        this.name = "stop";
        this.description = "Stop the current song and clear the entire music queue.";
        this.category = Category.MUSIC;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        MusicHandler musicHandler = GuildData.get(event.getGuild()).musicHandler;
        if (musicHandler == null || musicHandler.getQueue().isEmpty()) {
            String text = get(s -> s.music.stop.failure);
            event.replyEmbeds(EmbedUtils.createError(text)).queue();
        } else {
            musicHandler.stop();
            String text = get(s -> s.music.stop.success);
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
        }
    }
}
