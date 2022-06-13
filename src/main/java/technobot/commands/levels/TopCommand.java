package technobot.commands.levels;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");;

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
                            .setAuthor("Leveling Leaderboard", null, LEVELING_ICON)
                            .setDescription("Nobody has earned any XP or levels yet!\nSend some messages in chat and use `/rank` to get started!")
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
                            .setAuthor("Economy Leaderboard", null, ECONOMY_ICON)
                            .setDescription("Nobody has earned any money yet!\nUse `/work` and `/balance` to get started!")
                            .build()
                    ).queue();
                    return;
                }
            }
            // Send paginated embeds
            WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(embeds.get(0));
            if (embeds.size() == 1) { action.queue(); }
            else { paginateMenu(action, embeds, userID); }
        } else {
            // Top 5 for all leaderboards
            LinkedList<Leveling> levelLeaderboard = data.levelingHandler.getLeaderboard();
            AggregateIterable<Economy> econLeaderboard = data.economyHandler.getLeaderboard();

            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .setAuthor("Guild Leaderboards", null, LEADERBOARD_ICON)
                    .setTimestamp(new Date().toInstant());

            int counter = 1;
            StringBuilder levelDesc = new StringBuilder();
            for (Leveling profile : levelLeaderboard) {
                if (counter == 6) break;
                if (counter == 1) levelDesc.append("**");
                levelDesc.append("#").append(counter)
                        .append(" | <@!")
                        .append(profile.getUser())
                        .append("> XP: `")
                        .append(FORMATTER.format(profile.getTotalXP()))
                        .append("`\n");
                if (counter == 1) levelDesc.append("**");
                counter++;
            }
            levelDesc.append(":sparkles: **More?** `/top leveling`");
            embed.addField("TOP 5 LEVELING :chart_with_upwards_trend:", levelDesc.toString(), true);

            counter = 1;
            StringBuilder econDesc = new StringBuilder();
            for (Economy profile : econLeaderboard) {
                if (counter == 6) break;
                if (counter == 1) econDesc.append("**");
                long networth = calculateNetworth(profile.getBalance(), profile.getBank());
                econDesc.append("#").append(counter)
                        .append(" | <@!")
                        .append(profile.getUser())
                        .append("> ")
                        .append(data.economyHandler.getCurrency())
                        .append(" ")
                        .append(FORMATTER.format(networth))
                        .append("\n");
                if (counter == 1) econDesc.append("**");
                counter++;
            }
            econDesc.append(":sparkles: **More?** `/top economy`");
            embed.addField("TOP 5 ECONOMY :moneybag:", econDesc.toString(), true);

            event.getHook().sendMessageEmbeds(embed.build()).queue();
        }
    }

    /**
     * Build a economy leaderboard with paginated embeds.
     *
     * @param economyHandler the guild's economy handler instance.
     * @param guildID the ID of the guild.
     * @param userID the ID of the user who ran this command.
     * @return a list of economy leaderboard pages.
     */
    private List<MessageEmbed> buildEconomyLeaderboard(EconomyHandler economyHandler, long guildID, long userID) {
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder embed = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color);
        StringBuilder description = new StringBuilder();
        int currRank = 1;
        int counter = 0;
        int page = 1;
        int rank = 0;
        boolean foundRank = false;

        embed.setAuthor("Economy Leaderboard", null, ECONOMY_ICON);
        AggregateIterable<Economy> leaderboard = economyHandler.getLeaderboard();
        long size = bot.database.economy.countDocuments(Filters.eq("guild", guildID));
        if (size % 10 == 0) size--;
        long maxPages = 1 + (size / 10);
        for (Economy profile : leaderboard) {
            if (!foundRank) rank++;
            if (profile.getUser() == userID) foundRank = true;
            long networth = calculateNetworth(profile.getBalance(), profile.getBank());
            if (counter == 0 && page == 1) description.append("**");
            description.append("#").append(currRank)
                    .append(" | <@!")
                    .append(profile.getUser())
                    .append("> ")
                    .append(economyHandler.getCurrency())
                    .append(" ")
                    .append(FORMATTER.format(networth))
                    .append("\n");
            if (counter == 0 && page == 1) description.append("**");
            counter++;
            currRank++;
            if (counter % USERS_PER_PAGE == 0) {
                embed.setDescription(description);
                embed.setFooter("Page "+page+"/"+maxPages + "  •  Your rank: " + ordinalSuffixOf(rank));
                embeds.add(embed.build());
                description = new StringBuilder();
                counter = 0;
                page++;
            }
        }
        if (counter != 0) {
            embed.setDescription(description);
            embed.setFooter("Page "+page+"/"+maxPages + "  •  Your rank: " + ordinalSuffixOf(rank));
            embeds.add(embed.build());
        }
        return embeds;
    }

    /**
     * Build a leveling leaderboard with paginated embeds.
     *
     * @param levelingHandler the guild's leveling handler instance.
     * @param guildID the ID of the guild.
     * @param userID the ID of the user who ran this command.
     * @return a list of leveling leaderboard pages.
     */
    private List<MessageEmbed> buildLevelingLeaderboard(LevelingHandler levelingHandler, long guildID, long userID) {
        List<MessageEmbed> embeds = new ArrayList<>();
        EmbedBuilder embed = new EmbedBuilder().setColor(EmbedColor.DEFAULT.color);
        StringBuilder description = new StringBuilder();
        int currRank = 1;
        int counter = 0;
        int page = 1;
        int rank = 0;
        boolean foundRank = false;

        embed.setAuthor("Leveling Leaderboard", null, LEVELING_ICON);
        LinkedList<Leveling> leaderboard = levelingHandler.getLeaderboard();
        long size = leaderboard.size();
        if (size % 10 == 0) size--;
        long maxPages = 1 + (size / 10);
        for (Leveling profile : leaderboard) {
            if (!foundRank) rank++;
            if (profile.getUser() == userID) foundRank = true;
            if (counter == 0 && page == 1) description.append("**");
            description.append("#").append(currRank)
                    .append(" | <@!")
                    .append(profile.getUser())
                    .append("> XP: `")
                    .append(FORMATTER.format(profile.getTotalXP()))
                    .append("`\n");
            if (counter == 0 && page == 1) description.append("**");
            counter++;
            currRank++;
            if (counter % USERS_PER_PAGE == 0) {
                embed.setDescription(description);
                embed.setFooter("Page "+page+"/"+maxPages + "  •  Your rank: " + ordinalSuffixOf(rank));
                embeds.add(embed.build());
                description = new StringBuilder();
                counter = 0;
                page++;
            }
        }
        if (counter != 0) {
            embed.setDescription(description);
            embed.setFooter("Page "+page+"/"+maxPages + "  •  Your rank: " + ordinalSuffixOf(rank));
            embeds.add(embed.build());
        }
        return embeds;
    }

    /**
     * Add pagination buttons and functionality to an embed.
     *
     * @param action the embed send action to add onto.
     * @param embeds list of message embeds for each page.
     * @param userID the ID of the user who can access this menu.
     */
    private void paginateMenu(WebhookMessageAction<Message> action, List<MessageEmbed> embeds, long userID) {
        // Add buttons to paginated menu
        String uuid = userID + ":" + UUID.randomUUID();
        ButtonListener.menus.put(uuid, embeds);
        List<Button> components = Arrays.asList(
                Button.primary("pagination:prev:"+uuid, "Previous").asDisabled(),
                Button.of(ButtonStyle.SECONDARY, "top:page:0", "1/"+embeds.size()).asDisabled(),
                Button.primary("pagination:next:"+uuid, "Next")
        );
        ButtonListener.buttons.put(uuid, components);
        action.addActionRow(components).queue(interactionHook -> {
            // Timer task to disable buttons and clear cache after 3 minutes
            Runnable task = () -> {
                List<Button> actionRow = ButtonListener.buttons.get(uuid);
                actionRow.set(0, actionRow.get(0).asDisabled());
                actionRow.set(2, actionRow.get(2).asDisabled());
                interactionHook.editMessageComponents(ActionRow.of(actionRow)).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE));
                ButtonListener.buttons.remove(uuid);
                ButtonListener.menus.remove(uuid);
            };
            ButtonListener.executor.schedule(task, 3, TimeUnit.MINUTES);
        });
    }

    /**
     * Calculates a user's networth, taking into account null values.
     *
     * @param balance user's cash balance.
     * @param bank user's bank balance.
     * @return the user's cash and bank balance combined.
     */
    private long calculateNetworth(Long balance, Long bank) {
        if (balance == null) balance = 0L;
        if (bank == null) bank = 0L;
        return balance + bank;
    }

    /**
     * Get the string ordinal suffix of a number (1st, 2nd, 3rd, 4th, etc).
     *
     * @param i the number to format.
     * @return ordinal suffix of number in string form.
     */
    private String ordinalSuffixOf(int i) {
        int j = i % 10, k = i % 100;
        if (j == 1 && k != 11) {
            return i + "st";
        }
        if (j == 2 && k != 12) {
            return i + "nd";
        }
        if (j == 3 && k != 13) {
            return i + "rd";
        }
        return i + "th";
    }
}
