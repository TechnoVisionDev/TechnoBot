package technobot.commands.economy;

import net.dv8tion.jda.api.entities.User;
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

        // Check if item exists
        String itemName = event.getOption("item").getAsString();
        Item item = guildData.configHandler.getItem(itemName);
        if (item == null) {
            event.replyEmbeds(EmbedUtils.createError("That item doesn't exist! See your items with `/inventory`")).setEphemeral(true).queue();
            return;
        }

        // Check if item is in inventory
        User user = event.getUser();
        long itemCount = econ.countItemInInventory(user.getIdLong(), item.getUuid());
        if (itemCount <= 0) {
            event.replyEmbeds(EmbedUtils.createError("You don't own that item! See your items with `/inventory`")).setEphemeral(true).queue();
            return;
        }

        // Check for requirements
        if (!econ.canUseItem(event.getMember(), item)) {
            // Does not meet requirements
            event.replyEmbeds(EmbedUtils.createError("You do not meet the requirements to use that item! Use `/inspect <item>` for more details.")).setEphemeral(true).queue();
        } else {
            // Use item
            econ.useItem(event.getMember(), item, itemCount);
            String reply = item.getReplyMessage() != null ? item.getReplyMessage() : ":thumbsup:";
            event.reply(reply).queue();
        }
    }
}
