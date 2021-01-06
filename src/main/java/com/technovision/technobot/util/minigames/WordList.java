package com.technovision.technobot.util.minigames;

import com.technovision.technobot.data.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reads words from json file
 *
 * @author Adex
 */
public class WordList {
    private static final ArrayList<String> words = new ArrayList<>();

    private static final Configuration config = new Configuration("data/config/", "words.json");

    static {
        try {
            JSONObject jsonObject = config.getJson();
            JSONArray jsonArray = (JSONArray) jsonObject.get("words");

            for (Object word : jsonArray) {
                words.add(word.toString().toLowerCase());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getWord() {
        return words.get(ThreadLocalRandom.current().nextInt(words.size()));
    }
}
