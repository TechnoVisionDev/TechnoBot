package technobot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import technobot.commands.CommandRegistry;
import technobot.data.Database;
import technobot.data.GuildData;
import technobot.listeners.*;

import javax.security.auth.login.LoginException;

public class TechnoBot {

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
        //Setup Database
        config = Dotenv.load();
        database = new Database(config.get("DATABASE"));

        //Build JDA shards
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.playing("/help"));
        builder.setChunkingFilter(ChunkingFilter.ALL);
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableCache(CacheFlag.ONLINE_STATUS);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_PRESENCES);
        builder.addEventListeners(new CommandRegistry(this));
        shardManager = builder.build();
        GuildData.init(this);

        //Register Listeners
        buttonListener = new ButtonListener();
        musicListener = new MusicListener();
        shardManager.addEventListener(
                new GuildListener(),
                buttonListener,
                musicListener,
                new StarboardListener(),
                new LevelingListener(this));
    }

    /**
     * Initialize CivBot.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        TechnoBot bot;
        try {
            bot = new TechnoBot();
        } catch (LoginException e) {
            System.out.println("ERROR: Provided bot token is invalid!");
        }
    }
}
