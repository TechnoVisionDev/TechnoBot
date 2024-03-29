package technobot.commands.fun;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import okhttp3.*;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;

/**
 * Command that generates a joke from a joke API.
 *
 * @author TechnoVision
 */
public class JokeCommand extends Command {

    public JokeCommand(TechnoBot bot) {
        super(bot);
        this.name = "joke";
        this.description = "Get a random joke.";
        this.category = Category.FUN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Request request = new Request.Builder()
                .url("https://icanhazdadjoke.com")
                .addHeader("Accept", "application/json")
                .build();

        // Asynchronous API call
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = "I was unable to fetch any jokes!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException();
                Joke entity = bot.gson.fromJson(response.body().string(), Joke.class);
                event.getHook().sendMessage(entity.joke).queue();
            }
        });
    }

    /**
     * Represents a joke retrieved from the joke api.
     * Used by OkHttp and Gson to convert JSON to java code.
     */
    private class Joke {

        public String joke;

        public Joke(String joke) {
             this.joke = joke;
        }
    }
}
