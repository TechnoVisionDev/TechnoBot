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
import technobot.data.json.Emote;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.IOException;

import static technobot.util.localization.Localization.get;

/**
 * Command that generates an image to match an emotion with another user
 *
 * @author TechnoVision
 */
public class ActionCommand extends Command {

    public ActionCommand(TechnoBot bot) {
        super(bot);
        this.name = "action";
        this.description = "Express your emotions on others.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "action", "The action to perform.", true)
                .addChoice("bite", "bite")
                .addChoice("brofist", "brofist")
                .addChoice("cuddle", "cuddle")
                .addChoice("handhold", "handhold")
                .addChoice("hug", "hug")
                .addChoice("kiss", "kiss")
                .addChoice("lick", "lick")
                .addChoice("pat", "pat")
                .addChoice("pinch", "pinch")
                .addChoice("poke", "poke")
                .addChoice("punch", "punch")
                .addChoice("slap", "slap")
                .addChoice("smack", "smack")
                .addChoice("sorry", "sorry")
                .addChoice("stare", "stare")
                .addChoice("thumbsup", "thumbsup")
                .addChoice("tickle", "tickle")
                .addChoice("wave", "wave")
                .addChoice("wink", "wink"));
        this.args.add(new OptionData(OptionType.USER, "user", "The user to direct this action towards.", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String url = "https://api.otakugifs.xyz/gif?reaction=";
        String emote = event.getOption("action").getAsString();
        url += emote;

        String target = event.getOption("user").getAsUser().getName();
        String text = event.getUser().getName() + " " + switch (emote) {
            case "bite" -> get(s -> s.fun().action().bite(), target);
            case "brofist" -> get(s -> s.fun().action().brofist(), target);
            case "cuddle" -> get(s -> s.fun().action().cuddle(), target);
            case "handhold" -> get(s -> s.fun().action().handhold(), target);
            case "hug" -> get(s -> s.fun().action().hug(), target);
            case "kiss" -> get(s -> s.fun().action().kiss(), target);
            case "lick" -> get(s -> s.fun().action().lick(), target);
            case "pat" -> get(s -> s.fun().action().pat(), target);
            case "pinch" -> get(s -> s.fun().action().pinch(), target);
            case "poke" -> get(s -> s.fun().action().poke(), target);
            case "punch" -> get(s -> s.fun().action().punch(), target);
            case "slap" -> get(s -> s.fun().action().slap(), target);
            case "smack" -> get(s -> s.fun().action().smack(), target);
            case "sorry" -> get(s -> s.fun().action().sorry(), target);
            case "stare" -> get(s -> s.fun().action().stare(), target);
            case "thumbsup" -> get(s -> s.fun().action().thumbsup(), target);
            case "tickle" -> get(s -> s.fun().action().tickle(), target);
            case "wave" -> get(s -> s.fun().action().wave(), target);
            case "wink" -> get(s -> s.fun().action().wink(), target);
        };

        // Asynchronous API call
        Request request = new Request.Builder().url(url).build();
        String finalText = text;
        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String text = get(s -> s.fun().action().failure());
                event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException();
                Emote entity = bot.gson.fromJson(response.body().string(), Emote.class);

                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setAuthor(finalText, null, event.getUser().getEffectiveAvatarUrl())
                        .setImage(entity.getUrl());
                event.replyEmbeds(embed.build()).queue();
            }
        });
    }
}
