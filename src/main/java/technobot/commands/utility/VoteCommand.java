package technobot.commands.utility;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;
import technobot.util.localization.Vote;

import java.io.IOException;

import static technobot.util.Localization.format;
import static technobot.util.Localization.get;

/**
 * Command that retrieves information about a Twitter account.
 * Uses the Twitter API v2 with bearer token.
 *
 * @author TechnoVision
 */
public class VoteCommand extends Command {

    private static final String VOTE_LINK = "https://top.gg/bot/979590525428580363/vote";
    private final String TOPGG_TOKEN;

    public VoteCommand(TechnoBot bot) {
        super(bot);
        this.name = "vote";
        this.description = "Display voting links for TechnoBot.";
        this.category = Category.UTILITY;
        TOPGG_TOKEN = bot.config.get("TOPGG_TOKEN");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        //event.getJDA().getSelfUser().getId()
        Request request = new Request.Builder()
                .url("https://top.gg/api/bots/" + "979590525428580363" + "/check?userId=" + event.getUser().getId())
                .addHeader("Authorization", "Bearer " + TOPGG_TOKEN)
                .build();

        bot.httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendErrorMessage(event.getHook());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    sendErrorMessage(event.getHook());
                    return;
                }
                JsonObject responseObject = bot.gson.fromJson(response.body().string(), JsonObject.class);
                int voted = responseObject.get("voted").getAsInt();
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(EmbedColor.DEFAULT.color)
                        .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

                Vote voteText = get(s -> s.utility.vote);
                if (voted == 1) {
                    embed.setTitle(voteText.cooldown);
                    embed.appendDescription(voteText.thanks);
                    embed.appendDescription("\n" + format(voteText.dontForget, VOTE_LINK));
                } else {
                    embed.setTitle(voteText.voteAvailable);
                    embed.appendDescription(format(voteText.clickHere, VOTE_LINK));
                    embed.appendDescription("\n" + voteText.voteEvery12h);
                }
                event.getHook().sendMessageEmbeds(embed.build()).addActionRow(Button.link(VOTE_LINK, "Vote Here")).queue();
            }
        });
    }

    private void sendErrorMessage(InteractionHook hook) {
        String text = get(s -> s.utility.vote.noLink);
        hook.sendMessageEmbeds(EmbedUtils.createError(text)).queue();
    }
}
