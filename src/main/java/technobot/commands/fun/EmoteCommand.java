package technobot.commands.fun;

import com.google.gson.JsonObject;
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
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;

/**
 * Command that generates an image to match an emotion.
 *
 * @author TechnoVision
 */
public class EmoteCommand extends Command {

    public EmoteCommand(TechnoBot bot) {
        super(bot);
        this.name = "emote";
        this.description = "Express your emotions virtually.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "emote", "The emotion to express.", true)
                .addChoice("angry", "mad")
                .addChoice("blush", "blush")
                .addChoice("celebrate", "celebrate")
                .addChoice("clap", "clap")
                .addChoice("confused", "confused")
                .addChoice("cry", "cry")
                .addChoice("dance", "dance")
                .addChoice("facepalm", "facepalm")
                .addChoice("happy", "happy")
                .addChoice("laugh", "laugh")
                .addChoice("pout", "pout")
                .addChoice("shrug", "shrug")
                .addChoice("shy", "shy")
                .addChoice("sigh", "sigh")
                .addChoice("slowclap", "slowclap")
                .addChoice("scared", "scared")
                .addChoice("sleep", "sleep")
                .addChoice("yawn", "yawn"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String url = "https://api.otakugifs.xyz/gif?reaction=";
        String emote = event.getOption("emote").getAsString();
        url += emote;

        String text = event.getUser().getName() + " ";
        switch (emote) {
            case "mad" -> text += "doesn't like that.";
            case "blush" -> text += "has turned into a tomato.";
            case "celebrate" -> text += "is ready to celebrate!";
            case "clap" -> text += "claps excitedly.";
            case "confused" -> text += "is really confused.";
            case "cry" -> text += "needs a hug...";
            case "dance" -> text += "is dancing!";
            case "facepalm" -> text += "is in disbelief.";
            case "happy" -> text += "smiles.";
            case "laugh" -> text += "laughs out loud.";
            case "pout" -> text += "is in a bad mood.";
            case "shrug" -> text += "doesn't care...";
            case "shy" -> text += "is feeling timid.";
            case "sigh" -> text += "is disappointed.";
            case "slowclap" -> text += "is not amused.";
            case "scared" -> text += "fears for their life.";
            case "sleep" -> text += "falls into a deep sleep.";
            case "yawn" -> text += "is getting very sleepy.";
        }

        // Asynchronous API call
        Request request = new Request.Builder().url(url).build();
        String finalText = text;
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = "I was unable to fetch that emote!";
                event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException();
                JsonObject entity = bot.gson.fromJson(response.body().string(), JsonObject.class);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setAuthor(finalText, null, event.getUser().getEffectiveAvatarUrl())
                        .setImage(entity.get("url").getAsString());
                event.replyEmbeds(embed.build()).queue();
            }
        });
    }
}
