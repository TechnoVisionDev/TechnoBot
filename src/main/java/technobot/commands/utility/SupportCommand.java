package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Command that generates a link to the support server.
 *
 * @author TechnoVision
 */
public class SupportCommand extends Command {

    public SupportCommand(TechnoBot bot) {
        super(bot);
        this.name = "support";
        this.description = "Get an invite to the support server.";
        this.category = Category.UTILITY;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("TechnoBot Support")
                .setDescription("Hey "+event.getUser().getName()+", how can I help you?")
                .addField("Server", "[Ask a question.](https://discord.com/invite/2TKJqfUQas)", true)
                .addField("Bug Report", "[Report an issue.](https://github.com/TechnoVisionDev/TechnoBot/issues)", true)
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
