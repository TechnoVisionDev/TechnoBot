package technobot.commands.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Command that retrieves information about a YouTube channel.
 * Uses the YouTube API v3 with token.
 *
 * @author TechnoVision
 */
public class YouTubeCommand extends Command {

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    private final String YOUTUBE_TOKEN;

    public YouTubeCommand(TechnoBot bot) {
        super(bot);
        this.name = "youtube";
        this.description = "Show information about a YouTube channel.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.STRING, "channel", "The channel you want to get information about", true));
        YOUTUBE_TOKEN = bot.config.get("YOUTUBE_TOKEN");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String channel = event.getOption("channel").getAsString().replace(" ", "+");
        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=channel&maxResults=1&q="+channel+"&key="+YOUTUBE_TOKEN;

        // Asynchronous API call
        Request request = new Request.Builder().url(url).build();
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendErrorMessage(event.getHook());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    sendErrorMessage(event.getHook());
                    return;
                }
                JsonArray results = bot.gson.fromJson(response.body().string(), JsonObject.class).getAsJsonArray("items");
                if (results.isEmpty()) {
                    sendErrorMessage(event.getHook());
                    return;
                }
                // Get channel ID and text data
                JsonObject channelObject = results.get(0).getAsJsonObject();
                String channelID = channelObject.getAsJsonObject("id").get("channelId").getAsString();
                JsonObject channelSnippet = channelObject.getAsJsonObject("snippet");
                String title = channelSnippet.get("title").getAsString();
                String desc = channelSnippet.get("description").getAsString();
                String avatar = channelSnippet.getAsJsonObject("thumbnails").getAsJsonObject("default").get("url").getAsString();

                // Second Asynchronous API call
                String statsURL = "https://www.googleapis.com/youtube/v3/channels?part=statistics&id="+channelID+"&key="+YOUTUBE_TOKEN;
                Request request2 = new Request.Builder().url(statsURL).build();
                bot.httpClient.newCall(request2).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        sendErrorMessage(event.getHook());
                    }

                    @Override
                    public void onResponse(Call call, final Response response2) throws IOException {
                        if (!response2.isSuccessful()) {
                            sendErrorMessage(event.getHook());
                            return;
                        }
                        // Get channel statistics
                        JsonArray results = bot.gson.fromJson(response2.body().string(), JsonObject.class).getAsJsonArray("items");
                        JsonObject stats = results.get(0).getAsJsonObject().getAsJsonObject("statistics");
                        String views = FORMATTER.format(stats.get("viewCount").getAsLong());
                        String subs = FORMATTER.format(stats.get("subscriberCount").getAsLong());
                        String videos = FORMATTER.format(stats.get("videoCount").getAsLong());
                        String link = "https://www.youtube.com/"+channel.toLowerCase();

                        // Build nice embed displaying all info
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(EmbedColor.DEFAULT.color)
                                .setAuthor(title + " | YouTube Channel", link, avatar)
                                .setThumbnail(avatar)
                                .setDescription(desc)
                                .addField("Statistics", "**Subscribers:** "+subs+"\n**Views:** "+views+"\n**Videos:** "+videos, false)
                                .setFooter(link);
                        event.getHook().sendMessageEmbeds(embed.build()).queue();
                    }
                });
            }
        });
    }

    private void sendErrorMessage(InteractionHook hook) {
        String text = "I was unable to find that YouTube channel!";
        hook.sendMessageEmbeds(EmbedUtils.createError(text)).queue();
    }
}
