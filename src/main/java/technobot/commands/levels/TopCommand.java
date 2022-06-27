package technobot.commands.levels;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.data.cache.Economy;
import technobot.data.cache.Leveling;
import technobot.handlers.LevelingHandler;
import technobot.handlers.economy.EconomyHandler;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedColor;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static technobot.util.Localization.get;

/**
 * Command that displays various leaderboards.
 *
 * @author TechnoVision
 */
public class TopCommand extends Command {

    private static final int USERS_PER_PAGE = 10;
    private static final String ECONOMY_ICON = "https://images.emojiterra.com/google/noto-emoji/v2.034/512px/1f4b0.png";
    private static final String LEVELING_ICON = "https://images.emojiterra.com/twitter/v13.1/512px/1f4c8.png";
    private static final String LEADERBOARD_ICON = "https://cdn-icons-png.flaticon.com/512/1657/1657088.png";
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    ;

    public TopCommand(TechnoBot bot) {
        super(bot);
        this.name = "top";
        this.description = "Displays the most active members on the server.";
        this.category = Category.LEVELS;
        this.args.add(new OptionData(OptionType.STRING, "type", "The leaderboard type to display")
                .addChoice("leveling", "leveling")
                .addChoice("economy", "economy"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long guildID = event.getGuild().getIdLong();
        long userID = event.getUser().getIdLong();
        GuildData data = GuildData.get(event.getGuild());

        OptionMapping typeOption = event.getOption("type");
        if (typeOption != null) {
            String type = typeOption.getAsString();
            List<MessageEmbed> embeds = new ArrayList<>();

            // Leveling leaderboard (Rank)
            if (type.equalsIgnoreCase("Leveling")) {
                embeds = buildLevelingLeaderboard(data.levelingHandler, guildID, userID);
                if (embeds.isEmpty()) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(EmbedColor.DEFAULT.color)
                            .setAuthor(get(s -> s.levels.top.leveling.name), null, LEVELING_ICON)
                            .setDescription(get(s -> s.levels.top.leveling.empty))
                            .build()
                    ).queue();
                    return;
                }
            }
            // Economy Leaderboard (Economy)
            else if (type.equalsIgnoreCase("Economy")) {
                embeds = buildEconomyLeaderboard(data.economyHandler, guildID, userID);
                if (embeds.isEmpty()) {
                    event.getHook().sendMessageEmbeds(new EmbedBuilder()
                            .setColor(EmbedColor.DEFAULT.color)
                            .setAuthor(get(s -> s.levels.top.economy.name), null, ECONOMY_ICON)
                            .setDescription(get(s -> s.levels.top.economy.empty))
                            .build()
                    ).queue();
                    return;
                }
            }
            // Send paginated embeds
            WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(embeds.get(0));
            if (embeds.size() == 1) {
                action.queue();
            } else {
                ButtonListener.sendPaginatedMenu(String.valueOf(userID), action, embeds);
            }
        } else {
            // Top 5 for all leaderboards
            FindIterable<Leveling> levelLeaderboard = data.levelingHandler.getLeaderboard();
            AggregateIterable<Economy> econLeaderboard = data.economyHandler.getLeaderboard();

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .setAuthor(get(s -> s.levels.top.guild.name), null, LEADERBOARD_ICON)
                    .setTimestamp(new Date().toInstant());

            int counter = 1;
            StringBuilder levelDesc = new StringBuilder();
            for (Leveling profile : levelLeaderboard) {
                if (counter == 6) break;
                if (counter == 1) levelDesc.append("**");
                levelDesc.append(get(
                                s -> s.levels.top.leveling.entry,
                                counter,
                                profile.getUser(),
                                FORMATTER.format(profile.getTotalXP())
                        ) + "")
                        .append("\n");
                if (counter == 1) levelDesc.append("**");
                counter++;
            }
            levelDesc.append(get(s -> s.levels.top.guild.more, "leveling") + "");
            embed.addField(
                    get(s -> s.levels.top.guild.title, "LEVELING", ":chart_with_upwards_trend:"),
                    levelDesc.toString(),
                    true
            );

            counter = 1;
            StringBuilder econDesc = new StringBuilder();
            for (Economy profile : econLeaderboard) {
                if (counter == 6) break;
                if (counter == 1) econDesc.append("**");
                long netWorth = calculateNetWorth(profile.getBalance(), profile.getBank());
                econDesc.append(get(
                                s -> s.levels.top.economy.entry,
                                counter,
                                profile.getUser(),
                                FORMATTER.format(netWorth)
                        ) + "")
                        .append("\n");
                if (counter == 1) econDesc.append("**");
                counter++;
            }
            econDesc.append(get(s -> s.levels.top.guild.more, "economy") + "");
            embed.addField(
                    get(s -> s.levels.top.guild.title,
                            "ECONOMY",
                            "moneybag"), econDesc.toString(), true
            );

            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }

    /**
     * Build a economy leaderboard with paginated embeds.
     *
     * @param economyHandler the guild's economy handler instance.
     * @param guildID        the ID of the guild.
     * @param userID         the ID of the user who ran this command.
     * @return a list of economy leaderboard pages.
     */
    private List<MessageEmbed> buildEconomyLeaderboard(EconomyHandler economyHandler, long guildID, long userID) {
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder embed = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color);
        StringBuilder description = new StringBuilder();
        int currRank = 1;
        int counter = 0;
        int page = 1;

        embed.setAuthor("Economy Leaderboard", null, ECONOMY_ICON);
        AggregateIterable<Economy> leaderboard = economyHandler.getLeaderboard();
        long size = bot.database.economy.countDocuments(Filters.eq("guild", guildID));
        if (size % 10 == 0) size--;
        long maxPages = 1 + (size / 10);
        for (Economy profile : leaderboard) {
            long netWorth = calculateNetWorth(profile.getBalance(), profile.getBank());
            if (counter == 0 && page == 1) description.append("**");
            description.append(get(
                            s -> s.levels.top.economy.entry,
                            currRank,
                            profile.getUser(),
                            FORMATTER.format(netWorth)
                    ) + "")
                    .append("\n");
            if (counter == 0 && page == 1) description.append("**");
            counter++;
            currRank++;
            if (counter % USERS_PER_PAGE == 0) {
                embed.setDescription(description);
                embed.setFooter("Page " + page + "/" + maxPages + "  •  Your rank: " + currRank);
                embeds.add(embed.build());
                description = new StringBuilder();
                counter = 0;
                page++;
            }
        }
        if (counter != 0) {
            embed.setDescription(description);
            embed.setFooter("Page " + page + "/" + maxPages + "  •  Your rank: " + currRank);
            embeds.add(embed.build());
        }
        return embeds;
    }

