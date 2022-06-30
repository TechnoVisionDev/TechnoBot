package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.handlers.economy.EconomyHandler;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedColor;

import java.util.*;

/**
 * Command that displays the server shop and available items.
 *
 * @author TechnoVision
 */
public class ShopCommand extends Command {

    public static final int ITEMS_PER_PAGE = 6;

    public ShopCommand(TechnoBot bot) {
        super(bot);
        this.name = "shop";
        this.description = "View a list of items available to buy in the store.";
        this.category = Category.ECONOMY;
    }

    public void execute(SlashCommandInteractionEvent event) {
        // Create base embed template
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setAuthor(event.getGuild().getName()+" Store", null, event.getGuild().getIconUrl());

        // Check if shop is empty
        GuildData guildData = GuildData.get(event.getGuild());
        if (guildData.configHandler.getConfig().getShop().isEmpty()) {
            embed.setDescription("There are no items in this shop!\nUse the `/item create <name>` command to add some.");
            event.replyEmbeds(embed.build()).queue();
            return;
        }
        String info = "Buy an item with the `/buy <item> [quantity]` command.\n"+"For more information on an item use the `/inspect <item>` command.";
        embed.setDescription(info);

        // Create paginated embeds
        List<MessageEmbed> embeds = new ArrayList<>();
        int count = 0;
        LinkedHashMap<String, Item> shop = guildData.configHandler.getConfig().getShop();
        ListIterator<Map.Entry<String, Item>> it = new ArrayList<>(shop.entrySet()).listIterator(shop.entrySet().size());
        while(it.hasPrevious()) {
            Item item = it.previous().getValue();
            String price = guildData.economyHandler.getCurrency() + " " + EconomyHandler.FORMATTER.format(item.getPrice());
            String desc = item.getDescription() != null ? "**\n" + item.getDescription() : "**";
            embed.appendDescription("\n\n**" + price + " - " + item.getName() + desc);
            if (item.isExpired()) embed.appendDescription(" [Expired]");
            count++;
            if (count % ITEMS_PER_PAGE == 0) {
                embeds.add(embed.build());
                embed.setDescription(info);
            }
        }
        if (count % ITEMS_PER_PAGE != 0) {
            embeds.add(embed.build());
        }

        // Send embed
        ReplyCallbackAction action = event.replyEmbeds(embeds.get(0));
        if (embeds.size() > 1) {
            ButtonListener.sendPaginatedMenu(event.getUser().getId(), action, embeds);
        } else {
            action.queue();
        }
    }
}
