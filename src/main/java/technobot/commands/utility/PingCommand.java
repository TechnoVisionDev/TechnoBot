package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.EmbedColor;

/**
 * Ping command to check latency with Discord API.
 *
 * @author TechnoVision
 */
public class PingCommand extends Command {

    public PingCommand(TechnoBot bot) {
        super(bot);
        this.name = "ping";
        this.description = "Check bot latency";
        this.category = Category.UTILITY;
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long time = System.currentTimeMillis();
        event.getHook().sendMessage(":signal_strength: Ping").queue(m -> {
            long latency = System.currentTimeMillis() - time;
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(":ping_pong: Pong!");
            embed.addField("Latency", latency + "ms", false);
            embed.addField("Discord API", event.getJDA().getGatewayPing() + "ms", false);
            embed.setColor(EmbedColor.DEFAULT.color);
            m.editMessageEmbeds(embed.build()).override(true).queue();
        });
    }
}
