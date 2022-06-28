package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;

import static technobot.util.Localization.get;

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
        StringBuilder content = new StringBuilder();
        for (Role role : event.getGuild().getRoles()) {
            if (!role.isManaged()) {
                content.append(role.getAsMention());
                content.append("\n");
            }
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle(get(s -> s.utility.roles))
                .setDescription(content);
        event.replyEmbeds(embed.build()).queue();
    }
}
