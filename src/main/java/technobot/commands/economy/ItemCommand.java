package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
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
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

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
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to create", true),
                        new OptionData(OptionType.STRING, "description", "Set the description for this item"),
                        new OptionData(OptionType.INTEGER, "price", "Set the price for this item").setMinValue(0),
                        new OptionData(OptionType.BOOLEAN, "inventory", "Choose if this items appears in the inventory"),
                        new OptionData(OptionType.INTEGER, "duration", "Set the amount of hours this item will remain in the store").setMinValue(0).setMaxValue(8760),
                        new OptionData(OptionType.INTEGER, "stock", "Set the amount of stock this item has").setMinValue(-1),
                        new OptionData(OptionType.ROLE, "required_role", "Set a role required to purchase this item"),
                        new OptionData(OptionType.ROLE, "role_given", "Set a role to be given when this item is used"),
                        new OptionData(OptionType.ROLE, "role_removed", "Set a role to be removed when this item is used"),
                        new OptionData(OptionType.INTEGER, "required_balance", "Set a balance amount required to purchased this item").setMinValue(0),
                        new OptionData(OptionType.STRING, "reply", "Set a message to be sent when this item is used")));
        this.subCommands.add(new SubcommandData("edit", "Edit an existing item in the store.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to edit", true),
                        new OptionData(OptionType.STRING, "description", "Set a new description for this item"),
                        new OptionData(OptionType.INTEGER, "price", "Set a new price for this item").setMinValue(0),
                        new OptionData(OptionType.BOOLEAN, "inventory", "Choose if this items appears in the inventory"),
                        new OptionData(OptionType.INTEGER, "duration", "Set the amount of hours this item will remain in the store").setMinValue(0).setMaxValue(8760),
                        new OptionData(OptionType.INTEGER, "stock", "Set the amount of stock this item has").setMinValue(-1),
                        new OptionData(OptionType.ROLE, "required_role", "Set a role required to purchase this item"),
                        new OptionData(OptionType.ROLE, "role_given", "Set a role to be given when this item is used"),
                        new OptionData(OptionType.ROLE, "role_removed", "Set a role to be removed when this item is used"),
                        new OptionData(OptionType.INTEGER, "required_balance", "Set a balance amount required to purchased this item").setMinValue(0),
                        new OptionData(OptionType.STRING, "reply", "Set a message to be sent when this item is used")));
        this.subCommands.add(new SubcommandData("remove", "Remove an item from your store (members can still use them).")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to remove", true)));
        this.subCommands.add(new SubcommandData("erase", "Erase an item entirely from the store and user inventories.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to remove", true)));
        this.subCommands.add(new SubcommandData("info", "Display details about an item.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to display", true)));
        this.subCommands.add(new SubcommandData("options", "View the different options available for editing an item."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData guildData = GuildData.get(event.getGuild());
        ConfigHandler configHandler = guildData.configHandler;
        String currency = guildData.economyHandler.getCurrency();

        String text = "";
        String name = event.getOption("name").getAsString();
        switch (event.getSubcommandName()) {
            case "create" -> {
                String name = event.getOption("name").getAsString();
                if (configHandler.getConfig().getShop().size() >= MAX_SHOP_SIZE) {
                    text = get(s -> s.economy.item.create.maxReached);
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                if (name.length() < 3) {
                    text = get(s -> s.economy.item.create.tooShort);
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                if (name.length() > 100) {
                    text = "The maximum length for an item name is 100 characters.";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                if (configHandler.containsItem(name)) {
                    text = get(s -> s.economy.item.create.alreadyExists);
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                ItemResult result = updateItem(new Item(name), event);
                MessageEmbed embed = configHandler.addItem(result.item()).toEmbed(currency);
                text = get(s -> s.economy.item.create.success);
                event.reply(text).addEmbeds(embed).queue();
            }
            case "edit" -> {
                // Get existing item
                String name = event.getOption("name").getAsString();
                Item item = configHandler.getItem(name);
                if (item == null) {
                    text = "That item name doesn't exist!";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                    return;
                }
                // Update item in cache and database
                ItemResult result = updateItem(item, event);
                if (!result.isUpdated) {
                    text = get(s -> s.economy.item.edit.notUpdated);
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                } else {
                    configHandler.updateItem(item);
                    text = get(s -> s.economy.item.edit.success);
                    event.reply(text).addEmbeds(item.toEmbed(currency)).queue();
                }
            }
            case "remove" -> {
                String name = configHandler.findClosestItem(event.getOption("name").getAsString());
                if (configHandler.containsItem(name)) {
                    configHandler.removeItem(name);
                    text = EmbedUtils.BLUE_X + " Item has been removed from the store. Users will still be able to `/use` this item if they already own it.";
                    event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
                } else {
                    text = "That item name doesn't exist!";
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                }
            }
            case "erase" -> {
                String name = configHandler.findClosestItem(event.getOption("name").getAsString());
                if (configHandler.containsItem(name)) {
                    configHandler.eraseItem(name);
                    text = EmbedUtils.BLUE_X + " Item has been erased and can never be purchased or used.";
                    event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
                } else {
                    text = get(s -> s.economy.item.noItem);
                    event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
                }
            }
            case "info" -> {
                String name = event.getOption("name").getAsString();
                Item item = configHandler.getItem(name);
                if (item == null) {
                    event.replyEmbeds(EmbedUtils.createError(
                            get(s -> s.economy.item.noItem))
                    ).queue();
                    return;
                }
                event.replyEmbeds(configHandler.getItem(name).toEmbed(currency)).queue();
            }
            case "options" -> {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setTitle("Item Options")
                        .setDescription("Usage: `/item edit <name> <option>`\nType the item name as it appears in the shop, including any spaces and emoji.")
                        .addField("Name", "A name to identify the item. Must be between 3 and 100 characters.", false)
                        .addField("Price", "Price to buy 1 of the item. Can be 0 for free items.", false)
                        .addField("Description", "Describe what it does or what it gives the member buying it.", false)
                        .addField("Inventory", "Whether the item should show in your inventory.\nInventory items have to be used with the `/use <item>` command before roles are given/removed or seeing the item reply.", false)
                        .addField("Duration", "The amount of hours the item will stay in the store for.\nTo remove the duration use `0`.", false)
                        .addField("Stock", "Amount of stock the item has. When the stock is 0 the item cannot be bought.\nUse `-1` for an unlimited stock.", false)
                        .addField("Required Role", "A role the user must already have in order to buy (and use) the item.\nSet the same role a second time to detach it from the item.", false)
                        .addField("Role Given", "The role you are given when buying (non-inventory item) or using (inventory item) the item.\nSet the same role a second time to detach it from the item.", false)
                        .addField("Role Removed", "The role taken when buying (non-inventory item) or using (inventory item) the item.\nSet the same role a second time to detach it from the item.", false)
                        .addField("Required Balance", "The balance the user must have in order to buy (and use if an inventory item) the item.\nSet to `0` for no required balance.", false)
                        .addField("Reply Message", "The message that the bot replies with when the item is bought (non-inventory item) or used (inventory item). You can use member and server placeholder tags in these messages!", false);
                event.replyEmbeds(embed.build()).queue();
            }
        }
    }

    private ItemResult updateItem(Item item, SlashCommandInteractionEvent event) {
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
        if (descOption != null) {
            isUpdated = true;
            item.setDescription(descOption.getAsString());
        }
        if (priceOption != null) {
            isUpdated = true;
            item.setPrice(priceOption.getAsLong());
        }
        if (inventoryOption != null) {
            isUpdated = true;
            item.setShowInInventory(inventoryOption.getAsBoolean());
        }
        if (durationOption != null) {
            isUpdated = true;
            long timestamp = (3600000 * durationOption.getAsLong()) + System.currentTimeMillis();
            item.setExpireTimestamp(timestamp);
        }
        if (stockOption != null) {
            if (durationOption.getAsLong() == 0) {
                item.setExpireTimestamp(null);
            } else {
                long timestamp = (3600000 * durationOption.getAsLong()) + System.currentTimeMillis();
                item.setExpireTimestamp(timestamp);
            }
        } if (stockOption != null) {
            isUpdated = true;
            item.setStock(stockOption.getAsLong());
        }
        if (reqRoleOption != null) {
            Long stock = stockOption.getAsLong() == -1 ? null : stockOption.getAsLong();
            item.setStock(stock);
        } if (reqRoleOption != null) {
            isUpdated = true;
            item.setRequiredRole(reqRoleOption.getAsRole().getIdLong());
        }
        if (roleGivenOption != null) {
            Long roleID = reqRoleOption.getAsRole().getIdLong();
            if (item.getRequiredRole() != null && item.getRequiredRole().equals(roleID)) {
                roleID = null;
            }
            item.setRequiredRole(roleID);
        } if (roleGivenOption != null) {
            isUpdated = true;
            Long roleID = roleGivenOption.getAsRole().getIdLong();
            if (item.getGivenRole() != null && item.getGivenRole().equals(roleID)) {
                roleID = null;
            }
            item.setGivenRole(roleID);
        } if (roleRemovedOption != null) {
            item.setGivenRole(roleGivenOption.getAsRole().getIdLong());
        }
        if (roleRemovedOption != null) {
            isUpdated = true;
            Long roleID = roleRemovedOption.getAsRole().getIdLong();
            if (item.getRemovedRole() != null && item.getRemovedRole().equals(roleID)) {
                roleID = null;
            }
            item.setRemovedRole(roleID);
        } if (reqBalOption != null) {
            item.setRemovedRole(roleRemovedOption.getAsRole().getIdLong());
        }
        if (reqBalOption != null) {
            isUpdated = true;
            Long reqBal = reqBalOption.getAsLong() == 0 ? null : reqBalOption.getAsLong();
            item.setRequiredBalance(reqBal);
        } if (replyOption != null) {
            item.setRequiredBalance(reqBalOption.getAsLong());
        }
        if (replyOption != null) {
            isUpdated = true;
            String reply = replyOption.getAsString().isBlank() ? null : replyOption.getAsString();
            item.setReplyMessage(reply);
        }
        return new ItemResult(item, isUpdated);
    }

    record ItemResult(Item item, boolean isUpdated) {
    }
}
