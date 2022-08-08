package technobot.commands.economy;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that displays and modifies a guild's economy config.
 *
 * @author TechnoVision
 */
public class EconomyCommand extends Command {

    public EconomyCommand(TechnoBot bot) {
        super(bot);
        this.name = "economy";
        this.description = "Modify this server's economy config.";
        this.category = Category.ECONOMY;
        this.permission = Permission.MANAGE_SERVER;
        this.subCommands.add(new SubcommandData("currency", "Set the currency symbol.")
                .addOptions(new OptionData(OptionType.STRING, "symbol", "The emoji or symbol to set as the currency.")));
        this.subCommands.add(new SubcommandData("add", "Add money to the balance of a user.")
                .addOptions(new OptionData(OptionType.USER, "user", "The user you want to add money to.", true))
                .addOptions(new OptionData(OptionType.INTEGER, "amount", "The amount of money to add", true)));
        this.subCommands.add(new SubcommandData("remove", "Remove money from the balance of a user.")
                .addOptions(new OptionData(OptionType.USER, "user", "The user you want to remove money from.", true))
                .addOptions(new OptionData(OptionType.INTEGER, "amount", "The amount of money to remove", true)));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;

        String text = "";
        switch (event.getSubcommandName()) {
            case "currency" -> {
                OptionMapping symbolOption = event.getOption("symbol");
                if (symbolOption != null) {
                    // Update currency symbol
                    String symbol = symbolOption.getAsString();
                    if (symbol.length() > 100) {
                        text = "The maximum length for the currency symbol is 100 characters!";
                        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                        return;
                    } else if (!symbol.startsWith("<") && !symbol.endsWith(">") && symbol.matches(".*[0-9].*")) {
                        text = "The currency symbol cannot contain numbers!";
                        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                        return;
                    }
                    economyHandler.setCurrency(symbol);
                    text = EmbedUtils.BLUE_TICK + " The currency symbol has been updated to **" + symbol + "**";
                } else {
                    // Reset currency symbol to default
                    economyHandler.resetCurrency();
                    text = EmbedUtils.BLUE_TICK + " The currency symbol has been reset.";
                }
            }
            case "add" -> {
                User user = event.getOption("user").getAsUser();
                long amount = event.getOption("amount").getAsLong();
                economyHandler.addMoney(user.getIdLong(), amount);
                String currency = economyHandler.getCurrency() + " **" + EconomyHandler.FORMATTER.format(amount) + "**";
                text = EmbedUtils.BLUE_TICK + " Successfully added " + currency + " to " + user.getAsMention();
            }
            case "remove" -> {
                User user = event.getOption("user").getAsUser();
                long amount = event.getOption("amount").getAsLong();
                long networth = economyHandler.getNetworth(user.getIdLong());
                if (amount > networth) {
                    text = "You cannot remove more money than a user has!";
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                    return;
                }
                economyHandler.removeMoney(user.getIdLong(), amount);
                String currency = economyHandler.getCurrency() + " **" + EconomyHandler.FORMATTER.format(amount) + "**";
                text = EmbedUtils.BLUE_TICK + " Successfully removed " + currency + " from " + user.getAsMention();
            }
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
