package technobot.commands.economy;

import net.dv8tion.jda.api.Permission;
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
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
