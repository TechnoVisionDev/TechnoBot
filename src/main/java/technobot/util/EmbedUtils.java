package technobot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class storing helpful methods for embeds.
 *
 * @author Technovision
 */
public class EmbedUtils {

    /** Custom Emojis. */
    public static final String GREEN_TICK = "<:green_tick:800555917472825418>";
    public static final String BLUE_TICK = "<:blue_tick:800623774293819413>";
    public static final String RED_X = "<:red_x:800554807164665916>";
    public static final String BLUE_X = "<:blue_x:800623785736798228>";

    /**
     * Custom Emojis - ID ONLY.
     */
    public static final String YES_ID = "800673964925386753";
    public static final String NO_ID = "800673984794329098";

    /**
     * Quickly creates a simple error embed.
     *
     * @param errorMessage message to be displayed.
     * @return completed error embed.
     */
    public static @NotNull MessageEmbed createError(String errorMessage) {
        return new EmbedBuilder()
                .setColor(EmbedColor.ERROR.color)
                .setDescription(RED_X + " " + errorMessage)
                .build();
    }

    /**
     * Quickly creates a simple default embed.
     *
     * @param message text to be displayed.
     * @return completed default embed.
     */
    public static @NotNull MessageEmbed createDefault(String message) {
        return new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setDescription(message)
                .build();
    }

    /**
     * Quickly creates a simple success embed.
     *
     * @param message text to be displayed.
     * @return completed success embed.
     */
    public static @NotNull MessageEmbed createSuccess(String message) {
        return new EmbedBuilder()
                .setColor(EmbedColor.SUCCESS.color)
                .setDescription(message)
                .build();
    }

    /**
     * Converts JSON code to an embed message.
     *
     * @param jsonText String containing JSON code.
     * @return a completed EmbedBuilder.
     * @throws IllegalStateException Invalid JSON code.
     */
    public static @NotNull
    EmbedBuilder fromJson(@NotNull String jsonText) throws IllegalStateException, IllegalArgumentException {
        DataObject json = DataObject.fromJson(jsonText);
        EmbedBuilder embed = new EmbedBuilder();

        if (json.hasKey("title")) embed.setTitle(json.getString("title"), json.getString("url", null));
        if (json.hasKey("description")) embed.setDescription(json.getString("description"));
        if (json.hasKey("color")) embed.setColor(json.getInt("color"));

        if (json.isType("thumbnail", DataType.OBJECT)) {
            embed.setThumbnail(json.getObject("thumbnail").getString("url", null));
        } else {
            embed.setThumbnail(json.getString("thumbnail", null));
        }

        if (json.isType("image", DataType.OBJECT)) {
            embed.setImage(json.getObject("image").getString("url", null));
        } else {
            embed.setImage(json.getString("image", null));
        }

        if (json.isType("author", DataType.OBJECT)) {
            DataObject author = json.getObject("author");
            embed.setAuthor(
                    author.getString("name", null),
                    author.getString("url", null),
                    author.getString("icon_url", null));
        } else {
            embed.setAuthor(json.getString("author", null));
        }

        if (json.isType("footer", DataType.OBJECT)) {
            DataObject footer = json.getObject("footer");
            embed.setFooter(
                    footer.getString("text", null),
                    footer.getString("icon_url", null));
        } else {
            embed.setFooter(json.getString("footer", null));
        }

        if (json.hasKey("fields")) {
            DataArray fields = json.getArray("fields");
            for (int i = 0; i < fields.length(); i++) {
                DataObject field = fields.getObject(i);
                embed.addField(
                        field.getString("name", null),
                        field.getString("value", null),
                        field.getBoolean("inline", false)
                );
            }
        }

        if (embed.isEmpty()) throw new IllegalStateException();
        return embed;
    }
}
