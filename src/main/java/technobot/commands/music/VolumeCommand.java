package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.cache.MusicPlayer;
import technobot.util.EmbedUtils;

/**
 * Command that changes volume of the music player.
 *
 * @author TechnoVision
 */
public class VolumeCommand extends Command {

    public VolumeCommand(TechnoBot bot) {
        super(bot);
        this.name = "volume";
        this.description = "Changes the volume of the music.";
        this.category = Category.MUSIC;
        this.args.add(new OptionData(OptionType.INTEGER, "amount", "Enter value between 0-100 to set", true)
                .setMinValue(0)
                .setMaxValue(100));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int volume = event.getOption("amount").getAsInt();

        MusicPlayer music = bot.musicHandler.getMusic(event, false);
        if (music == null) return;
        try {
            if (volume < 0 || volume > 100) {
                throw new NumberFormatException();
            }
            music.setVolume(volume);
            String text = String.format(":loud_sound: Set volume to %s%%", volume);
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        } catch (@NotNull NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}

        String text = "You must specify a volume between 0-100";
        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
    }
}
