package technobot.commands.utility;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Creates button links to invite bot and join the support server.
 *
 * @author TechnoVision
 */
public class InviteCommand extends Command {

    public InviteCommand(TechnoBot bot) {
        super(bot);
        this.name = "invite";
        this.description = "Invite TechnoBot to your servers.";
        this.category = Category.UTILITY;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Button botInvite = Button.link("https://discord.com/oauth2/authorize?client_id=979590525428580363&permissions=2088234238&scope=applications.commands%20bot", "Invite TechnoBot");
        Button supportServerInvite = Button.link("https://discord.gg/2TKJqfUQas", "Support Server");
        Button dashboardLink = Button.link("https://technobot.app", "Dashboard");
        event.replyEmbeds(EmbedUtils.createDefault(
                        get(s -> s.utility.invite)
                ))
                .addActionRow(botInvite, supportServerInvite, dashboardLink).queue();
    }
}
