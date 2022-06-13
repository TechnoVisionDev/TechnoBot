package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that deposits cash into user's bank.
 *
 * @author TechnoVision
 */
public class DepositCommand extends Command {

    public DepositCommand(TechnoBot bot) {
        super(bot);
        this.name = "deposit";
        this.description = "Deposit your money to the bank.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.INTEGER, "amount", "The amount of money you want to deposit.")
                .setMinValue(1));
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        User user = event.getUser();
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        String currency = economyHandler.getCurrency();
        long balance = economyHandler.getBalance(user.getIdLong());

        EmbedBuilder embed = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        if (balance <= 0) {
            // Balance is at 0
            embed.setDescription(EmbedUtils.RED_X + " You don't have any money to deposit!");
            embed.setColor(EmbedColor.ERROR.color);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            return;
        }

        OptionMapping amountOption = event.getOption("amount");
        long amount;
        if (amountOption != null) {
            amount = amountOption.getAsInt();
            if (amount > balance) {
                // Amount is higher than balance
                String value = currency + " " + EconomyHandler.FORMATTER.format(balance);
                embed.setDescription(EmbedUtils.RED_X + " You cannot deposit more than " + value + "!");
                embed.setColor(EmbedColor.ERROR.color);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
                return;
            }
        } else {
            amount = balance;
        }
        economyHandler.deposit(user.getIdLong(), amount);

        // Send embed message
        String value = currency + " " + EconomyHandler.FORMATTER.format(amount);
        embed.setDescription(EmbedUtils.GREEN_TICK + " Deposited " + value + " to your bank!");
        embed.setColor(EmbedColor.SUCCESS.color);
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
