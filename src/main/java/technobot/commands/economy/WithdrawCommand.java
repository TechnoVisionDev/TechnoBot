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

import static technobot.util.Localization.get;

/**
 * Command that withdraws cash from the user's bank.
 *
 * @author TechnoVision
 */
public class WithdrawCommand extends Command {

    public WithdrawCommand(TechnoBot bot) {
        super(bot);
        this.name = "withdraw";
        this.description = "Withdraw your money from the bank.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.INTEGER, "amount", "The amount of money you want to deposit.").setMinValue(1));
    }

    public void execute(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        long bank = economyHandler.getBank(user.getIdLong());

        EmbedBuilder embed = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        if (bank <= 0) {
            // Bank is at 0
            embed.setDescription(get(s -> s.economy.withdraw.noMoney));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        OptionMapping amountOption = event.getOption("amount");
        long amount;
        if (amountOption != null) {
            amount = amountOption.getAsLong();
            if (amount > bank) {
                // Amount is higher than balance
                embed.setDescription(get(s -> s.economy.withdraw.tooMuch, bank));
                embed.setColor(EmbedColor.ERROR.color);
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
        } else {
            amount = bank;
        }
        economyHandler.withdraw(user.getIdLong(), amount);

        // Send embed message
        embed.setDescription(get(s -> s.economy.withdraw.success, amount));
        embed.setColor(EmbedColor.SUCCESS.color);
        event.replyEmbeds(embed.build()).queue();
    }
}
