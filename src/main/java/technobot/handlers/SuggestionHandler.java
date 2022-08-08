package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.GuildData;
import technobot.data.cache.Suggestion;
import technobot.util.embeds.EmbedUtils;

import java.util.List;

import static technobot.util.Localization.get;

/**
 * Handles the suggestion board for a guild.
 *
 * @author TechnoVision
 */
public class SuggestionHandler {

    private final TechnoBot bot;
    private final Bson filter;

    private final Guild guild;
    private Suggestion suggestions;

    /**
     * Sets up the local cache for this guild's suggestions from MongoDB.
     *
     * @param bot   Instance of TechnoBot shard.
     * @param guild Instance of the guild this handler is for.
     */
    public SuggestionHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;

        // Get POJO objects from database
        filter = Filters.eq("guild", guild.getIdLong());
        this.suggestions = bot.database.suggestions.find(filter).first();
        if (suggestions == null) {
            suggestions = new Suggestion(guild.getIdLong());
            bot.database.suggestions.insertOne(suggestions);
        }
    }

    /**
     * Sets the suggestion board channel.
     *
     * @param channelID ID of the new channel.
     */
    public void setChannel(long channelID) {
        suggestions.setChannel(channelID);
        bot.database.suggestions.updateOne(filter, Updates.set("channel", channelID));
    }

    /**
     * Adds a suggestion message to the list.
     *
     * @param messageID the ID of the suggestion embed.
     */
    public void add(long messageID, long author) {
        // Update local cache
        suggestions.getMessages().add(messageID);
        suggestions.getAuthors().add(author);
        suggestions.setNumber(suggestions.getNumber() + 1);

        // Update MongoDB data file
        bot.database.suggestions.updateOne(filter, Updates.push("messages", messageID));
        bot.database.suggestions.updateOne(filter, Updates.push("authors", author));
        bot.database.suggestions.updateOne(filter, Updates.inc("number", 1));
    }

    /**
     * Resets all suggestion data locally and in MongoDB.
     */
    public void reset() {
        suggestions = new Suggestion(guild.getIdLong());
        bot.database.suggestions.replaceOne(filter, suggestions);
    }

    /**
     * Checks if suggestion board has a channel set.
     *
     * @return true if channel set, otherwise false.
     */
    public boolean isSetup() {
        return suggestions.getChannel() != null;
    }

    /**
     * Checks if anonymous mode is turned on/off.
     *
     * @return anonymous mode boolean.
     */
    public boolean isAnonymous() {
        return suggestions.isAnonymous();
    }

    /**
     * Checks if response DMs are enabled/disabled.
     *
     * @return response DMs boolean.
     */
    public boolean hasResponseDM() {
        return suggestions.isResponseDM();
    }

    /**
     * Gets the number of the next suggestion.
     *
     * @return Next suggestion number.
     */
    public long getNumber() {
        return suggestions.getNumber();
    }

    /**
     * Gets the channel ID of the suggestion board.
     *
     * @return ID of the suggestion channel.
     */
    public Long getChannel() {
        return suggestions.getChannel();
    }

    /**
     * Gets the list of suggestion message IDs.
     *
     * @return list of suggestion message IDs.
     */
    public List<Long> getMessages() {
        return suggestions.getMessages();
    }

    /**
     * Switches on/off anonymous mode and returns the result.
     *
     * @return the resulting boolean of toggling anonymous mode.
     */
    public boolean toggleAnonymous() {
        boolean result = !suggestions.isAnonymous();
        suggestions.setAnonymous(result);
        bot.database.suggestions.updateOne(filter, Updates.set("is_anonymous", result));
        return result;
    }

    /**
     * Switches on/off response DMs.
     *
     * @return the resulting boolean of toggling DMs.
     */
    public boolean toggleResponseDM() {
        boolean result = !suggestions.isResponseDM();
        suggestions.setResponseDM(result);
        bot.database.suggestions.updateOne(filter, Updates.set("response_dm", result));
        return result;
    }

    /**
     * Responds to a suggestion by editing the embed and responding to the author.
     *
     * @param event         The slash command event that triggered this method.
     * @param id            the id number of the suggestion to respond to.
     * @param reason        the reason passed in by user.
     * @param response      the response message (approve, deny, etc).
     * @param responseColor the color of the response message.
     */
    public void respond(SlashCommandInteractionEvent event, int id, String reason, String response, int responseColor) {
        try {
            SuggestionHandler suggestionHandler = GuildData.get(event.getGuild()).suggestionHandler;
            TextChannel channel = event.getGuild().getTextChannelById(suggestionHandler.getChannel());
            if (channel == null) {
                throw new NullPointerException();
            }

            // Edit suggestion embed
            Message suggestionMessage = channel.retrieveMessageById(suggestionHandler.getMessages().get(id)).complete();
            MessageEmbed embed = suggestionMessage.getEmbeds().get(0);
            MessageEmbed editedEmbed = new EmbedBuilder()
                    .setAuthor(embed.getAuthor().getName(), embed.getUrl(), embed.getAuthor().getIconUrl())
                    .setTitle(
                            get(s -> s.suggestions.respond.title, id + 1, response) + ""
                    )
                    .setDescription(embed.getDescription())
                    .addField(get(s -> s.suggestions.respond.reason, event.getUser().getAsTag()), reason, false)
                    .setColor(responseColor)
                    .build();
            suggestionMessage.editMessageEmbeds(editedEmbed).queue();

            String lowercaseResponse = response.toLowerCase();
            String text = get(s -> s.suggestions.respond.message, id + 1, lowercaseResponse);
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();

            // DM Author if response DMs are turned on
            if (suggestions.isResponseDM()) {
                User author = event.getJDA().getUserById(suggestions.getAuthors().get(id));
                if (author != null) {
                    author.openPrivateChannel().queue(dm -> {
                        String dmText = get(s -> s.suggestions.respond.dmText, lowercaseResponse, event.getUser().getAsTag());
                        dm.sendMessage(dmText).setEmbeds(editedEmbed).queue();
                    });
                }
            }

        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            // Invalid ID format
            String text = get(s -> s.suggestions.respond.noSuggestion);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
        } catch (ErrorResponseException | NullPointerException e) {
            // Invalid channel
            String text = get(s -> s.suggestions.respond.noMessage);
            event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
        }
    }

    /**
     * Suggestion Response Types.
     * Includes the correct color scheme and wording.
     */
    public enum SuggestionResponse {
        APPROVE(0xd2ffd0),
        DENY(0xffd0ce),
        CONSIDER(0xfdff91),
        IMPLEMENT(0x91fbff);

        public final int color;

        SuggestionResponse(int color) {
            this.color = color;
        }
    }
}
