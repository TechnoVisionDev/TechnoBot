package technobot.commands.fun;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

/**
 * Command that creates a fun 'would you rather' poll using an API.
 *
 * @author TechnoVision
 */
public class WouldYouRatherCommand extends Command {

    public WouldYouRatherCommand(TechnoBot bot) {
        super(bot);
        this.name = "wouldyourather";
        this.description = "Answer a \"would you rather\" question with your friends.";
        this.category = Category.FUN;
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Request request = new Request.Builder()
                .url("https://would-you-rather-api--abaanshanid.repl.co/")
                .addHeader("Accept", "application/json")
                .build();

        // Asynchronous API call
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = "I was unable to fetch any questions!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String text = "I was unable to fetch any questions!";
                    event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                    return;
                }

                // Build 'Would you Rathe' embed from API response
                String questions = bot.gson.fromJson(response.body().string(), JsonObject.class).get("data").getAsString();
                String[] split = questions.split(" or ");
                String optionA = ":regional_indicator_a: "+split[0].substring(17);
                String optionB = ":regional_indicator_b: "+split[1].substring(0, split[1].length()-1);
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Would you rather...")
                        .setColor(EmbedColor.DEFAULT.color)
                        .setDescription(optionA)
                        .addField("or", optionB, false);

                // Send embed and add emoji reactions
                event.getHook().sendMessageEmbeds(embed.build()).queue(msg -> {
                    msg.addReaction("\uD83C\uDDE6").queue();
                    msg.addReaction("\uD83C\uDDE7").queue();
                });
            }
        });
    }
}
