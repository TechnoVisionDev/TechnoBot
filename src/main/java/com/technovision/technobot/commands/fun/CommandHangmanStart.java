package com.technovision.technobot.commands.fun;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.tecgnovision.technobot.                                   //////////
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Discord Executable Command
 * @author Adex
 */
public class CommandHangmanStart extends Command{

    public CommandHangmanStart(final Technobot bot) {
        super(bot, "hangman", "Start a game of hangman", "{prefix}play hangman", Command.Category.FUN);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        Hangman.startGame(event.getTextChannel());

        return true;
    }

}
