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
                    event.replyEmbeds(EmbedUtils.createError("I cannot give out roles that have a higher position than me!")).setEphemeral(true).queue();
                    return;
                }
                if (configHandler.getConfig().getAutoRoles().size() >= 1 && !configHandler.isPremium()) {
                    event.replyEmbeds(EmbedUtils.createError("You can set multiple auto-roles with premium! For more info, use `/premium`.")).setEphemeral(true).queue();
                    return;
                }
                if (configHandler.getConfig().getAutoRoles().size() == MAX_AUTO_ROLES) {
                    event.replyEmbeds(EmbedUtils.createError("You have hit the maximum number of auto-roles for this guild!")).setEphemeral(true).queue();
                    return;
                }
                embed = EmbedUtils.createDefault(EmbedUtils.BLUE_TICK + " The <@&"+role.getId()+"> role will be given to all new members when they join the server.");
                configHandler.addAutoRole(role.getIdLong());
            }
            case "remove" -> {
                Role role = event.getOption("role").getAsRole();
                if (!configHandler.getConfig().getAutoRoles().contains(role.getIdLong())) {
                    event.replyEmbeds(EmbedUtils.createError("The <@&"+role.getId()+"> role is not set as an auto-role.")).setEphemeral(true).queue();
                    return;
                }
                embed = EmbedUtils.createDefault(EmbedUtils.BLUE_X + " The <@&"+role.getId()+"> role will no longer be given to new members when they join the server.");
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
                    embedBuilder.setDescription("Use `/auto-role add <role>` to set your first auto role!");
                } else {
                    int max = configHandler.isPremium() ? MAX_AUTO_ROLES : 1;
                    if (max == 1) {
                        embedBuilder.appendDescription("Add additional roles with `/premium`\n");
                    } else {
                        embedBuilder.appendDescription("There are "+roles.size()+" auto roles given to new members:\n");
                    }
                    int count = 0;
                    for (long roleID : roles) {
                        if (event.getGuild().getRoleById(roleID) != null) {
                            count++;
                            embedBuilder.appendDescription("\n**"+count+".** <@&"+roleID+">");
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