package technobot.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import technobot.data.GuildData;
import technobot.handlers.StarboardHandler;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Listens for reactions and updates starboard accordingly
 *
 * @author TechnoVision
 */
public class StarboardListener extends ListenerAdapter {

    /**
     * Emojis and colors for the Starboard.
     */
    public static final String STAR_EMOTE_1 = "\u2B50"; //:star:
    public static final String STAR_EMOTE_2 = "\uD83C\uDF1F"; //:star2:
    public static final String STAR_EMOTE_3 = "\uD83D\uDCAB"; //:dizzy:
    public static final int POST_COLOR = 0xfffa74;

    /**
     * Determines the correct star emoji based on a star count.
     *
     * @param stars The amount of stars a post has.
     * @return the correct star emoji to use.
     */
    private @NotNull String getStarEmoji(int stars) {
        String emoji = STAR_EMOTE_1;
        if (stars >= 10 && stars < 20) {
            emoji = STAR_EMOTE_2;
        } else if (stars >= 20) {
            emoji = STAR_EMOTE_3;
        }
        return emoji;
    }

    /**
     * Creates an embed post from a message for the starboard channel.
     *
     * @param msg The original message to pull data from.
     * @return Completed MessageEmbed for starboard.
     */
    private @NotNull MessageEmbed createPost(@NotNull Message msg, StarboardHandler starboardHandler) {
        // Build post embed
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(POST_COLOR)
                .setAuthor(msg.getAuthor().getName(), null, msg.getAuthor().getEffectiveAvatarUrl())
                .setDescription(msg.getContentRaw())
                .setFooter(msg.getId())
                .setTimestamp(new Date().toInstant());

        // Add source jump link if enabled
        if (starboardHandler.hasJumpLink()) {
            embed.addField("Source", "[Jump to Message!](" + msg.getJumpUrl() + ")", true);
        }

        // Add field for unsupported attachments
        if (!msg.getAttachments().isEmpty()) {
            Message.Attachment file = msg.getAttachments().get(0);
            if (file.isImage()) {
                embed.setImage(file.getUrl());
            } else if (!file.isSpoiler()) {
                embed.addField("Attachment", "[" + file.getFileName() + "](" + file.getUrl() + ")", false);
            }
        }
        return embed.build();
    }

    /**
     * Manages star reactions to messages. If the start count match or exceeds the set limit,
     * this method will add or update the corresponding post on the starboard.
     *
     * @param event Fires each time a user reacts to a message with the star emote.
     */
    @Override
    public synchronized void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        if (!event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(STAR_EMOTE_1)) return;

        // Check that starboard is valid
        StarboardHandler starboardHandler = GuildData.get(event.getGuild()).starboardHandler;
        if (!starboardHandler.isValid(event.getTextChannel())) return;

        // Check that starboard channel is valid
        TextChannel channel = event.getGuild().getTextChannelById(starboardHandler.getChannel());
        if (channel == null) return;

        // Check star count
        event.retrieveMessage().queue(msg -> {
            // Check that message does not include any embeds
            if (!msg.getEmbeds().isEmpty()) return;

            // Check if user can self star
            if (msg.getAuthor() == event.getUser() && !starboardHandler.canSelfStar()) return;

            msg.retrieveReactionUsers(STAR_EMOTE_1).queue(reaction -> {
                int stars = reaction.size();
                String text = getStarEmoji(stars) + " **" + stars + "** <#" + event.getChannel().getId() + ">";
                String messageID = event.getMessageId();
                if (starboardHandler.containsPost((messageID))) {
                    // Edit existing post on starboard
                    String postID = starboardHandler.getPost(messageID);
                    channel.retrieveMessageById(postID).flatMap(post -> post.editMessage(text)).queue();
                } else if (stars >= starboardHandler.getStarLimit()) {
                    // Add new post to starboard
                    channel.sendMessage(text).setEmbeds(createPost(msg, starboardHandler)).queue(post -> {
                        starboardHandler.addPost(messageID, post.getId());
                    });
                }
            });
        });
    }

    /**
     * Manages deleted star reactions on messages. If the start falls below 1,
     * this method will remove the corresponding post from the starboard.
     *
     * @param event Fires each time a user removes their star reaction on a message.
     */
    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        if (!event.getReactionEmote().getAsReactionCode().equalsIgnoreCase(STAR_EMOTE_1)) return;

        // Check that starboard is valid
        StarboardHandler starboardHandler = GuildData.get(event.getGuild()).starboardHandler;
        if (!starboardHandler.isValid(event.getTextChannel())) return;

        // Check that starboard channel is valid
        TextChannel channel = event.getGuild().getTextChannelById(starboardHandler.getChannel());
        if (channel == null) return;

        // Check star count
        event.retrieveMessage().queue(msg -> {
            // Check that message does not include any embeds
            if (!msg.getEmbeds().isEmpty()) return;

            msg.retrieveReactionUsers(STAR_EMOTE_1).queue(reaction -> {
                int stars = reaction.size();
                String text = getStarEmoji(stars) + " **" + stars + "** <#" + event.getChannel().getId() + ">";
                String messageID = event.getMessageId();
                if (stars <= 0) {
                    // Remove post from starboard
                    String post = starboardHandler.removePost(messageID);
                    if (post != null) channel.deleteMessageById(post).queue();
                } else {
                    // Edit existing post on starboard
                    String postID = starboardHandler.getPost(messageID);
                    channel.retrieveMessageById(postID).flatMap(post -> post.editMessage(text)).queue();
                }
            });
        });
    }
}
