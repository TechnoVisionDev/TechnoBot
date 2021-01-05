package com.technovision.technobot.util.minigames;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Discord Minigame
 *
 * @author Adex
 */
public class Hangman {

    public static final HashMap<TextChannel, Hangman> GAMES = new HashMap<>();

    private final String word;
    private int livesLeft;
    private final TextChannel channel;
    private final ArrayList<Character> guessed;
    private final ArrayList<Character> wrong;

    private Hangman(TextChannel channel) {
        word = WordList.getWord();
        livesLeft = 10;
        this.channel = channel;
        guessed = new ArrayList<>();
        wrong = new ArrayList<>();
    }

    public static void startGame(TextChannel channel) {
        if (GAMES.containsKey(channel)) {
            channel.sendMessage("This channel already has a game of Hangman on.").queue();
        } else {
            GAMES.put(channel, new Hangman(channel));
            GAMES.get(channel).sendWord();
        }
    }

    public ArrayList<Character> getGuessed() {
        return guessed;
    }

    public void addGuessed(char guessed) {
        this.guessed.add(guessed);
        sort();
    }

    public String getWord() {
        return word;
    }

    public void reduceLive() {
        livesLeft--;
    }

    public int getLivesLeft() {
        return livesLeft;
    }

    public void sort() {
        Collections.sort(guessed);
    }

    public static void guess(TextChannel channel, String guess) {
        if (!GAMES.containsKey(channel)) {
            channel.sendMessage("To start a game type: !start hangman").queue();      //can't be !play because it's already in use at music commands.
            return;
        }

        Hangman game = GAMES.get(channel);

        if (guess.length() == 1) {
            if (!game.getGuessed().contains(guess.charAt(0))) {
                if (!game.getWord().contains(guess)) {
                    channel.sendMessage("The word doesn't contain letter " + guess + ".").queue();
                    game.reduceLive();
                }
                game.addGuessed(guess.toLowerCase().charAt(0));
                game.addGuessed(guess.toLowerCase().charAt(0));
            } else {
                channel.sendMessage(guess + " has already been guessed.").queue();
            }
        } else {
            if (game.getWord().equalsIgnoreCase(guess)) {
                channel.sendMessage("Great job! " + game.getWord() + " was the right word.").queue();
                GAMES.remove(channel);
                return;
            } else {
                channel.sendMessage(guess + " was not the word.").queue();
                game.reduceLive();
            }

        }
        if (game.getLivesLeft() == 0) {
            game.finish();
            return;
        }

        game.sendWord();
    }

    public void finish() {
        channel.sendMessage("You ran out of lives. The word was " + word + ".").queue();
        GAMES.remove(channel);
    }

    public void sendWord() {
        boolean lastUnknown = false;
        String sendableWord = "";
        for (int i = 0; i < word.length(); i++) {
            if (guessed.contains(word.charAt(i))) {
                sendableWord += word.charAt(i);
                lastUnknown = false;
            } else {
                if (lastUnknown) {
                    sendableWord += " ";
                }
                sendableWord += "-";
                lastUnknown = true;
            }

        }

        String guessed = "";
        boolean first = true;
        for (char c : this.guessed) {
            if (first) {
                first = false;
            } else {
                guessed += ", ";
            }
            guessed += c;
        }

        channel.sendMessage("Word: " + sendableWord + " Lives left: " + livesLeft + "\nGuessed letters: " + guessed).queue();
    }
}
