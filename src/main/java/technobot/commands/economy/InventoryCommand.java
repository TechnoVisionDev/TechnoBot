package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedColor;

import java.util.*;

/**
 * Command that shows a user's inventory.
 *
 * @author TechnoVision
 */
public class InventoryCommand extends Command {

    public static final int ITEMS_PER_PAGE = 6;

    public InventoryCommand(TechnoBot bot) {
        super(bot);
        this.name = "inventory";
        this.description = "View your inventory items.";
        this.category = Category.ECONOMY;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData guildData = GuildData.get(event.getGuild());
        User user = event.getUser();

        // Build embed template
        String info = "Use an item with the `/use <item>` command.";
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setAuthor(user.getAsTag()+"'s Inventory", null, user.getEffectiveAvatarUrl())
                .setDescription(info);

        // Get inventory data for user
        LinkedHashMap<String,Long> inv = guildData.economyHandler.getInventory(user.getIdLong());
        if (inv == null || inv.isEmpty()) {
            embed.setDescription("You do not have any items!");
            event.replyEmbeds(embed.build()).queue();
            return;
        }

        // Create paginated embeds
        int count = 0;
        List<MessageEmbed> embeds = new ArrayList<>();
        ListIterator<Map.Entry<String, Long>> it = new ArrayList<>(inv.entrySet()).listIterator(inv.entrySet().size());
        while(it.hasPrevious()) {
            Map.Entry<String, Long> entry = it.previous();
            String uuid = entry.getKey();
            long itemCount = entry.getValue();
            Item item = guildData.configHandler.getItemByID(uuid);
            if (item != null) {
                String desc = item.getDescription() != null ? "**\n" + item.getDescription() : "**";
                embed.appendDescription("\n\n**" + itemCount + " - " + item.getName() + desc);
                count++;
                if (count % ITEMS_PER_PAGE == 0) {
                    embeds.add(embed.build());
                    embed.setDescription(info);
                }
            }
        }
        if (count % ITEMS_PER_PAGE != 0) {
            embeds.add(embed.build());
        }

        // Send embed
        if (embeds.isEmpty()) {
            embed.setDescription("You do not have any items!");
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
