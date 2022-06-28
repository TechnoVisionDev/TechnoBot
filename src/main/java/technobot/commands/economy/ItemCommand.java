package technobot.commands.economy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Item;
import technobot.handlers.ConfigHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that performs CRUD operations for economy shop items.
 *
 * @author TechnoVision
 */
public class ItemCommand extends Command {

    public static final int MAX_SHOP_SIZE = 10;

    public ItemCommand(TechnoBot bot) {
        super(bot);
        this.name = "item";
        this.description = "Modify this server's shop items.";
        this.category = Category.ECONOMY;
        this.permission = Permission.MANAGE_SERVER;
        this.subCommands.add(new SubcommandData("create", "Create an item for your store.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to create", true)));
        this.subCommands.add(new SubcommandData("edit", "Edit an existing item in the store.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to edit", true),
                            new OptionData(OptionType.STRING, "description", "Set a new description for this item"),
                            new OptionData(OptionType.INTEGER, "price", "Set a new price for this item").setMinValue(0),
                            new OptionData(OptionType.BOOLEAN, "inventory", "Choose if this items appears in the inventory"),
                            new OptionData(OptionType.INTEGER, "duration", "Set the amount of hours this item will remain in the store").setMinValue(0).setMaxValue(8760),
                            new OptionData(OptionType.INTEGER, "stock", "Set the amount of stock this item has").setMinValue(0),
                            new OptionData(OptionType.ROLE, "required_role", "Set a role required to purchase this item"),
                            new OptionData(OptionType.ROLE, "role_given", "Set a role to be given when this item is used"),
                            new OptionData(OptionType.ROLE, "role_removed", "Set a role to be removed when this item is used"),
                            new OptionData(OptionType.INTEGER, "required_balance", "Set a balance amount required to purchased this item").setMinValue(0),
                            new OptionData(OptionType.STRING, "reply", "Set a message to be sent when this item is used")));
        this.subCommands.add(new SubcommandData("remove", "Removes an item from your store.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to remove", true)));
        this.subCommands.add(new SubcommandData("info", "Display details about an item.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to display", true)));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData guildData = GuildData.get(event.getGuild());
        ConfigHandler configHandler = guildData.configHandler;
        String currency = guildData.economyHandler.getCurrency();

        String text = "";
        String name = event.getOption("name").getAsString();
        switch(event.getSubcommandName()) {
            case "create" -> {
                if (configHandler.getConfig().getShop().size() >= MAX_SHOP_SIZE) {
                    text = "You have reached the maximum item limit! Use `/item remove` to make some room before adding a new item.";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                if (configHandler.containsItem(name)) {
                    text = "There is already an item with that name!";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                MessageEmbed embed = configHandler.createItem(name).toEmbed(currency);
                text = EmbedUtils.GREEN_TICK + " Item created successfully!";
                event.reply(text).addEmbeds(embed).queue();
            }
            case "edit" -> {
                // Get command options from discord
                OptionMapping descOption = event.getOption("description");
                OptionMapping priceOption = event.getOption("price");
                OptionMapping inventoryOption = event.getOption("inventory");
                OptionMapping durationOption = event.getOption("duration");
                OptionMapping stockOption = event.getOption("stock");
                OptionMapping reqRoleOption = event.getOption("required_role");
                OptionMapping roleGivenOption = event.getOption("role_given");
                OptionMapping roleRemovedOption = event.getOption("role_removed");
                OptionMapping reqBalOption = event.getOption("required_balance");
                OptionMapping replyOption = event.getOption("reply");

                // Update item fields
                boolean isUpdated = false;
                Item item = configHandler.getItem(name);
                if (item == null) {
                    text = "That item name doesn't exist!";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                } if (descOption != null) {
                    isUpdated = true;
                    item.setDescription(descOption.getAsString());
                } if (priceOption != null) {
                    isUpdated = true;
                    item.setPrice(priceOption.getAsLong());
                } if (inventoryOption != null) {
                    isUpdated = true;
                    item.setShowInInventory(inventoryOption.getAsBoolean());
                } if (durationOption != null) {
                    isUpdated = true;
                    long timestamp = (3600000 * durationOption.getAsLong()) + System.currentTimeMillis();
                    item.setExpireTimestamp(timestamp);
                } if (stockOption != null) {
                    isUpdated = true;
                    item.setStock(stockOption.getAsLong());
                } if (reqRoleOption != null) {
                    isUpdated = true;
                    item.setRequiredRole(reqRoleOption.getAsRole().getIdLong());
                } if (roleGivenOption != null) {
                    isUpdated = true;
                    item.setGivenRole(roleGivenOption.getAsRole().getIdLong());
                } if (roleRemovedOption != null) {
                    isUpdated = true;
                    item.setRemovedRole(roleRemovedOption.getAsRole().getIdLong());
                } if (reqBalOption != null) {
                    isUpdated = true;
                    item.setRequiredBalance(reqBalOption.getAsLong());
                } if (replyOption != null) {
                    isUpdated = true;
                    item.setReplyMessage(replyOption.getAsString());
                }
                // Update item in cache and database
                if (!isUpdated) {
                    text = "Use one of the available command options to edit this item!";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                } else {
                    configHandler.updateItem(item);
                    text = EmbedUtils.GREEN_TICK + " Item updated successfully!";
                    event.reply(text).addEmbeds(item.toEmbed(currency)).queue();
                }
            }
            case "remove" -> {
                if (configHandler.containsItem(name)) {
                    configHandler.removeItem(name);
                    text = EmbedUtils.BLUE_TICK + " Item has been removed from the store.";
                    event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
                } else {
                    text = "That item name doesn't exist!";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                }
            }
            case "info" -> {
                Item item = configHandler.getItem(name);
                if (item == null) {
                    event.replyEmbeds(EmbedUtils.createError("That item does not exist!")).queue();
                    return;
                }
                event.replyEmbeds(configHandler.getItem(name).toEmbed(currency)).queue();
            }
        }
    }
}
