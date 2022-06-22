package technobot.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.json.RedditPost;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;

/**
 * Command that generates a post from various subreddits.
 *
 * @author TechnoVision
 */
public class RedditCommand extends Command {

    public RedditCommand(TechnoBot bot) {
        super(bot);
        this.name = "reddit";
        this.description = "Get a reddit post from various subreddits.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "category", "The subreddit to browse", true)
                .addChoice("facepalm", "facepalm")
                .addChoice("comic", "comics")
                .addChoice("blacktwitter", "BlackPeopleTwitter")
                .addChoice("foodporn", "foodporn"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String category = event.getOption("category").getAsString();
        String url = "https://meme-api.herokuapp.com/gimme/" + category;

        // Asynchronous API call
        Request request = new Request.Builder().url(url).build();
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = "I was unable to fetch any posts from that subreddit!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException();
                RedditPost entity = bot.gson.fromJson(response.body().string(), RedditPost.class);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setTitle(entity.getTitle(), entity.getPostLink())
                        .setImage(entity.getImageUrl())
                        .setFooter(""+entity.getUpvotes(), RedditPost.UPVOTE_EMOJI);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
        });
    }
}
