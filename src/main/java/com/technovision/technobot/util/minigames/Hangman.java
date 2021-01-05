package com.technovision.technobot.util.minigames;

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

    public static final HashMap<Long, Hangman> GAMES = new HashMap<>();

    private final String word;
    private int livesLeft;
    private final Long userID;
    private final String name;
    //private final User user;
    private final ArrayList<Character> guessed;


    private Hangman(User user) {
        word = WordList.getWord();
        livesLeft = 10;
        userID = user.getIdLong();
        name = user.getName();
        //this.user = user;
        guessed = new ArrayList<>();
    }

    public static void startGame(User user, TextChannel channel) {
        Long userID = user.getIdLong();
        GAMES.remove(userID);
        GAMES.put(userID, new Hangman(user));
        GAMES.get(userID).sendWord(channel);
        //GAMES.remove(user);
        //GAMES.put(user, new Hangman(user));
        //GAMES.get(user).sendWord(channel);
    }

    public ArrayList<Character> getGuessed() {
        return guessed;
    }

    /*
     * Adds guessed character to the list and sorts it into alphabetical order.
     * */
    public void addGuessed(char guessed) {
        this.guessed.add(guessed);
        Collections.sort(this.guessed);
    }

    public String getWord() {
        return word;
    }

    public int getLivesLeft() {
        return livesLeft;
    }

    public static void guess(TextChannel channel, User user, String guess) {

        Hangman game = GAMES.get(user.getIDLong());
        //Hangman game = GAMES.get(user);

        if (guess.length() == 1) { //letter
            if (!game.getGuessed().contains(guess.charAt(0))) {  //not guessed
                if (!game.getWord().contains(guess)) {
                    channel.sendMessage("The word doesn't contain letter " + guess + ".").queue();  //in word
                    game.livesLeft--;
                }
                game.addGuessed(guess.charAt(0));
            } else {
                channel.sendMessage(guess + " has already been guessed.").queue();  //already guessed
            }
        } else {  //word
            if (game.getWord().equals(guess)) {  //right
                channel.sendMessage("Great job! " + game.getWord() + " was the right word.").queue();
                game.finish();
                return;
            } else {  //wrong
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
    
    /*
     * Removes this game from active ones.
     * */
    public void finish() {
        GAMES.remove(userID);
    }
    
    /*
     * Sends the default game message.
     * */

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

        //channel.sendMessage(user.getName() + " Word: " + sendableWord + " Lives left: " + livesLeft + "\nGuessed letters: " + guessed).queue();
        channel.sendMessage(name + " Word: " + sendableWord + " Lives left: " + livesLeft + "\nGuessed letters: " + guessed).queue();
    }
}
