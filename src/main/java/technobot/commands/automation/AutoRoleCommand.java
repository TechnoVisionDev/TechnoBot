package technobot.commands.automation;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.ConfigHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.Set;

import static technobot.util.Localization.get;

/**
 * Command that sets roles to be given on user join.
 *
 * @author TechnoVision
 */
public class AutoRoleCommand extends Command {

    public static final int MAX_AUTO_ROLES = 10;

    public AutoRoleCommand(TechnoBot bot) {
        super(bot);
        this.name = "auto-role";
        this.description = "Set roles to be given to all new members.";
        this.category = Category.AUTOMATION;
        this.permission = Permission.MANAGE_SERVER;
        this.botPermission = Permission.MANAGE_ROLES;
        this.subCommands.add(new SubcommandData("add", "Add a role to be given to new members on joining.")
                .addOption(OptionType.ROLE, "role", "The role to be given", true));
        this.subCommands.add(new SubcommandData("remove", "Remove a role from the auto-role list.")
                .addOption(OptionType.ROLE, "role", "The role to remove", true));
        this.subCommands.add(new SubcommandData("list", "List the current auto-roles for this server."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        ConfigHandler configHandler = GuildData.get(event.getGuild()).configHandler;
        MessageEmbed embed = null;
        switch (event.getSubcommandName()) {
            case "add" -> {
                Role role = event.getOption("role").getAsRole();
                if (role.isManaged() || role.isPublicRole() || role.getPosition() >= event.getGuild().getBotRole().getPosition()) {
                    event.replyEmbeds(EmbedUtils.createError(
                            get(s -> s.automation.autoRole.add.higherLevel)
                    )).setEphemeral(true).queue();
                    return;
                }
                if (configHandler.getConfig().getAutoRoles().size() >= 1 && !configHandler.isPremium()) {
                    event.replyEmbeds(EmbedUtils.createError(
                            get(s -> s.automation.autoRole.add.premium)
                    )).setEphemeral(true).queue();
                    return;
                }
                if (configHandler.getConfig().getAutoRoles().size() == MAX_AUTO_ROLES) {
                    event.replyEmbeds(EmbedUtils.createError(
                            get(s -> s.automation.autoRole.add.maxRolesReached)
                    )).setEphemeral(true).queue();
                    return;
                }
                embed = EmbedUtils.createDefault(get(s -> s.automation.autoRole.add.roleAdded, role.getId()));
                configHandler.addAutoRole(role.getIdLong());
            }
            case "remove" -> {
                Role role = event.getOption("role").getAsRole();
                if (!configHandler.getConfig().getAutoRoles().contains(role.getIdLong())) {
                    event.replyEmbeds(EmbedUtils.createError(
                            get(s -> s.automation.autoRole.remove.failure, role.getId())
                    )).setEphemeral(true).queue();
                    return;
                }
                embed = EmbedUtils.createDefault(get(s -> s.automation.autoRole.remove.success, role.getId()));
                if (!configHandler.isPremium()) {
                    configHandler.clearAutoRoles();
                } else {
                    configHandler.removeAutoRole(role.getIdLong());
                }
            }
            case "list" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Auto Roles").setColor(EmbedColor.DEFAULT.color);
                Set<Long> roles = configHandler.getConfig().getAutoRoles();
                if (roles == null || roles.isEmpty()) {
                    embedBuilder.setDescription(get(s -> s.automation.autoRole.list.noAutoRoles));
                } else {
                    int max = configHandler.isPremium() ? MAX_AUTO_ROLES : 1;
                    if (max == 1) {
                        embedBuilder.appendDescription(get(s -> s.automation.autoRole.list.premium) + "\n");
                    } else {
                        embedBuilder.appendDescription(get(s -> s.automation.autoRole.list.roleCount, roles.size()) + "\n");
                    }
                    int count = 0;
                    for (long roleID : roles) {
                        if (event.getGuild().getRoleById(roleID) != null) {
                            count++;
                            embedBuilder.appendDescription("\n" + get(s -> s.automation.autoRole.list.role, count, roleID));
                            if (count == max) break;
                        }
                    }
                }
                embed = embedBuilder.build();
            }
        }
        event.replyEmbeds(embed).queue();
    }
}