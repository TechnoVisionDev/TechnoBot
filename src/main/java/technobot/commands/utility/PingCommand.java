package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;

import static technobot.util.Localization.get;

/**
 * Ping command to check latency with Discord API.
 *
 * @author TechnoVision
 */
public class PingCommand extends Command {

    public PingCommand(TechnoBot bot) {
        super(bot);
        this.name = "ping";
        this.description = "Display bot latency.";
        this.category = Category.UTILITY;
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long time = System.currentTimeMillis();
        event.getHook().sendMessage(get(s -> s.utility.ping.ping) + "").queue(m -> {
            long latency = System.currentTimeMillis() - time;
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(get(s -> s.utility.ping.pong));
            embed.addField(
                    get(s -> s.utility.ping.latency),
                    get(s -> s.utility.ping.value, latency),
                    false
            );
            embed.addField(
                    get(s -> s.utility.ping.discordApi),
                    get(s -> s.utility.ping.value, event.getJDA().getGatewayPing()),
                    false
            );
            embed.setColor(EmbedColor.DEFAULT.color);
            m.editMessageEmbeds(embed.build()).override(true).queue();
        });
    }
}
