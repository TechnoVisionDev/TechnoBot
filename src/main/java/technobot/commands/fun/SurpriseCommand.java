package technobot.commands.fun;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Command that generates a link from uselessweb.
 *
 * @author TechnoVision
 */
public class SurpriseCommand extends Command {

    public SurpriseCommand(TechnoBot bot) {
        super(bot);
        this.name = "surprise";
        this.description = "Get a fun and unique surprise.";
        this.category = Category.FUN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Request request = new Request.Builder()
                .url("https://gist.githubusercontent.com/quest/07bbc6908f84b50a9fc8/raw/d8983a0723d07203816b78953ff52f07423c808d/uselessweb.json")
                .build();

        // Asynchronous API call
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = "I was unable to fetch any surprises!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException();
                SiteList entity = bot.gson.fromJson(response.body().string(), SiteList.class);
                int index = ThreadLocalRandom.current().nextInt(entity.uselessweb.size());
                event.getHook().sendMessage(entity.uselessweb.get(index)).queue();
            }
        });
    }

    /**
     * Represents a joke retrieved from the joke api.
     * Used by OkHttp and Gson to convert JSON to java code.
     */
    private class SiteList {

        public List<String> uselessweb;

        public SiteList(List<String> uselessweb) {
             this.uselessweb = uselessweb;
        }
    }
}
