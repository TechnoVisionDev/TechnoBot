package technobot.commands.utility;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;
import technobot.util.localization.Invite;

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
        Invite inviteText = get(s -> s.utility.invite);
        Button botInvite = Button.link("https://discord.com/oauth2/authorize?client_id=979590525428580363&permissions=2088234238&scope=applications.commands%20bot", inviteText.inviteButton);
        Button supportServerInvite = Button.link("https://discord.gg/2TKJqfUQas", inviteText.serverButton);
        Button dashboardLink = Button.link("https://technobot.app", inviteText.dashboardButton);
        event.replyEmbeds(EmbedUtils.createDefault(inviteText.message))
                .addActionRow(botInvite, supportServerInvite, dashboardLink)
                .queue();
    }
}
