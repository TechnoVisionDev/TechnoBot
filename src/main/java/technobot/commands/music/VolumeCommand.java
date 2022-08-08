package technobot.commands.music;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.handlers.MusicHandler;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

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
        int volume = event.getOption("amount").getAsInt();
        MusicHandler music = bot.musicListener.getMusic(event, true);
        if (music == null) return;
        try {
            if (volume < 0 || volume > 100) {
                throw new NumberFormatException();
            }
            music.setVolume(volume);
            String text = get(s -> s.music.volume.success, volume);
            event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        } catch (@NotNull NumberFormatException | ArrayIndexOutOfBoundsException ignored) {}

        String text = get(s -> s.music.volume.failure);
        event.replyEmbeds(EmbedUtils.createError(text)).queue();
    }
}
