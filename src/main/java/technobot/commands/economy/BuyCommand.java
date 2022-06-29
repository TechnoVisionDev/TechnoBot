package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that buys an item from the server shop.
 *
 * @author TechnoVision
 */
public class BuyCommand extends Command {

    public BuyCommand(TechnoBot bot) {
        super(bot);
        this.name = "buy";
        this.description = "Purchase an item from the shop.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.STRING, "item", "The name of the item to purchase", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData guildData = GuildData.get(event.getGuild());
        EconomyHandler econ = guildData.economyHandler;

        // Get item data
        String itemName = event.getOption("item").getAsString();
        Item item = guildData.configHandler.getItem(itemName);
        if (item == null) {
            event.replyEmbeds(EmbedUtils.createError("That item doesn't exist! See all valid items with `/shop`")).setEphemeral(true).queue();
            return;
        }

        // Attempt to purchase item
        long balance = econ.getBalance(event.getUser().getIdLong());
        if (balance >= item.getPrice()) {
            // Purchase was successful
            econ.buyItem(event.getUser().getIdLong(), item);
            if (item.getShowInInventory()) {
                String price = econ.getCurrency() + " " + item.getPrice();
                String text = EmbedUtils.GREEN_TICK + " You have bought 1 " + item.getName() + " for " + price + "! This is now in your inventory.\nUse this item with the `/use <item>` command.";
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.SUCCESS.color)
                        .setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl())
                        .setDescription(text);
                event.replyEmbeds(embed.build()).queue();
            } else {
                // TODO: Instantly use item
                event.reply(":thumbsup:").queue();
            }
        } else {
            // Not enough money to purchase
            String value = econ.getCurrency() + " " + EconomyHandler.FORMATTER.format(balance);
            String text = " You do not have enough money to buy this item. You currently have "+value+" on hand.";
            EmbedBuilder embed = new EmbedBuilder().setDescription(EmbedUtils.RED_X + text).setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
