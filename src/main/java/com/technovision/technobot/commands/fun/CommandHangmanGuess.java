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
        super(bot, "guess", "Guess something in a game of hangman", "{prefix}g   ", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
         String messageContent = event.getMessage().getContentRaw();

        String guess = "";
        for (int i = 3; i < messageContent.length(); i++) {
            guess += messageContent.charAt(i);
        }
        Hangman.guess(event.getTextChannel(), guess);

        return true;
    }

}
