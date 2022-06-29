package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.LinkedHashMap;

/**
 * Command that shows a user's inventory.
 *
 * @author TechnoVision
 */
public class InventoryCommand extends Command {

    public static final int MAX_ITEMS_PER_PAGE = 6;

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

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setAuthor(user.getAsTag()+"'s Inventory", null, user.getEffectiveAvatarUrl())
                .setDescription("Use an item with the `/use <item>` command.");

        LinkedHashMap<String,Long> inv = guildData.economyHandler.getInventory(user.getIdLong());
        if (inv == null || inv.isEmpty()) {
            embed.setDescription("You do not have any items!");
            event.replyEmbeds(embed.build()).queue();
            return;
        }

        // TODO: Add pagination
        for (String uuid : inv.keySet()) {
            long count = inv.get(uuid);
            Item item = guildData.configHandler.getItemByID(uuid);
            if (item != null) {
                String desc = item.getDescription() != null ? "**\n" + item.getDescription() : "**";
                embed.appendDescription("\n\n**" + count + " - " + item.getName() + desc);
            }
        }
        event.replyEmbeds(embed.build()).queue();
    }
}
