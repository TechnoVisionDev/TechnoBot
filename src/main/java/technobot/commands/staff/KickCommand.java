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
 * Command that kicks a user from the guild.
 *
 * @author TechnoVision
 */
public class KickCommand extends Command {

    public KickCommand(TechnoBot bot) {
        super(bot);
        this.name = "kick";
        this.description = "Kicks a user from your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to kick", true));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the kick"));
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
        } else if (target.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("Do you seriously expect me to kick myself?")).queue();
            return;
        }
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Unspecified";

        // Kick user from guild
        user.openPrivateChannel().queue(privateChannel -> {
            // Private message user with reason for kick
            EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.ERROR.color)
                        .setTitle(EmbedUtils.RED_X + " You were kicked!")
                        .addField("Server", event.getGuild().getName(), false)
                        .addField("Reason", reason, false)
                        .setTimestamp(new Date().toInstant());
            privateChannel.sendMessageEmbeds(embed.build()).queue(
                    message -> target.kick(reason).queue(),
                    failure -> target.kick(reason).queue());
            }
        );

        // Send confirmation message
        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " has been kicked", null, user.getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason)
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
