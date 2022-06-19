package technobot.util.localization;

import com.google.gson.Gson;
import kotlin.text.Regex;
import technobot.handlers.economy.EconomyReply;
import technobot.util.embeds.EmbedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Handles all stuff related to localization.
 *
 * @author TheOnlyTails
 */
public class Localization {
    public static Locale currentLocale = Localization.Locale.EN_US;

    /**
     * Fetches a localization value from the localization file, based on the locale.
     *
     * @param query  The query for the localization value.
     * @param locale The locale to fetch the value from.
     * @return The localization value.
     * @throws NullPointerException If no localization file is found for the locale.
     */
    public static <T> T get(Function<LocalizationSchema, T> query, Locale locale, Object... args) throws NullPointerException {
        try (var inputStream = Localization.class.getClassLoader().getResourceAsStream("localization/" + locale.locale + ".json")) {
            if (inputStream == null) throw new IOException();

            var reader = new BufferedReader(new InputStreamReader(inputStream));
            var schema = new Gson().fromJson(reader, LocalizationSchema.class);

            final var value = query.apply(schema);
            if (args.length > 0 && value instanceof String template) {
                //noinspection unchecked
                return (T) format(template, args);
            } else {
                return value;
            }
        } catch (IOException e) {
            throw new NullPointerException("Could not load localization file for locale " + locale.locale);
        }
    }

    /**
     * Fetches a localization value from the localization file, based on the current locale.
     *
     * @param query The query for the localization value.
     * @return The localization value.
     * @throws NullPointerException If no localization file is found for the locale.
     */
    public static <T> T get(Function<LocalizationSchema, T> query, Object... args) throws NullPointerException {
        return get(query, currentLocale, args);
    }

    public static String format(String template, Object... args) {
        // replace all special constants, to save on args
        template = template.replaceAll("\\{red_x\\}", EmbedUtils.RED_X)
                .replaceAll("\\{green_tick\\}", EmbedUtils.GREEN_TICK)
                .replaceAll("\\{blue_x\\}", EmbedUtils.BLUE_X)
                .replaceAll("\\{blue_tick\\}", EmbedUtils.BLUE_TICK)
                .replaceAll("\\{mention\\}", "<@{}>")
                .replaceAll("\\{role\\}", "<@&{}>")
                .replaceAll("\\{channel\\}", "<#{}>")
                .replaceAll("\\{currency\\}", "\uD83E\uDE99");

        var argsList = Arrays.stream(args).map(Object::toString).toList();
        var templateArgsCount = Pattern.compile("\\{\\}").matcher(template).groupCount();

        switch (Integer.compare(argsList.size(), templateArgsCount)) {
            case -1 -> {
                System.err.printf("""
                Too few arguments (%d) provided for template: %s
                All unused template slots will not be filled.
                """, argsList.size(), template);
                for (final var arg : argsList) {
                    template = template.replace("{}", arg);
                }
            }
            case 0 -> {
                for (final var arg : argsList) {
                    template = template.replace("{}", arg);
                }
            }
            case 1 -> {
                System.err.printf("Too many arguments (%d) provided for template: %s%n", argsList.size(), template);
                for (final var arg : argsList.subList(0, templateArgsCount - 1)) {
                    template = template.replace("{}", arg);
                }
            }
        }

        return template;
    }

    public enum Locale {
        EN_US("en_US", "English (US)"),
        EN_GB("en_GB", "English (UK)"),
        HE_IL("he_IL", "Hebrew"),
        ;

        public final String locale;
        public final String displayName;

        Locale(String locale, String displayName) {
            this.locale = locale;
            this.displayName = displayName;
        }
    }
}
