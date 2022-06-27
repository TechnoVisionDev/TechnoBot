package technobot.commands.music;

import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

import java.net.MalformedURLException;
import java.net.URL;

import static technobot.util.Localization.get;

/**
 * Command that searches and plays music.
 *
 * @author TechnoVision
 */
public class PlayCommand extends Command {

    public PlayCommand(TechnoBot bot) {
        super(bot);
        this.name = "play";
        this.description = "Add a song to the queue and play it.";
        this.category = Category.MUSIC;
        this.args.add(new OptionData(OptionType.STRING, "song", "Song to search for or a link to the song", true));
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String song = event.getOption("song").getAsString();

        MusicHandler music = bot.musicListener.getMusic(event, true);
        if (music == null) return;

        // Check if member is in the right voice channel
        AudioChannel channel = event.getMember().getVoiceState().getChannel();
        if (music.getPlayChannel() != channel) {
            String text = get(s -> s.music.play.differentChannel);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Cannot have more than 100 songs in the queue
        if (music.getQueue().size() >= 100) {
            String text = get(s -> s.music.play.tooManySongs);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Find working URL
        try {
            String url;
            try {
                // Check for real URL
                url = new URL(song).toString();
            } catch (MalformedURLException e) {
                // Else search YouTube using args
                url = "ytsearch:" + song;
                music.setLogChannel(event.getTextChannel());
                bot.musicListener.addTrack(event, url);
                return;
            }
            // Search YouTube if using a soundcloud link
            if (url.contains("https://soundcloud.com/")) {
                String[] contents = url.split("/");
                url = "ytsearch:" + contents[3] + "/" + contents[4];
            }
            // Otherwise add real URL to queue
            music.setLogChannel(event.getTextChannel());
            bot.musicListener.addTrack(event, url);
        } catch (IndexOutOfBoundsException e) {
            String text = get(s -> s.music.play.specifySong);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
        }
    }
}
