package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

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
        event.deferReply().queue();
        MusicHandler music = bot.musicListener.getMusic(event, false);
        if (music == null) return;

        music.skipTrack();
        ReplyCallbackAction action = event.reply(
                get(s -> s.music.skip.skipping) + ""
        );
        if (music.getQueue().size() == 1) {
            action = action.addEmbeds(EmbedUtils.createDefault(
                    get(s -> s.music.skip.queueEmpty)
            ));
        }
        action.queue();
    }
}
