package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;

import static technobot.util.Localization.get;

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
                .setTitle(get(s -> s.utility.premium.title))
                .setDescription(get(s -> s.utility.premium.description1))
                .appendDescription(get(s -> s.utility.premium.description2))
                .addField(get(s -> s.utility.premium.features), get(s -> s.utility.premium.list), true);
        event.replyEmbeds(embed.build()).addActionRow(Button.link("https://www.patreon.com/TechnoVision", get(s -> s.utility.premium.button) + "")).queue();
    }
}
