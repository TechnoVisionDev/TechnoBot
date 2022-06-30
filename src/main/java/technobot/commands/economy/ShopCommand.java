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

import static technobot.util.Localization.get;

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
                .setAuthor(event.getGuild().getName() + " Store", null, event.getGuild().getIconUrl());

        // Check if shop is empty
        GuildData guildData = GuildData.get(event.getGuild());
        if (guildData.configHandler.getConfig().getShop().isEmpty()) {
            embed.setDescription(get(s -> s.economy.shop.empty));
            event.replyEmbeds(embed.build()).queue();
            return;
        }
        embed.setDescription(get(s -> s.economy.shop.info));

        // Create paginated embeds
        List<MessageEmbed> embeds = new ArrayList<>();
        int count = 0;
        LinkedHashMap<String, Item> items = guildData.configHandler.getConfig().getItems();
        LinkedHashMap<String, String> shop = guildData.configHandler.getConfig().getShop();
        ListIterator<Map.Entry<String, String>> it = new ArrayList<>(shop.entrySet()).listIterator(shop.entrySet().size());
        while(it.hasPrevious()) {
            Item item = items.get(it.previous().getValue());
            String price = guildData.economyHandler.getCurrency() + " " + EconomyHandler.FORMATTER.format(item.getPrice());
            String desc = item.getDescription() != null ? "**\n" + item.getDescription() : "**";
            embed.appendDescription("\n\n**" + price + " - " + item.getName() + desc);
            count++;
            if (count % ITEMS_PER_PAGE == 0) {
                embeds.add(embed.build());
                embed.setDescription(get(s -> s.economy.shop.info));
            }
        }
        if (count % ITEMS_PER_PAGE != 0) {
            embeds.add(embed.build());
        }

        // Send embed
        if (embeds.isEmpty()) {
            event.replyEmbeds(embed.build()).queue();
            return;
        }
        ReplyCallbackAction action = event.replyEmbeds(embeds.get(0));
        if (embeds.size() > 1) {
            ButtonListener.sendPaginatedMenu(event.getUser().getId(), action, embeds);
        } else {
            action.queue();
        }
    }
}
