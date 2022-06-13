package technobot.commands.levels;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Leveling;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * Command that displays the leveling leaderboard.
 *
 * @author TechnoVision
 */
public class LeaderboardCommand extends Command {

    private static final DecimalFormat formatter = new DecimalFormat("#,###");;

    public LeaderboardCommand(TechnoBot bot) {
        super(bot);
        this.name = "leaderboard";
        this.description = "Displays the most active members on the server.";
        this.category = Category.LEVELS;
        this.args.add(new OptionData(OptionType.INTEGER, "page", "The page to display").setMinValue(1).setMaxValue(5000));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int usersPerPage = 10;
        int start = 0;
        OptionMapping pageOption = event.getOption("page");
        LinkedList<Leveling> leaderboard = GuildData.get(event.getGuild()).levelingHandler.getLeaderboard();

        // Check for pages
        if (pageOption != null) {
            try {
                int page = pageOption.getAsInt();
                if (page > 1) {
                    int comparison = (leaderboard.size() / usersPerPage) + 1;
                    if (leaderboard.size() % usersPerPage != 0) {
                        comparison++;
                    }
                    if (page >= comparison) {
                        String text = "That page doesn't exist!";
                        event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                        return;
                    }
                    start = (usersPerPage * (page - 1)) - 1;
                }
            } catch (NumberFormatException e) {
                String text = "That is not a valid page number!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
                return;
            }
        }

        // Build leaderboard
        StringBuilder result = new StringBuilder();
        int finish = start + usersPerPage;
        if (start != 0) {
            finish++;
        }
        if (start != 0) {
            start++;
        }
        for (int i = start; i < finish; i++) {
            try {
                Leveling profile = leaderboard.get(i);
                long totalXP = profile.getTotalXP();
                long id = profile.getUser();
                if (i == start) result.append("**");
                result.append("#").append(i + 1)
                        .append(" | <@!")
                        .append(id)
                        .append("> XP: `")
                        .append(formatter.format(totalXP))
                        .append("`\n");
                if (i == start) result.append("**");
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        // Create and send embed
        int maxPage = leaderboard.size() / usersPerPage;
        if (maxPage * usersPerPage != leaderboard.size()) { maxPage++; }
        if (maxPage == 0) { maxPage++; }
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":trophy: RANK LEADERBOARD ["+(1 + (start / usersPerPage))+"/"+maxPage+"]")
                .setColor(EmbedColor.DEFAULT.color)
                .setDescription(result.toString())
                .setFooter(event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant());
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

}
