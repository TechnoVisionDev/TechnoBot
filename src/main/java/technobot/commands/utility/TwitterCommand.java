package technobot.commands.utility;

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

import static technobot.util.Localization.get;

/**
 * Command that retrieves information about a Twitter account.
 * Uses the Twitter API v2 with bearer token.
 *
 * @author TechnoVision
 */
public class TwitterCommand extends Command {

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    private final String TWITTER_TOKEN;

    public TwitterCommand(TechnoBot bot) {
        super(bot);
        this.name = "twitter";
        this.description = "Show information about a twitter user.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.STRING, "user", "The user you want to get information about", true));
        TWITTER_TOKEN = bot.config.get("TWITTER_TOKEN");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String username = event.getOption("user").getAsString();
        String url = "https://api.twitter.com/2/users/by/username/" + username + "?user.fields=profile_image_url%2Cpublic_metrics%2Clocation%2Cdescription%2Curl";

        // Asynchronous API call
        Request request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + TWITTER_TOKEN).build();
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

                JsonObject twitterUser = bot.gson.fromJson(response.body().string(), JsonObject.class).getAsJsonObject("data");
                if (twitterUser == null) {
                    sendErrorMessage(event.getHook());
                    return;
                }
                JsonObject publicMetrics = twitterUser.getAsJsonObject("public_metrics");

                String name = twitterUser.get("name").getAsString();
                String username = twitterUser.get("username").getAsString();
                String following = FORMATTER.format(publicMetrics.get("following_count").getAsLong());
                String followers = FORMATTER.format(publicMetrics.get("followers_count").getAsLong());
                String avatar = twitterUser.get("profile_image_url").getAsString();
                String location = twitterUser.has("location") ? twitterUser.get("location").getAsString() : "";
                String bio = twitterUser.has("description") ? twitterUser.get("description").getAsString() : "";
                String url = twitterUser.has("url") ? twitterUser.get("url").getAsString() : "";

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setAuthor(name + "(@" + username + ")", "https://twitter.com/" + username, avatar)
                        .setThumbnail(avatar)
                        .setFooter(
                                get(s -> s.utility.twitter.footer, following, followers)
                        );
                if (!location.isEmpty()) embed.appendDescription("\n:pushpin: " + location);
                if (!url.isEmpty()) embed.appendDescription("\n:link: " + url);
                if (!bio.isEmpty()) embed.appendDescription("\n:information_source: " + bio);

                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
        });
    }

    private void sendErrorMessage(InteractionHook hook) {
        String text = get(s -> s.utility.twitter.failure);
        hook.sendMessageEmbeds(EmbedUtils.createError(text)).queue();
    }
}
