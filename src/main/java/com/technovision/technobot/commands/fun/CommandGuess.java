package com.technovision.technobot.commands.fun;

import com.google.common.collect.Sets;
import com.technovision.technobot.util.minigames.Hangman;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Discord Executable Command
 *
 * @author Adex
 */
public class CommandGuess extends Command {

    public CommandGuess(final TechnoBot bot) {
        super(bot, "guess", "Guess something in a game of hangman", "{prefix}g", Command.Category.FUN);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (Hangman.GAMES.containsKey(event.getAuthor().getIdLong())) {  //Checking user has an ongoing game.

            String guess = args[0];  //  !g e -> e
            //  !g carpet -> carpet
            Hangman.guess(event.getTextChannel(), event.getAuthor(), guess.toLowerCase());
        }

        return true;
    }


    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("g");
    }
}
