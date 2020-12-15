package com.technovision.technobot.commands;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.economy.*;
import com.technovision.technobot.commands.levels.CommandLeaderboard;
import com.technovision.technobot.commands.levels.CommandRank;
import com.technovision.technobot.commands.levels.CommandRankcard;
import com.technovision.technobot.commands.music.*;
import com.technovision.technobot.commands.other.*;
import com.technovision.technobot.commands.staff.*;

/**
 * Registers commands and their execution.
 *
 * @author TechnoVision
 * @author Sparky
 */
public class CommandRegistry {

    public CommandRegistry() {
        TechnoBot.getInstance().getRegistry().registerCommands(

                // Levels
                new CommandRank(),
                new CommandRankcard(),
                new CommandLeaderboard(),

                // Economy
                new CommandBalance(),
                new CommandWork(),
                new CommandCrime(),
                new CommandPay(),
                new CommandRob(),
                new CommandDeposit(),
                new CommandWithdraw(),

                // Music
                new CommandJoin(),
                new CommandLeave(),
                new CommandPlay(),
                new CommandQueue(),
                new CommandSkip(),
                new CommandSkipto(),
                new CommandNp(),
                new CommandSeek(),
                new CommandLoop(),
                new CommandPause(),
                new CommandResume(),
                new CommandDj(),
                new CommandShuffle(),
                new CommandVolume(),

                // Staff
                new CommandInfractions(),
                new CommandClearWarn(),
                new CommandWarn(),
                new CommandKick(),
                new CommandBan(),
                new CommandMute(),
                new CommandApprove(),
                new CommandDeny(),
                new CommandConsider(),
                new CommandImplement(),
                new CommandClear(),
                new CommandUnmute(),

                // Other
                new CommandHelp(),
                new CommandSuggest(),
                new CommandYoutube(),
                new CommandPing(),
                new CommandGoogle(),
                new CommandReport()
        );
    }
}
