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
        String text = event.getUser().getName() + " ";
        switch (emote) {
            case "bite" -> text += "takes a bite out of "+target+".";
            case "brofist" -> text += "and "+target+" brofist!";
            case "cuddle" -> text += "cuddles with "+target+".";
            case "handhold" -> text += "and "+target+" hold hands. How sweet <3";
            case "hug" -> text += "gives "+target+" a big hug!";
            case "kiss" -> text += "kisses "+target+".";
            case "lick" -> text += "licks "+target+"... gross!";
            case "pat" -> text += "gives "+target+" a little pat on the head";
            case "pinch" -> text += "pinches "+target+". Ouch!";
            case "poke" -> text += "gives "+target+" a little poke.";
            case "punch" -> text += "punches "+target+" right in the face!";
            case "slap" -> text += "slaps "+target+". They deserved it!";
            case "smack" -> text += "gives "+target+" a smack they will remember.";
            case "sorry" -> text += "apologizes to "+target+".";
            case "stare" -> text += "won't stop starting at "+target+"...";
            case "thumbsup" -> text += "gives "+target+" two thumbs up!";
            case "tickle" -> text += "tickles "+target+".";
            case "wave" -> text += "waves at "+target+".";
            case "wink" -> text += "winks at "+target+".";
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
