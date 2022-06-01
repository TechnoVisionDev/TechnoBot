package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

import java.awt.*;
import java.util.Date;

/**
 * Command that bans a user from the guild.
 *
 * @author TechnoVision
 */
public class BanCommand extends Command {

    public BanCommand(TechnoBot bot) {
        super(bot);
        this.name = "ban";
        this.description = "Bans a user from your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to ban", true));
        this.args.add(new OptionData(OptionType.INTEGER, "days", "Time duration for the ban in days").setMinValue(1).setMaxValue(365));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the ban"));
        this.permission = Permission.BAN_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        if (user.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("Did you seriously expect me to ban myself?")).queue();
            return;
        }
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Unspecified";

        // Ban user from guild
        user.openPrivateChannel().queue(privateChannel -> {
            // Private message user with reason for Ban
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.ERROR.color)
                    .setTitle(EmbedUtils.RED_X + " You were banned!")
                    .addField("Server", event.getGuild().getName(), false)
                    .addField("Duration", "Forever", false)
                    .addField("Reason", reason, false)
                    .setTimestamp(new Date().toInstant());
            privateChannel.sendMessageEmbeds(embed.build()).queue(
                    message -> event.getGuild().ban(user, 7, reason).queue(),
                    failure -> event.getGuild().ban(user, 7, reason).queue());
            }
        );

        // Start unban timer if temp ban specified
        OptionMapping timeOption = event.getOption("time");
        if (timeOption != null) {
            // TODO: Add banned user to database and start scheduled executor service to unban
        }

        // Send confirmation message
        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " has been banned", null, user.getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason)
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
