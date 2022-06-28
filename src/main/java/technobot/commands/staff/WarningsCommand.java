package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.moderation.Warning;
import technobot.util.embeds.EmbedColor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static technobot.util.Localization.get;

/**
 * Command that shows the warnings for a specified user.
 *
 * @author TechnoVision
 */
public class WarningsCommand extends Command {

    public WarningsCommand(TechnoBot bot) {
        super(bot);
        this.name = "warnings";
        this.description = "Display a list of warnings for you or another user.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to get warnings for"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData data = GuildData.get(event.getGuild());
        User target = event.getOption("user", event.getUser(), OptionMapping::getAsUser);

        // Create embed template
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(EmbedColor.DEFAULT.color);

        // Check if user has no warnings
        List<Warning> warnings = data.moderationHandler.getWarnings(target.getId());
        if (warnings == null || warnings.isEmpty()) {
            embed.setAuthor(get(s -> s.staff.warnings.noWarnings, target.getAsTag()), null, target.getEffectiveAvatarUrl());
            event.replyEmbeds(embed.build()).queue();
            return;
        }

        // Display warnings in an embed
        int lastWeek = 0;
        int lastDay = 0;
        StringBuilder content = new StringBuilder();
        int counter = 0;
        for (int i = warnings.size() - 1; i >= 0; i--) {
            Warning w = warnings.get(i);
            String time = TimeFormat.RELATIVE.format(w.getTimestamp());
            if (counter < 10) {
                content.append("`[").append(w.getId()).append("]` **").append(w.getReason()).append("** • ").append(time).append("\n");
            }
            if (w.getTimestamp() >= System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)) {
                lastDay++;
            }
            if (w.getTimestamp() >= System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)) {
                lastWeek++;
            }
            counter++;
        }
        embed.setAuthor(get(s -> s.staff.warnings.title, target.getAsTag()), null, target.getEffectiveAvatarUrl());
        embed.addField(
                get(s -> s.staff.warnings.lastDay),
                get(s -> s.staff.warnings.message, lastDay), true
        );
        embed.addField(
                get(s -> s.staff.warnings.lastWeek),
                get(s -> s.staff.warnings.message, lastWeek), true
        );
        embed.addField(
                get(s -> s.staff.warnings.total),
                get(s -> s.staff.warnings.message, warnings.size()), true
        );
        embed.addField(
                get(s -> s.staff.warnings.last10),
                content.toString(), false
        );
        event.replyEmbeds(embed.build()).queue();
    }
}
