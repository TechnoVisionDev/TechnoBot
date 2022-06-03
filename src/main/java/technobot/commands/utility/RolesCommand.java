package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.EmbedColor;

/**
 * Command that displays all server roles.
 *
 * @author TechnoVision
 */
public class RolesCommand extends Command {

    public RolesCommand(TechnoBot bot) {
        super(bot);
        this.name = "roles";
        this.description = "Display all server roles and member counts.";
        this.category = Category.UTILITY;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        StringBuilder content = new StringBuilder();
        for (Role role : event.getGuild().getRoles()) {
            int amount = guild.getMembersWithRoles(role).size();
            if (role.getName().equals("@everyone")) amount = guild.getMemberCount();
            content.append(role.getAsMention()).append(" ").append(amount).append(" Members\n");
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle("All Roles")
                .setDescription(content);
        event.replyEmbeds(embed.build()).queue();
    }
}
