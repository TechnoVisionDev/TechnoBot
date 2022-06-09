package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.CommandUtils;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that gives or removes a role from user.
 *
 * @author TechnoVision
 */
public class RoleCommand extends Command {

    public RoleCommand(TechnoBot bot) {
        super(bot);
        this.name = "role";
        this.description = "Manages roles for a user.";
        this.category = Category.STAFF;
        this.permission = Permission.MANAGE_ROLES;
        this.subCommands.add(new SubcommandData("give", "Gives a role to a user.")
                .addOption(OptionType.USER, "user", "The user to give the role to", true)
                .addOption(OptionType.ROLE, "role", "The role to give", true));
        this.subCommands.add(new SubcommandData("remove", "Removes a role from a user.")
                .addOption(OptionType.USER, "user", "The user to remove role from", true)
                .addOption(OptionType.ROLE, "role", "The role to remove", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getOption("user").getAsMember();
        Role role = event.getOption("role").getAsRole();
        if (member == null) {
            event.replyEmbeds(EmbedUtils.createError("That user is not in your server!")).queue();
            return;
        }

        // Check that bot has necessary permissions
        Role botRole = event.getGuild().getBotRole();
        if (!CommandUtils.hasPermission(botRole, this.permission) || role.isManaged()) {
            event.replyEmbeds(EmbedUtils.createError("I couldn't change the roles for that user. Please check my permissions and role position.")).queue();
            return;
        }

        // Check if bot has a higher role than user
        int botPos = event.getGuild().getBotRole().getPosition();
        for (Role r : member.getRoles()) {
            if (r.getPosition() >= botPos) {
                event.replyEmbeds(EmbedUtils.createError("I couldn't change the roles for that user. Please check my permissions and role position.")).queue();
                return;
            }
        }

        String text = EmbedUtils.GREEN_TICK + " Changed roles for " + member.getEffectiveName() + ", ";
        switch (event.getSubcommandName()) {
            case "give" -> {
                text += "**+" + role.getName() + "**";
                event.getGuild().addRoleToMember(member, role).queue();
            }
            case "remove" -> {
                text += "**-" + role.getName() + "**";
                event.getGuild().removeRoleFromMember(member, role).queue();
            }
        }
        event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
