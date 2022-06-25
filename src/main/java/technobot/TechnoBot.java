package technobot;

import com.google.gson.Gson;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import technobot.commands.CommandRegistry;
import technobot.data.Database;
import technobot.data.GuildData;
import technobot.listeners.*;

import javax.security.auth.login.LoginException;

/**
 * Main class for TechnoBot Discord Bot.
 * Initializes shard manager, database, and listeners.
 *
 * @author TechnoVision
 */
public class TechnoBot {

    public Gson gson;
    public OkHttpClient httpClient;

    public final @NotNull Dotenv config;
    public final @NotNull ShardManager shardManager;
    public final @NotNull Database database;
    public final @NotNull ButtonListener buttonListener;
    public final @NotNull MusicListener musicListener;

    /**
     * Builds bot shards and registers commands and modules.
     *
     * @throws LoginException throws if bot token is invalid.
     */
    public TechnoBot() throws LoginException {
        //Setup HTTP tools
        gson = new Gson();
        httpClient = new OkHttpClient();

        //Setup Database
        config = Dotenv.configure().ignoreIfMissing().load();
        database = new Database(config.get("DATABASE", System.getenv("DATABASE")));

        //Build JDA shards
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN", System.getenv("TOKEN")));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("/help | technobot.app"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_PRESENCES);
        builder.addEventListeners(new CommandRegistry(this));
        shardManager = builder.build();
        GuildData.init(this);

        //Register Listeners
        buttonListener = new ButtonListener();
        musicListener = new MusicListener(config.get("SPOTIFY_CLIENT_ID"), config.get("SPOTIFY_TOKEN"));
        shardManager.addEventListener(
                new GuildListener(this),
                buttonListener,
                musicListener,
                new StarboardListener(),
                new LevelingListener(this),
                new GreetingListener(),
                new AfkListener());
    }

    /**
     * Initialize TechnoBot.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            TechnoBot bot = new TechnoBot();
        } catch (LoginException e) {
            System.out.println("ERROR: Provided bot token is invalid!");
        }
    }
}
