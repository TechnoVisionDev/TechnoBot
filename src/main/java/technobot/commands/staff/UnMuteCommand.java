package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.util.CommandUtils;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that removes a muted role from a user in the guild.
 *
 * @author TechnoVision
 */
public class UnMuteCommand extends Command {

    public UnMuteCommand(TechnoBot bot) {
        super(bot);
        this.name = "unmute";
        this.description = "Unmutes a user in your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to unmute", true));
        this.permission = Permission.MANAGE_ROLES;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("That user is not in this server!")).queue();
            return;
        } else if (target.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("Do you seriously expect me to unmute myself?")).queue();
            return;
        }

        // Check that muted role is valid and user has it
        Role muteRole = GuildData.get(event.getGuild()).moderationHandler.getMuteRole();
        if (muteRole == null) {
            String text = "This server does not have a mute role, use `/mute-role <role>` to set one or `/mute-role create [name]` to create one.";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }
        if (!target.getRoles().contains(muteRole)) {
            String text = "That user is not muted!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            return;
        }

        // Check that bot has necessary permissions
        Role botRole = event.getGuild().getBotRole();
        int botPos = botRole.getPosition();
        if (!CommandUtils.hasPermission(botRole, this.permission) || target.isOwner() || muteRole.getPosition() >= botPos) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("I couldn't unmute that user. Please check my permissions and role position.")).queue();
            return;
        }

        // Check if bot has a higher role than user
        for (Role role : target.getRoles()) {
            if (role.getPosition() >= botPos) {
                event.getHook().sendMessageEmbeds(EmbedUtils.createError("I couldn't unmute that user. Please check my permissions and role position.")).queue();
                return;
            }
        }

        // Remove muted role to user
        event.getGuild().removeRoleFromMember(target, muteRole).queue();

        // Send confirmation message
        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " is now unmuted", null, user.getEffectiveAvatarUrl())
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
