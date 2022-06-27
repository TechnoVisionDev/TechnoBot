package technobot.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.json.Post;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;

import static technobot.util.Localization.get;

/**
 * Command that generates a cute picture from reddit.
 *
 * @author TechnoVision
 */
public class CuteCommand extends Command {

    public CuteCommand(TechnoBot bot) {
        super(bot);
        this.name = "cute";
        this.description = "Get something cute and cuddly.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "category", "The type of cuteness to generate")
                .addChoice("aww", "aww")
                .addChoice("puppy", "puppies")
                .addChoice("kitten", "kitten")
                .addChoice("ferret", "ferrets")
                .addChoice("bunny", "rabbits")
                .addChoice("snek", "snek")
                .addChoice("wholesome", "wholesomememes"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        String url = "https://meme-api.herokuapp.com/gimme/";
        OptionMapping category = event.getOption("category");
        if (category != null) url += category.getAsString();
        else url += "aww";

        // Asynchronous API call
        Request request = new Request.Builder().url(url).build();
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = get(s -> s.fun.cute);
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException();
                Post entity = bot.gson.fromJson(response.body().string(), Post.class);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setTitle(entity.getTitle(), entity.getPostLink())
                        .setImage(entity.getImageUrl())
                        .setFooter(entity.getUpvotes(), Post.UPVOTE_EMOJI);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            }
        });
    }
}