    /**
     * Build a leveling leaderboard with paginated embeds.
     *
     * @param levelingHandler the guild's leveling handler instance.
     * @param guildID         the ID of the guild.
     * @param userID          the ID of the user who ran this command.
     * @return a list of leveling leaderboard pages.
     */
    private List<MessageEmbed> buildLevelingLeaderboard(LevelingHandler levelingHandler, long guildID, long userID) {
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder embed = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color);
        StringBuilder description = new StringBuilder();
        int currRank = 1;
        int counter = 0;
        int page = 1;

        embed.setAuthor("Leveling Leaderboard", null, LEVELING_ICON);
        FindIterable<Leveling> leaderboard = levelingHandler.getLeaderboard();
        long size = bot.database.leveling.countDocuments(Filters.eq("guild", guildID));
        if (size % 10 == 0) size--;
        long maxPages = 1 + (size / 10);
        for (Leveling profile : leaderboard) {
            if (counter == 0 && page == 1) description.append("**");
            description.append(get(
                            s -> s.levels.top.leveling.entry,
                            currRank,
                            profile.getUser(),
                            FORMATTER.format(profile.getTotalXP())
                    ) + "")
                    .append("\n");
            if (counter == 0 && page == 1) description.append("**");
            counter++;
            currRank++;
            if (counter % USERS_PER_PAGE == 0) {
                embed.setDescription(description);
                embed.setFooter(get(
                        s -> s.levels.top.footer,
                        page,
                        maxPages,
                        currRank
                ));
                embeds.add(embed.build());
                description = new StringBuilder();
                counter = 0;
                page++;
            }
        }
        if (counter != 0) {
            embed.setDescription(description);
            embed.setFooter(get(
                    s -> s.levels.top.footer,
                    page,
                    maxPages,
                    currRank
            ));
            embeds.add(embed.build());
        }
        return embeds;
    }

    /**
     * Calculates a user's networth, taking into account null values.
     *
     * @param balance user's cash balance.
     * @param bank    user's bank balance.
     * @return the user's cash and bank balance combined.
     */
    private long calculateNetWorth(Long balance, Long bank) {
        if (balance == null) balance = 0L;
        if (bank == null) bank = 0L;
        return balance + bank;
    }
}
