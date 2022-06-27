package technobot.commands.economy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to create.", true)));
        this.subCommands.add(new SubcommandData("edit", "Edit an existing item in the store.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to edit.", true)));
        this.subCommands.add(new SubcommandData("remove", "Removes an item from your store.")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to remove.", true)));
        this.subCommands.add(new SubcommandData("info", "Display details about an item..")
                .addOptions(new OptionData(OptionType.STRING, "name", "The name of the item to display.", true)));
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
                return;
            }
            case "edit" -> {
               text = "This command is under construction! Coming soon...";
            }
            case "remove" -> {
                text = "This command is under construction! Coming soon...";
            }
            case "info" -> {
                Item item = configHandler.getItem(name);
                if (item == null) {
                    event.replyEmbeds(EmbedUtils.createError("That item does not exist!")).queue();
                    return;
                }
                event.replyEmbeds(configHandler.getItem(name).toEmbed(currency)).queue();
                return;
            }
        }
        event.replyEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
