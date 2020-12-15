package com.technovision.technobot.data;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * JSON Configuration
 *
 * @author Sparky
 */
public class Configuration implements DataSave {
    /**
     * The directory File.
     */
    private final File directory;
    private final String fileName;
    private final String filePath;
    /**
     * The cached JSON in this instance
     */
    private JSONObject json;

    public Configuration(String filePath, String fileName) {
        json = new JSONObject();

        this.fileName = fileName;
        this.filePath = filePath;

        directory = new File(filePath);

        if (!directory.exists() && !directory.mkdirs()) {
            // TODO: 7/14/2020 Logger#severe (see line 48/61)
        }
        load();
        save();
    }

    @Override
    public JSONObject getJson() {
        return json;
    }

    @Override
    public void load() {
        StringBuilder jsonStr = new StringBuilder();

        File t = new File(directory.getPath() + "/" + fileName);
        if (!t.exists()) {
            try {
                t.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Scanner scanner = new Scanner(new File(directory.getPath() + "/" + fileName));
            scanner.reset();

            while (scanner.hasNextLine()) {
                jsonStr.append(scanner.nextLine());
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            // TODO: 7/14/2020 Logger#severe(new RuntimeException("An internal error occured while loading "+directory.getPath()+fileName+"!", e));
            e.printStackTrace();
            jsonStr.append("{}");
        }

        if (jsonStr.toString().equalsIgnoreCase("")) jsonStr.append("{}");

        json = new JSONObject(jsonStr.toString());
    }

    @Override
    public void save() {
        try {
            FileWriter writer = new FileWriter(directory.getPath() + "/" + fileName);
            writer.write(json.toString(4));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO: 7/14/2020 Logger#severe(new RuntimeException("An internal error occurred while saving "+directory.getPath()+fileName+"!", e));
            e.printStackTrace();
        }
    }
}
