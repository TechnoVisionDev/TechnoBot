package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

/**
 * Creates button links to the Patreon to buy premium.
 *
 * @author TechnoVision
 */
public class PremiumCommand extends Command {

    public PremiumCommand(TechnoBot bot) {
        super(bot);
        this.name = "premium";
        this.description = "Provides information on TechnoBot Premium.";
        this.category = Category.UTILITY;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle("TechnoBot Premium")
                .setDescription("Premium features are coming soon! But in the meantime, feel free to donate [HERE](https://www.patreon.com/TechnoVision)! ")
                .appendDescription("TechnoBot is developed by one guy who pays for server costs out of pocket! I appreciate any support :heart:")
                .addField("Premium Features", "⦁ Add up to 10 auto-roles\n⦁ More coming soon...", true);
        event.replyEmbeds(embed.build()).addActionRow(Button.link("https://www.patreon.com/TechnoVision", "Buy Premium")).queue();
    }
}
