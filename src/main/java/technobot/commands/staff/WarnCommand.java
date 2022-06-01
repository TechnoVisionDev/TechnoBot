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
import technobot.data.GuildData;
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

import java.awt.*;
import java.util.Date;

/**
 * Command that adds a warning to user's account.
 *
 * @author TechnoVision
 */
public class WarnCommand extends Command {

    public WarnCommand(TechnoBot bot) {
        super(bot);
        this.name = "warn";
        this.description = "Adds a warning to a user's profile.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to warn", true));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the warning"));
        this.permission = Permission.KICK_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member target = event.getGuild().getMemberById(user.getIdLong());
        if (target == null) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("That user is not in this server!")).queue();
            return;
        }
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Unspecified";

        // Add warning for user
        GuildData data = GuildData.get(event.getGuild());
        data.moderationhandler.addWarning(reason, target.getIdLong(), event.getUser().getIdLong());

        // Private message user with reason for warn
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(Color.yellow)
                    .setTitle(":warning: You were warned!")
                    .setDescription(reason)
                    .setFooter(event.getGuild().getName())
                    .setTimestamp(new Date().toInstant());
            privateChannel.sendMessageEmbeds(embed.build()).queue();
        });

        // Send confirmation message
        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " has been warned", null, user.getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason)
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
