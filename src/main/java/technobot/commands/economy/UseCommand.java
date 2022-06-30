package technobot.commands.economy;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedUtils;

import java.util.LinkedHashMap;

/**
 * Command that uses an item from the inventory.
 *
 * @author TechnoVision
 */
public class UseCommand extends Command {

    public UseCommand(TechnoBot bot) {
        super(bot);
        this.name = "use";
        this.description = "Use an item in your inventory.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.STRING, "item", "The name of the item to use", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData guildData = GuildData.get(event.getGuild());
        EconomyHandler econ = guildData.economyHandler;
        long userID = event.getUser().getIdLong();

        // Check if item exists & is in inventory
        String itemName = event.getOption("item").getAsString();
        ItemAndCount response = getItemForUse(itemName, userID, guildData);
        Item item = response.item();
        if (item == null) {
            event.replyEmbeds(EmbedUtils.createError("You don't own that item! See your items with `/inventory`")).setEphemeral(true).queue();
            return;
        }

        // Check for requirements
        if (!econ.canUseItem(event.getMember(), item)) {
            // Does not meet requirements
            event.replyEmbeds(EmbedUtils.createError("You do not meet the requirements to use that item! Use `/inspect <item>` for more details.")).setEphemeral(true).queue();
        } else {
            // Use item
            econ.useItem(event.getMember(), item, response.count());
            String reply = item.getReplyMessage() != null ? item.getReplyMessage() : ":thumbsup:";
            event.reply(reply).queue();
        }
    }

    /**
     * Loops through item map and checks if name and uuid match any.
     * This is necessary in the case that an item is created with the same name as a deleted one.
     *
     * @param itemName the name of the item to search for.
     * @param userID the ID of the user using this item.
     * @param guildData the GuildData instance for the user's guild.
     * @return an ItemAndCount object, which contains the item itself and it's count in the inventory. Both may be null.
     */
    private ItemAndCount getItemForUse(String itemName, long userID, GuildData guildData) {
        LinkedHashMap<String, Long> inventory = guildData.economyHandler.getInventory(userID);
        for (Item item : guildData.configHandler.getConfig().getItems().values()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                Long count = inventory.get(item.getUuid());
                if (count != null) {
                    return new ItemAndCount(item, count);
                }
            }
        }
        return new ItemAndCount(null, 0);
    }

    /**
     * Used exclusively for getItemForUse to store a pair response.
     *
     * @param item an inventory item object.
     * @param count the number of occurrences of this item in a user's inventory.
     */
    private record ItemAndCount(Item item, long count) { }
}
