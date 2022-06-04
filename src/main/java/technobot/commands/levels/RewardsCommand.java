package technobot.commands.levels;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.Map;

/**
 * Command that displays leveling rewards.
 *
 * @author TechnoVision
 */
public class RewardsCommand extends Command {

    public RewardsCommand(TechnoBot bot) {
        super(bot);
        this.name = "rewards";
        this.description = "Displays available leveling rewards.";
        this.category = Category.LEVELS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Map<String,Integer> rewards = GuildData.get(event.getGuild()).config.getRewards();

        StringBuilder content = new StringBuilder();
        for (Map.Entry<String,Integer> reward : rewards.entrySet()) {
            content.append("Level ").append(reward.getValue()).append(" ----> <@&").append(reward.getKey()).append(">\n");
        }

        if (!content.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .setTitle(":crown: Leveling Rewards")
                    .setDescription(content);
            event.replyEmbeds(embed.build()).queue();
            return;
        }
        event.replyEmbeds(EmbedUtils.createDefault(EmbedUtils.BLUE_X + " No leveling rewards have been set for this server!")).queue();
    }
}
