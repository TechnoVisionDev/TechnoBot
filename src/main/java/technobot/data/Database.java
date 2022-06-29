package technobot.data;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import technobot.data.cache.*;
import technobot.data.cache.moderation.Moderation;

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
    public @NotNull MongoCollection<Greetings> greetings;
    public @NotNull MongoCollection<Economy> economy;
    public @NotNull MongoCollection<Item> items;

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
        greetings = database.getCollection("greetings", Greetings.class);
        economy = database.getCollection("economy", Economy.class);
        items = database.getCollection("items", Item.class);

        Bson guildIndex = Indexes.descending("guild");
        suggestions.createIndex(guildIndex);
        moderation.createIndex(guildIndex);
        starboard.createIndex(guildIndex);
        config.createIndex(guildIndex);
        greetings.createIndex(guildIndex);
        leveling.createIndex(Indexes.compoundIndex(guildIndex, Indexes.descending("user")));
        economy.createIndex(Indexes.compoundIndex(guildIndex, Indexes.descending("user")));
    }
}
