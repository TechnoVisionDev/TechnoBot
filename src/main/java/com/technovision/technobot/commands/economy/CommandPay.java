package com.technovision.technobot.commands.economy;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import com.technovision.technobot.util.exceptions.InvalidBalanceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandPay extends Command {

    public CommandPay() {
        super("pay", "Send cash to a friend", "{prefix}pay [user] <amount>", Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
        if (args.length > 1) {

            User receiver;
            List<Member> mentions = event.getMessage().getMentionedMembers();
            if (mentions.size() > 0) {
                receiver = mentions.get(0).getUser();
            } else {
                embed.setColor(ERROR_EMBED_COLOR);
                embed.setDescription(":x: Invalid `[user]` argument given\n\nUsage:\n`pay [user] <amount>`");
                event.getChannel().sendMessage(embed.build()).queue();
                return true;
            }
            try {
                long amt = Long.parseLong(args[1]);
                try {
                    TechnoBot.getInstance().getEconomy().pay(event.getAuthor(), receiver, amt);
                    embed.setColor(EconManager.SUCCESS_COLOR);
                    String money = EconManager.FORMATTER.format(amt);
                    embed.setDescription(":white_check_mark: <@!" + receiver.getId() + "> has received your " + EconManager.SYMBOL + money);
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                } catch (InvalidBalanceException e) {
                    embed.setColor(ERROR_EMBED_COLOR);
                    long bal = TechnoBot.getInstance().getEconomy().getBalance(event.getAuthor()).getLeft();
                    String balFormat = EconManager.FORMATTER.format(bal);
                    embed.setDescription(String.format(":x: You don't have that much money to give! You currently have %s%s on hand", EconManager.SYMBOL, balFormat));
                    event.getChannel().sendMessage(embed.build()).queue();
                    return true;
                }
            } catch (NumberFormatException e) {
                embed.setColor(ERROR_EMBED_COLOR);
                embed.setDescription(":x: Invalid `<amount>` argument given\n\nUsage:\n`pay [user] <amount>`");
                event.getChannel().sendMessage(embed.build()).queue();
                return true;
            }
        }
        embed.setColor(ERROR_EMBED_COLOR);
        embed.setDescription(":x: Too few arguments given.\n\nUsage:\n`pay [user] <amount>`");
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
