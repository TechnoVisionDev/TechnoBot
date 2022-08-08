package technobot.commands.economy;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that displays details about a shop item
 *
 * @author TechnoVision
 */
public class InspectCommand extends Command {

    public InspectCommand(TechnoBot bot) {
        super(bot);
        this.name = "inspect";
        this.description = "Display details about a shop item.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.STRING, "item", "The name of the item to display", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData guildData = GuildData.get(event.getGuild());
        String currency = guildData.economyHandler.getCurrency();

        Item item = guildData.configHandler.getItem(event.getOption("item").getAsString());
        if (item == null) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.economy.inspect)
            )).setEphemeral(true).queue();
            return;
        }
        event.replyEmbeds(item.toEmbed(currency)).queue();
    }
}
