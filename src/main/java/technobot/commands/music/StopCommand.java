package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

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
        if (musicHandler == null) {
            String text = "The music player is already stopped!";
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
        } else {
            musicHandler.disconnect();
            event.getGuild().getAudioManager().closeAudioConnection();
            String text = ":stop_button: Stopped the music player.";
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
        }
    }
}
