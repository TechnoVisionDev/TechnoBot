package com.technovision.technobot.util.minigames;

import java.io.*;

import org.json.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Reads words from json file
 * @author Adex
 */
public class WordList {
    private static final ArrayList<String> words = new ArrayList<>();

    static{
        try {
            Object obj = new JSONParser().parse(new FileReader("C:\\Users\\adex\\Desktop\\AdexBot\\src\\main\\resources\\words.json"));      //Change this
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonArray = (JSONArray) ((JSONObject) obj).get("words");
            for(Object word : words){
                words.add(word.toString().toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getWord(){
        return words.get(new Random().nextInt(words.size()));
    }
}
