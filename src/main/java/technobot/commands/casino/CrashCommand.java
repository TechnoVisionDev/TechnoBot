package technobot.commands.casino;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.listeners.ButtonListener;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.*;
import java.util.concurrent.*;

/**
 * Command that plays the crash gambling game.
 *
 * @author TechnoVision
 */
public class CrashCommand extends Command {

    public static final HashMap<Long, CrashGame> games = new HashMap<>();
    private static final ScheduledExecutorService tasks = Executors.newScheduledThreadPool(5);

    public CrashCommand(TechnoBot bot) {
        super(bot);
        this.name = "crash";
        this.description = "Bet against a multiplier that crashes at any moment.";
        this.category = Category.CASINO;
        this.args.add(new OptionData(OptionType.INTEGER, "bet", "The amount you want to wager", true).setMinValue(1));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command data
        User user = event.getUser();
        long bet = event.getOption("bet").getAsLong();
        if (games.containsKey(user.getIdLong())) {
            String text = "You are already playing a game of crash!";
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }

        // Charge player for bet
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        long balance = economyHandler.getBalance(user.getIdLong());
        if (balance < bet) {
            String currency = economyHandler.getCurrency() + " **" + balance + "**";
            String text = "You don't have enough money for this bet. You currently have " + currency + " in cash.";
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }
        economyHandler.removeMoney(user.getIdLong(), bet);

        // Setup crash game
        double multiplier = 0.01 + (0.99 / ThreadLocalRandom.current().nextDouble());
        if (multiplier > 30) multiplier = 30;
        double finalMultiplier = multiplier;
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
                .setDescription("Your profit: " + economyHandler.getCurrency() + " " + EconomyHandler.FORMATTER.format(bet))
                .addField("Multiplier", "x1.00", false);

        // Start crash game
        String uuid = user.getId() + ":" + UUID.randomUUID();
        Button button = Button.primary("crash:cashout:"+uuid+":"+bet, "Cashout");
        ButtonListener.buttons.put(uuid, List.of(button));
        event.replyEmbeds(embed.build()).addActionRow(button).queue(msg -> {
            ScheduledFuture task = tasks.scheduleAtFixedRate(() -> {
                CrashGame game = games.get(user.getIdLong());
                game.currMultiplier += 0.1;
                String multiplierString = "x"+String.format("%.2f", game.currMultiplier);
                EmbedBuilder embed2 = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
                if (game.currMultiplier >= game.maxMultiplier) {
                    embed2.setColor(EmbedColor.ERROR.color);
                    embed2.setDescription("Result: Loss " + economyHandler.getCurrency() + " -" + EconomyHandler.FORMATTER.format(bet));
                    embed2.addField("Crashed At", multiplierString, false);
                    msg.editOriginalEmbeds(embed2.build()).queue();
                    games.remove(user.getIdLong()).task.cancel(true);
                    ButtonListener.buttons.remove(uuid);
                } else {
                    int profit = (int) (((double)bet)*game.currMultiplier);
                    embed2.setColor(EmbedColor.DEFAULT.color);
                    embed2.setDescription("Your profit: " + economyHandler.getCurrency() + " " + EconomyHandler.FORMATTER.format(profit));
                    embed2.addField("Multiplier", multiplierString, false);
                    msg.editOriginalEmbeds(embed2.build()).queue();
                }
            }, 1500, 1500, TimeUnit.MILLISECONDS);

            games.put(user.getIdLong(), new CrashGame(task, 1.0, finalMultiplier, bet));
        });
    }

    public static MessageEmbed cashout(Guild guild, User user) {
        // Cancel game
        CrashGame game = games.remove(user.getIdLong());
        game.task.cancel(true);

        // Award profit
        int profit = (int) (((double)game.bet)*game.currMultiplier);
        EconomyHandler econ = GuildData.get(guild).economyHandler;
        econ.addMoney(user.getIdLong(), profit);

        // Send result embed
        String multiplierString = "x"+String.format("%.2f", game.currMultiplier);
        return new EmbedBuilder()
            .setColor(EmbedColor.SUCCESS.color)
            .setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
            .setDescription("Result: Win " + econ.getCurrency() + " " + EconomyHandler.FORMATTER.format(profit))
            .addField("Multiplier", multiplierString, false)
            .build();
    }

    public static class CrashGame {

        ScheduledFuture task;
        double currMultiplier;
        double maxMultiplier;
        long bet;

        public CrashGame(ScheduledFuture task, double currMultiplier, double maxMultiplier, long bet) {
            this.task = task;
            this.currMultiplier = currMultiplier;
            this.maxMultiplier = maxMultiplier;
            this.bet = bet;
        }
    }
}
