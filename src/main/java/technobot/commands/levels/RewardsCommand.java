package technobot.commands.levels;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.LinkedHashMap;
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
        event.deferReply().queue();
        LinkedHashMap<String, Integer> rewards = new LinkedHashMap<>();
        GuildData.get(event.getGuild()).config.getRewards().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .forEachOrdered(x -> rewards.put(x.getKey(), x.getValue()));

        StringBuilder content = new StringBuilder();
        for (Map.Entry<String,Integer> reward : rewards.entrySet()) {
            if (event.getGuild().getRoleById(reward.getKey()) != null) {
                content.append("Level ").append(reward.getValue()).append(" ----> <@&").append(reward.getKey()).append(">\n");
            } else {
                // Remove any deleted roles from database
                Bson filter = Filters.eq("guild", event.getGuild().getIdLong());
                bot.database.config.updateOne(filter, Updates.unset("rewards."+reward.getKey()));
            }
        }

        if (!content.isEmpty()) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .setTitle(":crown: Leveling Rewards")
                    .setDescription(content);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            return;
        }
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(EmbedUtils.BLUE_X + " No leveling rewards have been set for this server!")).queue();
    }
}
