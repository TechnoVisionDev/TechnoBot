package com.technovision.technobot.util.minigames;

import com.technovision.technobot.data.Configuration;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Discord Minigame
 *
 * @author Adex
 */
public class Hangman {

    public static final HashMap<Long, Hangman> GAMES = new HashMap<>();

    private static final Configuration saveConfig = new Configuration("data/config/", "hangmanSave.json");

    private final String word;
    private final Long userID;
    private final String name;
    //private final User user;
    private final SortedSet<Character> guessed;
    private int livesLeft;


    private Hangman(User user) {
        word = WordList.getWord();
        livesLeft = 10;
        userID = user.getIdLong();
        name = user.getName();
        //this.user = user;
        guessed = new TreeSet<>();
    }

    private Hangman(Long userID, String name, String word, int lives, SortedSet<Character> guessed) {
        this.userID = userID;
        this.name = name;
        this.word = word;
        livesLeft = lives;
        this.guessed = guessed;
    }

    public static void startGame(User user, TextChannel channel) {
        Long userID = user.getIdLong();
        if (GAMES.containsKey(userID)) {
            channel.sendMessage("Your last game ended. The old word was " + GAMES.get(userID).word + ".").queue();
        }
        GAMES.put(userID, new Hangman(user));
        GAMES.get(userID).sendWord(channel);
    }

    public static void guess(TextChannel channel, User user, String guess) {

        Hangman game = GAMES.get(user.getIdLong());
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

    public static void save() {
        JSONObject json = new JSONObject();
        int i = 1;

        for (Map.Entry<Long, Hangman> entry : GAMES.entrySet()) {
            JSONObject game = new JSONObject();
            Hangman hangmanGame = entry.getValue();

            //simple data
            game.put("userID", hangmanGame.userID);
            game.put("name", hangmanGame.name);
            game.put("word", hangmanGame.word);
            game.put("lives", hangmanGame.livesLeft);

            //adding guessed letters
            String guesses = "";
            for (char c : hangmanGame.guessed) {
                guesses += c;
            }

            game.put("guesses", guesses);
            json.put(Integer.toString(i), game);
            i++;
        }

        //writing actual file
        try {
            FileWriter fileWriter = new FileWriter(String.valueOf(saveConfig.getJson()));

            fileWriter.write(json.toString());
            fileWriter.flush();
            fileWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {

        try {
            JSONObject jsonObject = saveConfig.getJson();

            for (int i = 1; jsonObject.toMap().containsKey("" + i); i++) {
                JSONObject game = (JSONObject) jsonObject.get("" + i);

                Long userID = (Long) game.get("userID");
                String name = game.get("name").toString();
                String word = game.get("word").toString();
                int lives = (int) (long) game.get("lives");

                SortedSet<Character> guesses = new TreeSet<>();
                String stringifiedGuesses = game.get("guesses").toString();
                for (int i2 = 0; i2 < stringifiedGuesses.length(); i2++) {
                    guesses.add(stringifiedGuesses.charAt(i2));
                }

                GAMES.put(userID, new Hangman(userID, name, word, lives, guesses));
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public SortedSet<Character> getGuessed() {
        return guessed;
    }

    /*
     * Adds guessed character to the list and sorts it into alphabetical order.
     * */
    public void addGuessed(char guessed) {
        this.guessed.add(guessed);
    }

    public String getWord() {
        return word;
    }

    /*
     * Sends the default game message.
     * */

    public int getLivesLeft() {
        return livesLeft;
    }

    /*
     * Removes this game from active ones.
     * */
    public void finish() {
        GAMES.remove(userID);
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

        //channel.sendMessage(user.getName() + " Word: " + sendableWord + " Lives left: " + livesLeft + "\nGuessed letters: " + guessed).queue();
        channel.sendMessage(name + " Word: " + sendableWord + " Lives left: " + livesLeft + "\nGuessed letters: " + guessed).queue();
    }
}
