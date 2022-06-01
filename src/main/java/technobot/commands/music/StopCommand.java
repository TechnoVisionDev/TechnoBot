package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.music.MusicPlayer;
import technobot.util.EmbedUtils;

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
        event.deferReply().queue();
        MusicPlayer musicHandler = GuildData.get(event.getGuild()).music;
        if (musicHandler == null || musicHandler.getQueue().isEmpty()) {
            String text = "The music player is already stopped!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
        } else {
            musicHandler.stop();
            String text = EmbedUtils.BLUE_TICK + " Stopped the music player!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
        }
    }
}
