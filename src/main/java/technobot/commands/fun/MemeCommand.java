package technobot.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.*;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.json.RedditPost;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Command that generates a meme from the r/dankmemes subreddit.
 *
 * @author TechnoVision
 */
public class MemeCommand extends Command {

    public MemeCommand(TechnoBot bot) {
        super(bot);
        this.name = "meme";
        this.description = "Get a random meme.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "category", "The type of meme to generate")
                .addChoice("meme", "meme")
                .addChoice("dankmeme", "dankmemes")
                .addChoice("surreal", "surrealmemes")
                .addChoice("me_irl", "me_irl")
                .addChoice("wholesome", "wholesomememes"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String url = "https://meme-api.herokuapp.com/gimme/";
        OptionMapping category = event.getOption("category");
        if (category != null) {
            url += category.getAsString();
        } else {
            int result = ThreadLocalRandom.current().nextInt(2) + 1;
            if (result == 1) url += "dankmemes";
            else url += "memes";
        }

        // Asynchronous API call
        Request request = new Request.Builder().url(url).build();
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = "I was unable to fetch any memes!";
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
