package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Economy;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Command that displays the economy leaderboard.
 *
 * @author TechnoVision
 */
public class BalTopCommand extends Command {

    public BalTopCommand(TechnoBot bot) {
        super(bot);
        this.name = "baltop";
        this.description = "Displays the richest members on the server.";
        this.category = Category.LEVELS;
        this.args.add(new OptionData(OptionType.INTEGER, "page", "The page to display").setMinValue(1).setMaxValue(5000));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int usersPerPage = 10;
        int start = 0;
        OptionMapping pageOption = event.getOption("page");
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        List<Economy> leaderboard = StreamSupport.stream(economyHandler.getLeaderboard().spliterator(), false).toList();

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
                Economy profile = leaderboard.get(i);
                long networth = calculateNetworth(profile.getBalance(), profile.getBank());
                long id = profile.getUser();
                if (i == start) result.append("**");
                result.append("#").append(i + 1)
                        .append(" | <@!")
                        .append(id)
                        .append("> ")
                        .append(economyHandler.getCurrency())
                        .append(EconomyHandler.FORMATTER.format(networth))
                        .append("\n");
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
                .setTitle(":bank: ECONOMY LEADERBOARD ["+(1 + (start / usersPerPage))+"/"+maxPage+"]")
                .setColor(EmbedColor.DEFAULT.color)
                .setDescription(result.toString())
                .setFooter(event.getUser().getAsTag(), event.getUser().getEffectiveAvatarUrl())
                .setTimestamp(new Date().toInstant());
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }

    private long calculateNetworth(Long balance, Long bank) {
        if (balance == null) balance = 0L;
        if (bank == null) bank = 0L;
        return balance + bank;
    }
}
