package com.technovision.technobot.commands.economy;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import com.technovision.technobot.util.exceptions.InvalidBalanceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandWithdraw extends Command {
    private final TechnoBot bot;

    public CommandWithdraw(final TechnoBot bot) {
        super("withdraw", "Withdraw cash from the bank", "{prefix}withdraw <amount>", Category.ECONOMY);
        this.bot = bot;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
        if (args.length > 0) {
            try {
                long amt = Long.parseLong(args[0]);
                try {
                    bot.getEconomy().withdraw(event.getAuthor(), amt);
                    embed.setColor(EconManager.SUCCESS_COLOR);
                    String money = EconManager.FORMATTER.format(amt);
                    embed.setDescription(":white_check_mark: Withdrew " + EconManager.SYMBOL + money + " from your bank!");
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                } catch (InvalidBalanceException e) {
                    embed.setColor(ERROR_EMBED_COLOR);
                    long bankBal = bot.getEconomy().getBalance(event.getAuthor()).getRight();
                    String bankBalFormat = EconManager.FORMATTER.format(bankBal);
                    embed.setDescription(String.format(":x: You don't have that much money to withdraw! You currently have %s%s in the bank.", EconManager.SYMBOL, bankBalFormat));
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                }
            } catch (NumberFormatException e) {
                if (args[0].equalsIgnoreCase("all") || args[0].equalsIgnoreCase("half")) {
                    String amount = args[0];

                    try {
                        bot.getEconomy().withdraw(event.getAuthor(), amount);

                        embed.setColor(EconManager.SUCCESS_COLOR);
                        embed.setDescription(":white_check_mark: Withdrew " + amount + " of your money from your bank!");
                        event.getChannel().sendMessage(embed.build()).queue();

                        return true;
                    } catch (InvalidBalanceException ee) {
                        embed.setColor(ERROR_EMBED_COLOR);
                        long bankBal = bot.getEconomy().getBalance(event.getAuthor()).getRight();
                        String bankBalFormat = EconManager.FORMATTER.format(bankBal);
                        embed.setDescription(String.format(":x: You don't have that much money to withdraw! You currently have %s%s in the bank.", EconManager.SYMBOL, bankBalFormat));
                        event.getChannel().sendMessage(embed.build()).queue();
                        return true;
                    }

                } else {
                    embed.setColor(ERROR_EMBED_COLOR);
                    embed.setDescription(":x: Invalid `<amount>` argument given\n\nUsage:\n`withdraw <amount>`");
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                }
            }
        }
        embed.setColor(ERROR_EMBED_COLOR);
        embed.setDescription(":x: Too few arguments given.\n\nUsage:\n`withdraw <amount>`");
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
