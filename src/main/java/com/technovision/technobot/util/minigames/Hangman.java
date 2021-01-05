package com.adex.adexbot.minigames.hangman;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Discord Minigame
 *
 * @author Adex
 */
public class Hangman {

    public static final HashMap<User, Hangman> GAMES = new HashMap<>();

    private final String word;
    private int livesLeft;
    private final User user;
    private final ArrayList<Character> guessed;


    private Hangman(User user) {
        word = WordList.getWord();
        livesLeft = 10;
        this.user = user;
        guessed = new ArrayList<>();
    }

    public static void startGame(User user, TextChannel channel) {
        GAMES.remove(user);
        GAMES.put(user, new Hangman(user));
        GAMES.get(user).sendWord(channel);

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

    public int getLivesLeft() {
        return livesLeft;
    }

    public void sort() {
        Collections.sort(guessed);
    }

    public static void guess(TextChannel channel, User user, String guess) {

        Hangman game = GAMES.get(user);

        if (guess.length() == 1) {
            if (!game.getGuessed().contains(guess.charAt(0))) {
                if (!game.getWord().contains(guess)) {
                    channel.sendMessage("The word doesn't contain letter " + guess + ".").queue();
                    game.livesLeft--;
                }
                game.addGuessed(guess.toLowerCase().charAt(0));
            } else {
                channel.sendMessage(guess + " has already been guessed.").queue();
            }
        } else {
            if (game.getWord().equalsIgnoreCase(guess)) {
                channel.sendMessage("Great job! " + game.getWord() + " was the right word.").queue();
                game.finish();
                return;
            } else {
                channel.sendMessage(guess + " was not the word.").queue();
                game.livesLeft--;
            }

        }
        if (game.getLivesLeft() == 0) {
            channel.sendMessage("You ran out of lives. The word was " + game.getWord() + ".").queue();
            game.finish();
            return;
        }

        game.sendWord(channel);
    }

    public void finish() {
        GAMES.remove(user);
    }

    public void sendWord(TextChannel channel) {
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
