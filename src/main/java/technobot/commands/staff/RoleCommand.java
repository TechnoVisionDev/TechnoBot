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
import technobot.data.GuildData;
import technobot.handlers.ModerationHandler;
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
        this.botPermission = Permission.MANAGE_ROLES;
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
            event.replyEmbeds(EmbedUtils.createError("That user is not in your server!")).setEphemeral(true).queue();
            return;
        }
        if (role.isManaged() || role.isPublicRole()) {
            event.replyEmbeds(EmbedUtils.createError("I cannot give/remove bot or managed roles!")).setEphemeral(true).queue();
            return;
        }

        // Check target role position
        ModerationHandler moderationHandler = GuildData.get(event.getGuild()).moderationHandler;
        if (!moderationHandler.canTargetMember(member)) {
            event.replyEmbeds(EmbedUtils.createError("This member cannot be updated. I need my role moved higher than theirs.")).setEphemeral(true).queue();
            return;
        }

        String text = EmbedUtils.GREEN_TICK + " Changed roles for " + member.getEffectiveName() + ", ";
        switch (event.getSubcommandName()) {
            case "give" -> {
                text += "**+" + role.getName() + "**";
                event.getGuild().addRoleToMember(member, role).queue(null, fail -> {});
            }
            case "remove" -> {
                text += "**-" + role.getName() + "**";
                event.getGuild().removeRoleFromMember(member, role).queue(null, fail -> {});
            }
        }
        event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
