package technobot.data;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import technobot.data.cache.Config;
import technobot.data.cache.Leveling;
import technobot.data.cache.Suggestion;
import technobot.data.cache.moderation.Moderation;
import technobot.data.cache.Starboard;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Manages data between the bot and the MongoDB database.
 *
 * @author TechnoVision
 */
public class Database {

    /** Collections */
    public @NotNull MongoCollection<Suggestion> suggestions;
    public @NotNull MongoCollection<Moderation> moderation;
    public @NotNull MongoCollection<Starboard> starboard;
    public @NotNull MongoCollection<Leveling> leveling;
    public @NotNull MongoCollection<Config> config;

    /**
     * Connect to database using MongoDB URI and
     * initialize any collections that don't exist.
     *
     * @param uri MongoDB uri string.
     */
    public Database(String uri) {
        // Setup MongoDB database with URI.
        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .codecRegistry(codecRegistry)
                .build();
        MongoClient mongoClient = MongoClients.create(clientSettings);
        MongoDatabase database = mongoClient.getDatabase("TechnoBot");

        // Initialize collections if they don't exist.
        suggestions = database.getCollection("suggestions", Suggestion.class);
        moderation = database.getCollection("moderation", Moderation.class);
        starboard = database.getCollection("starboard", Starboard.class);
        leveling = database.getCollection("leveling", Leveling.class);
        config = database.getCollection("config", Config.class);
    }
}
