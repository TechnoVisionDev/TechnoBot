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

import static technobot.util.Localization.get;

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

        String text = event.getUser().getName() + switch (emote) {
            case "mad" -> get(s -> s.fun.emote.mad);
            case "blush" -> get(s -> s.fun.emote.blush);
            case "celebrate" -> get(s -> s.fun.emote.celebrate);
            case "clap" -> get(s -> s.fun.emote.clap);
            case "confused" -> get(s -> s.fun.emote.confused);
            case "cry" -> get(s -> s.fun.emote.cry);
            case "dance" -> get(s -> s.fun.emote.dance);
            case "facepalm" -> get(s -> s.fun.emote.facepalm);
            case "happy" -> get(s -> s.fun.emote.happy);
            case "laugh" -> get(s -> s.fun.emote.laugh);
            case "pout" -> get(s -> s.fun.emote.pout);
            case "shrug" -> get(s -> s.fun.emote.shrug);
            case "shy" -> get(s -> s.fun.emote.shy);
            case "sigh" -> get(s -> s.fun.emote.sigh);
            case "slowclap" -> get(s -> s.fun.emote.slowClap);
            case "scared" -> get(s -> s.fun.emote.scared);
            case "sleep" -> get(s -> s.fun.emote.sleep);
            case "yawn" -> get(s -> s.fun.emote.yawn);
            default -> "";
        };

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
