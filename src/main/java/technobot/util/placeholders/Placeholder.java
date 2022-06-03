package technobot.util.placeholders;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Placeholder Class
 * Utility class for creating placeholders and parsing strings with them.
 *
 * @author Sparky
 * @see PlaceholderFactory
 * @see Placeholder#parse(String)
 */
public final class Placeholder {

    /**
     * REGEX for placeholders in strings.
     */
    private static final String REGEX = "\\{[a-z0-9-_]+}";

    /**
     * Map of placeholders and their replacements.
     */
    private final HashMap<String, String> map;

    /**
     * Pattern for the placeholder. Uses REGEX to compile.
     */
    private final @NotNull Pattern pattern;

    /**
     * Creates a new placeholder and initializes fields.
     */
    public Placeholder() {
        map = new HashMap<>();
        pattern = Pattern.compile(REGEX);
    }

    /**
     * Adds a placeholder and replacement to the global map.
     *
     * @param key The placeholder to replace.
     * @param value The replacement.
     */
    public void add(String key, String value) {
        map.put(key, value);
    }

    /**
     * The main method of the class that sets placeholders in `s` and returns the new string.
     * @return null if input is null.
     */
    public String parse(String s) {
        if (s == null) { return null; }
        Matcher matcher = pattern.matcher(s);

        String str = s;
        while (matcher.find()) {
            String res = matcher.group();
            res = res.substring(1, res.length() - 1);

            if (map.containsKey(res)) {
                str = str.replaceFirst("\\{" + res + "}", map.get(res));
            }
        }
        return str;
    }
}
