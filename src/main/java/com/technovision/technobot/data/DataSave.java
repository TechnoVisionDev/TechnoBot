package com.technovision.technobot.data;

import org.json.JSONObject;

/**
 * Implementation details for saving, loading,
 * and accessing a JSON configuration file.
 *
 * @author Sparky
 */
public interface DataSave {

    /**
     * Returns the JSONObject currently cached in this instance.
     *
     * @return The {@link JSONObject}
     * @since 1.0
     */
    JSONObject getJson();

    /**
     * Loads the file. Overwrites current JSONObject!
     *
     * @see DataSave#save()
     * @since 1.0
     */
    void load();

    /**
     * Saves to the file. Overwrites file!
     *
     * @see DataSave#load()
     * @since 1.0
     */
    void save();
}
