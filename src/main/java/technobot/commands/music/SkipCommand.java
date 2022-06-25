package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that skips the current song.
 *
 * @author TechnoVision
 */
public class SkipCommand extends Command {

    public SkipCommand(TechnoBot bot) {
        super(bot);
        this.name = "skip";
        this.description = "Skip the current song.";
        this.category = Category.MUSIC;
    }

    public void execute(SlashCommandInteractionEvent event) {
        MusicHandler music = bot.musicListener.getMusic(event, false);
        if (music == null) return;

        music.skipTrack();
        ReplyCallbackAction action = event.reply(":fast_forward: Skipping...");
        if (music.getQueue().size() == 1) {
            action = action.addEmbeds(EmbedUtils.createDefault(":sound: The music queue is now empty!"));
        }
        action.queue();
    }
}
