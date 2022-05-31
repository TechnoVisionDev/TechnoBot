package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.EmbedUtils;

/**
 * Command that puts a channel in slowmode with specified time.
 *
 * @author TechnoVision
 */
public class SlowmodeCommand extends Command {

    private static final PeriodFormatter formatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix("d ")
            .appendHours().appendSuffix("h ")
            .appendMinutes().appendSuffix("m ")
            .appendSeconds().appendSuffix("s")
            .toFormatter();

    public SlowmodeCommand(TechnoBot bot) {
        super(bot);
        this.name = "slowmode";
        this.description = "Sets slowmode time for a channel.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.STRING, "time", "The time to set for slowmode"));
        this.permission = Permission.MANAGE_CHANNEL;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        OptionMapping timeOption = event.getOption("time");
        if (timeOption != null) {
            // Check that timer does not exceed 6 hours
            int time = formatter.parsePeriod(timeOption.getAsString()).getSeconds();
            if (time > TextChannel.MAX_SLOWMODE) {
                event.getHook().sendMessageEmbeds(EmbedUtils.createError("Time should be less than or equal to **6 hours**.")).queue();
                return;
            }
            // Set slowmode timer
            event.getTextChannel().getManager().setSlowmode(time).queue();
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(":stopwatch: This channel's slowmode has been set to **"+formatTime(time)+"**.")).queue();
        } else {
            // Display current slowmode timer
            int totalSecs = event.getTextChannel().getSlowmode();
            String timeString = formatTime(totalSecs);
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(":stopwatch: This channel's slowmode is **"+timeString+"**.")).queue();
        }
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        String time = "";
        if (hours > 0) {
            time += hours + " hour";
            if (hours > 1) time += "s";
            if (minutes > 0) time += ", ";
        }
        if (minutes > 0) {
            time += minutes + " minute";
            if (minutes > 1) time += "s";
            if (seconds > 0) time += ", ";
        }
        if (seconds > 0) {
            time += seconds + " second";
            if (seconds > 1) time += "s";
        }
        return time;
    }
}
