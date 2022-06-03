package technobot.data;

import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.handlers.ModerationHandler;
import technobot.handlers.StarboardHandler;
import technobot.handlers.MusicHandler;
import technobot.handlers.SuggestionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Local cache of settings and data for each guild. Interacts with various commands, modules,
 * and the MongoDB database to always keep data updated locally.
 *
 * @author TechnoVision, Sparky
 */
public class GuildData {

    /** The GuildSettings instance for each guild. */
    private static final Map<Long, GuildData> guilds = new HashMap<>();

    /** Static utility variables. */
    private static TechnoBot bot;
    private static boolean initialized;

    /** Local memory caches. */
    public MusicHandler musicHandler;
    public SuggestionHandler suggestionHandler;
    public ModerationHandler moderationHandler;
    public StarboardHandler starboardHandler;

    /**
     * Represents the local memory cache of guild data stored in the MongoDB databases.
     *
     * @param guild the guild these settings represent.
     */
    private GuildData(Guild guild) {
        // Setup caches
        musicHandler = null;
        suggestionHandler = new SuggestionHandler(bot, guild);
        moderationHandler = new ModerationHandler(bot, guild);
        starboardHandler = new StarboardHandler(bot, guild);
    }

    /**
     * Removes a GuildSettings instance from settings HashMap.
     *
     * @param guild specified guild.
     */
    public static void removeGuild(@NotNull Guild guild) {
        guilds.remove(guild.getIdLong());
    }

    /**
     * Retrieves the GuildSettings instance for a given guild.
     * If it doesn't exist, it will create one.
     *
     * @param guild the discord guild.
     * @return GuildSettings for specified guild.
     */
    public static GuildData get(@NotNull Guild guild) {
        if (!guilds.containsKey(guild.getIdLong())) {
            return create(guild);
        }
        return guilds.get(guild.getIdLong());
    }

    /**
     * This should ask for the webserver to get or create a settings object.
     * For testing purposes, this will instantiate GuildData.
     *
     * @param guild The guild to fetch from webserver for.
     * @return the guild data object for the newly created guild.
     */
    private static GuildData create(@NotNull Guild guild) {
        GuildData data = new GuildData(guild);
        guilds.put(guild.getIdLong(), data);
        return data;
    }

    /**
     * Initializes the GuildSettings with an instance of TechnoBot if it hasn't already been initializes.
     *
     * @param bot instance of TechnoBot.
     */
    public static void init(final TechnoBot bot) {
        if (!initialized) {
            initialized = true;
            GuildData.bot = bot;
        }
    }
}
