package com.technovision.technobot.commands;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.economy.*;
import com.technovision.technobot.commands.fun.CommandRoll;
import com.technovision.technobot.commands.fun.CommandSay;
import com.technovision.technobot.commands.levels.CommandLeaderboard;
import com.technovision.technobot.commands.levels.CommandRank;
import com.technovision.technobot.commands.levels.CommandRankcard;
import com.technovision.technobot.commands.music.*;
import com.technovision.technobot.commands.other.*;
import com.technovision.technobot.commands.staff.*;
import com.technovision.technobot.commands.tickets.CommandTicketMessage;
import com.technovision.technobot.commands.tickets.CommandTicketSettings;

/**
 * Registers commands and their execution.
 *
 * @author TechnoVision
 * @author Sparky
 */
public class CommandRegistry {

    public CommandRegistry(final TechnoBot bot) {
        bot.getRegistry().registerCommands(

                // Levels
                new CommandRank(bot),
                new CommandRankcard(bot),
                new CommandLeaderboard(bot),

                // Economy
                new CommandBalance(bot),
                new CommandWork(bot),
                new CommandCrime(bot),
                new CommandPay(bot),
                new CommandRob(bot),
                new CommandDeposit(bot),
                new CommandWithdraw(bot),
                new CommandBalTop(bot),

                // Music
                new CommandJoin(bot),
                new CommandLeave(bot),
                new CommandPlay(bot),
                new CommandQueue(bot),
                new CommandSkip(bot),
                new CommandSkipTo(bot),
                new CommandNp(bot),
                new CommandSeek(bot),
                new CommandLoop(bot),
                new CommandPause(bot),
                new CommandResume(bot),
                new CommandDj(bot),
                new CommandShuffle(bot),
                new CommandVolume(bot),

                // Staff
                new CommandInfractions(bot),
                new CommandClearWarn(bot),
                new CommandWarn(bot),
                new CommandKick(bot),
                new CommandBan(bot),
                new CommandMute(bot),
                new CommandApprove(bot),
                new CommandDeny(bot),
                new CommandConsider(bot),
                new CommandImplement(bot),
                new CommandClear(bot),
                new CommandUnmute(bot),

                // Other
                new CommandHelp(bot),
                new CommandSuggest(bot),
                new CommandYoutube(bot),
                new CommandPing(bot),
                new CommandGoogle(bot),
                new CommandReport(bot),
                new CommandLearnJava(bot),
                new CommandLearnFabric(bot),

                // Tickets
                new CommandTicketMessage(bot),
                new CommandTicketSettings(bot),

                // Fun
                new CommandSay(bot),
                new CommandRoll(bot)
        );
    }
}
