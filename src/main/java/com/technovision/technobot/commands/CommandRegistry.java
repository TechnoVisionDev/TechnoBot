package com.technovision.technobot.commands;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.economy.*;
import com.technovision.technobot.commands.levels.CommandLeaderboard;
import com.technovision.technobot.commands.levels.CommandRank;
import com.technovision.technobot.commands.levels.CommandRankcard;
import com.technovision.technobot.commands.music.*;
import com.technovision.technobot.commands.other.*;
import com.technovision.technobot.commands.staff.*;
import com.technovision.technobot.commands.tickets.CommandTicketMessage;
import com.technovision.technobot.commands.tickets.CommandTicketSettings;
import com.technovision.technobot.listeners.managers.MusicManager;

/**
 * Registers commands and their execution.
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

                // Music
                new CommandJoin(bot.getMusicManager()),
                new CommandLeave(bot.getMusicManager()),
                new CommandPlay(bot),
                new CommandQueue(bot.getMusicManager()),
                new CommandSkip(bot.getMusicManager()),
                new CommandSkipto(bot.getMusicManager()),
                new CommandNp(bot.getMusicManager()),
                new CommandSeek(bot.getMusicManager()),
                new CommandLoop(bot.getMusicManager()),
                new CommandPause(bot.getMusicManager()),
                new CommandResume(bot.getMusicManager()),
                new CommandDj(bot.getMusicManager()),
                new CommandShuffle(bot.getMusicManager()),
                new CommandVolume(bot.getMusicManager()),

                // Staff
                new CommandInfractions(),
                new CommandClearWarn(),
                new CommandWarn(bot),
                new CommandKick(bot),
                new CommandBan(bot),
                new CommandMute(bot),
                new CommandApprove(bot),
                new CommandDeny(bot),
                new CommandConsider(bot),
                new CommandImplement(bot),
                new CommandClear(bot),
                new CommandUnmute(bot, null),

                // Other
                new CommandHelp(bot),
                new CommandSuggest(bot),
                new CommandYoutube(),
                new CommandPing(),
                new CommandGoogle(),
                new CommandReport(),
                new CommandLearnJava(),

                // Tickets
                new CommandTicketMessage(bot),
                new CommandTicketSettings(bot)
        );
    }
}
