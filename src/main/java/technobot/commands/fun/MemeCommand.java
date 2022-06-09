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

    private static final String UPVOTE_EMOJI = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/120/sony/336/thumbs-up_1f44d.png";

    public MemeCommand(TechnoBot bot) {
        super(bot);
        this.name = "meme";
        this.description = "Get a random meme.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "category", "The type of meme to generate")
                .addChoice("meme", "meme")
                .addChoice("dankmemes", "dankmemes")
                .addChoice("surreal", "surreal")
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
                Meme entity = bot.gson.fromJson(response.body().string(), Meme.class);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setTitle(entity.title, entity.postLink)
                        .setImage(entity.url)
                        .setFooter(""+entity.ups, UPVOTE_EMOJI);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
        });
    }

    /**
     * Represents a meme retrieved from reddit.
     * Used by OkHttp and Gson to convert JSON to java code.
     */
    private class Meme {

        public String title;
        public String postLink;
        public String url;
        public int ups;

        public Meme(String title, String postLink, String url, int ups) {
            this.title = title;
            this.postLink = postLink;
            this.url = url;
            this.ups = ups;
        }
    }
}
