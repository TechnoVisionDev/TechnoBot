package com.technovision.technobot.commands.fun;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.tecgnovision.technobot.util.minigames.Hangman;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 * Discord Executable Command
 * @author Adex
 */
 public class CommandGuess extends Command {

    public CommandGuess(final TechnoBot bot) {
        super(bot, "guess", "Guess something in a game of hangman", "{prefix}g", Command.Category.FUN);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
         if (Hangman.GAMES.containsKey(event.getAuthor().getIDLong())) {  //Checking user has an ongoing game.
    
            String guess = event.getMessage().getContentRaw().substring(3);  //  !g e -> e
                                                                             //  !g carpet -> carpet
            Hangman.guess(event.getTextChannel(), event.getAuthor() , guess);
        }

        return true;
    }

}
