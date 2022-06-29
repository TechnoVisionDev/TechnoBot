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

import java.io.IOException;

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
                if (voted == 1) {
                    embed.setTitle(":stopwatch: Your daily vote is on cooldown!");
                    embed.appendDescription("Thanks so much for voting for me today! :heart_eyes:");
                    embed.appendDescription("\nDon't forget to vote every 12 hours [here]("+VOTE_LINK+").");
                } else {
                    embed.setTitle(EmbedUtils.BLUE_TICK + " Your daily vote is available!");
                    embed.appendDescription("Click [here]("+VOTE_LINK+") to vote for me!");
                    embed.appendDescription("\nYou can vote every 12 hours.");
                }
                event.getHook().sendMessageEmbeds(embed.build()).addActionRow(Button.link(VOTE_LINK, "Vote Here")).queue();
            }
        });
    }

    private void sendErrorMessage(InteractionHook hook) {
        String text = "I was unable to find the vote link!";
        hook.sendMessageEmbeds(EmbedUtils.createError(text)).queue();
    }
}
