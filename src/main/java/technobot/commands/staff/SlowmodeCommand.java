package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import static technobot.util.Localization.get;

/**
 * Command that puts a channel in slowmode with specified time.
 *
 * @author TechnoVision
 */
public class SlowmodeCommand extends Command {

    public SlowmodeCommand(TechnoBot bot) {
        super(bot);
        this.name = "slowmode";
        this.description = "Sets slowmode time for a channel.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.STRING, "time", "The time to set for slowmode"));
        this.permission = Permission.MANAGE_CHANNEL;
        this.botPermission = Permission.MANAGE_CHANNEL;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Prevent slowmode in threads
        ChannelType type = event.getChannelType();
        if (type == ChannelType.GUILD_PUBLIC_THREAD || type == ChannelType.GUILD_NEWS_THREAD || type == ChannelType.GUILD_PRIVATE_THREAD) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.slowMode.failureThread)
            )).setEphemeral(true).queue();
            return;
        }

        OptionMapping timeOption = event.getOption("time");
        if (timeOption != null) {
            // Retrieve time in seconds from input
            String timeString = timeOption.getAsString();
            int time;
            try {
                try {
                    Duration duration = Duration.parse("PT" + timeString.replaceAll(" ", ""));
                    time = (int) duration.toSeconds();
                    if (time <= 0) throw new NumberFormatException();
                } catch (DateTimeParseException e) {
                    time = Integer.parseInt(timeString);
                    if (time <= 0) throw new NumberFormatException();
                }
            } catch (NumberFormatException e2) {
                // Disable slowmode
                event.getTextChannel().getManager().setSlowmode(0).queue();
                event.replyEmbeds(EmbedUtils.createDefault(get(s -> s.staff.slowMode.disable)
                )).queue();
                return;
            }
            // Set slowmode timer
            if (time > TextChannel.MAX_SLOWMODE) {
                event.replyEmbeds(EmbedUtils.createError(
                        get(s -> s.staff.slowMode.tooHigh)
                )).setEphemeral(true).queue();
                return;
            }
            event.getTextChannel().getManager().setSlowmode(time).queue();
            event.replyEmbeds(EmbedUtils.createDefault(
                    get(s -> s.staff.slowMode.set, formatTime(time))
            )).queue();
        } else {
            // Display current slowmode timer
            int totalSecs = event.getTextChannel().getSlowmode();
            String timeString = formatTime(totalSecs);
            event.replyEmbeds(EmbedUtils.createDefault(
                    get(s -> s.staff.slowMode.display, timeString)
            )).queue();
        }
    }

    /**
     * Formats seconds into a string 'x hours, x minutes, x seconds'.
     *
     * @param totalSeconds the number of seconds to convert to string.
     * @return a formatted string.
     */
    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        String time = "";
        if (hours > 0) {
            time += get(s -> s.staff.slowMode.format.hours, hours);

            if (minutes > 0) time += ", ";
        }
        if (minutes > 0) {
            time += get(s -> s.staff.slowMode.format.minutes, minutes);

            if (seconds > 0) time += ", ";
        }
        if (seconds > 0) {
            time += get(s -> s.staff.slowMode.format.seconds, seconds);
        }
        return time;
    }
}
