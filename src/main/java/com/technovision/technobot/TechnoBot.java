package com.technovision.technobot;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import com.technovision.technobot.commands.CommandRegistry;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.listeners.*;
import com.technovision.technobot.listeners.managers.*;
import com.technovision.technobot.logging.AutoModLogger;
import com.technovision.technobot.logging.Loggable;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.BotRegistry;
import com.technovision.technobot.util.ImageProcessor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.imageio.ImageIO;
import javax.security.auth.login.LoginException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Multi-Purpose Bot for TechnoVision Discord.
 * @author TechnVision
 * @author Sparky
 * @version 0.5
 */
@Loggable(display = "TechnoBot")
public class TechnoBot {

    public static Logger logger;
    private final JDA jda;
    private final BotRegistry registry;
    private final SuggestionManager suggestionManager;
    private final YoutubeManager youtubeManager;
    private final EconManager econManager;
    private final AutoModLogger autoModLogger;
    private final MongoDatabase mongoDatabase;
    private final LevelManager levelManager;
    private final MusicManager musicManager;
    private final TicketManager ticketManager;
    private final AutoPastebinManager autoPastebinManager;
    private final StarboardManager starboardManager;
    private final Configuration config = new Configuration("data/config/","botconfig.json"){
        @Override
        public void load() {
            super.load();
            if(!getJson().has("token")) getJson().put("token", "");
            if(!getJson().has("guildlogs-webhook")) getJson().put("guildlogs-webhook", "");
            if(!getJson().has("youtube-api-key")) getJson().put("youtube-api-key", "");
            if(!getJson().has("mongo-client-uri")) getJson().put("mongo-client-uri", "");
            if (!getJson().has("github-token")) getJson().put("github-token", "");
        }
    };

    /**
     * Public TechnoBot Constructor.
     * Initializes the JDABuilder and Bot Registry.
     * @throws LoginException Malformed bot token.
     */
    public TechnoBot() throws LoginException, InterruptedException {
        registry = new BotRegistry();

        MongoClientURI clientURI = new MongoClientURI(getBotConfig().getJson().getString("mongo-client-uri"));
        MongoClient mongoClient = new MongoClient(clientURI);
        mongoDatabase = mongoClient.getDatabase("TechnoBot");

        JDABuilder builder = JDABuilder.createDefault(getToken());
        builder.setStatus(OnlineStatus.ONLINE).setActivity(Activity.watching("TechnoVisionTV"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS);
        jda = builder.build().awaitReady();
        suggestionManager = new SuggestionManager(this);
        youtubeManager = new YoutubeManager(this);
        econManager = new EconManager();
        autoModLogger = new AutoModLogger();
        levelManager = new LevelManager(this);
        musicManager = new MusicManager(this);
        ticketManager = new TicketManager(this);
        starboardManager = new StarboardManager();
        autoPastebinManager = new AutoPastebinManager(this);
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public AutoModLogger getAutoModLogger() {
        return autoModLogger;
    }

    public SuggestionManager getSuggestionManager() {
        return suggestionManager;
    }

    public YoutubeManager getYoutubeManager() {
        return youtubeManager;
    }

    public EconManager getEconomy() {
        return econManager;
    }

    public TicketManager getTicketManager() {
        return ticketManager;
    }

    public AutoPastebinManager getAutoPastebinManager() {
        return autoPastebinManager;
    }

    /**
     * Accessor for the bot's JSON configuration file.
     * @return JSON config file.
     */
    public Configuration getBotConfig() {
        return config;
    }

    /**
     * Accessor for the console logger.
     * @return logger.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Accessor for the JDA API instance.
     * @return JDA API instance.
     */
    public JDA getJDA() {
        return jda;
    }

    /**
     * Accessor for the bot registry
     * @return Bot registry
     */
    public BotRegistry getRegistry() {
        return registry;
    }

    /**
     * Accessor for the securely stored bot token
     * @return Bot token
     * @deprecated Probably not a good idea to have a getter for a token ;)
     */
    @Deprecated
    private String getToken() {
        return getBotConfig().getJson().getString("token");
    }

    /** Download and store images needed for rank-card creation */
    private void setupImages() {
        try {
            System.setProperty("http.agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2");
            BufferedImage base = ImageIO.read(new URL("https://i.imgur.com/HktDs1Y.png"));
            BufferedImage outline = ImageIO.read(new URL("https://i.imgur.com/oQhl6yW.png"));
            BufferedImage background = ImageIO.read(new URL("https://i.imgur.com/vGmvhZg.jpg"));
            File file = new File("data/images/rankCardOutline.png");
            if (!file.exists()) {
                file.mkdirs();
            }
            ImageProcessor.saveImage("data/images/rankCardBase.png", base);
            ImageProcessor.saveImage("data/images/rankCardOutline.png", outline);
            ImageProcessor.saveImage("data/images/rankCardBackground.png", background);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * initialize bot and register listeners.
     * @param args ignored
     */
    public static void main(String[] args)  {
        TechnoBot bot;
        try {
            bot = new TechnoBot();
            bot.logger = new Logger(bot);
        } catch(LoginException | InterruptedException e) { throw new RuntimeException(e); }

        bot.getLogger().log(Logger.LogLevel.INFO, "Bot Starting...");
        bot.setupImages();
        GuildMemberEvents.loadJoinMessage();

        new CommandRegistry(bot);
        bot.getRegistry().registerEventListeners(new AutomodListener(bot), new ExtrasEventListener(), bot.musicManager, new GuildLogEventListener(bot), bot.levelManager, new CommandEventListener(bot), new GuildMemberEvents(), bot.starboardManager, bot.ticketManager, bot.autoPastebinManager);
        bot.getRegistry().addListeners(bot.getJDA());
    }
}
