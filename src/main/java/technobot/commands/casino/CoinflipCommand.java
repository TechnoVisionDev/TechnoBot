package technobot.commands.casino;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Command that plays a coinflip casino game.
 *
 * @author TechnoVision
 */
public class CoinflipCommand extends Command {

    public CoinflipCommand(TechnoBot bot) {
        super(bot);
        this.name = "coinflip";
        this.description = "Flip a coin and bet on heads or tails.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.STRING, "choice", "The side you think the coin will land on", true)
                .addChoice("heads", "heads")
                .addChoice("tails", "tails"));
        this.args.add(new OptionData(OptionType.INTEGER, "bet", "The amount you want to wager", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command data
        User user = event.getUser();
        String choice = event.getOption("choice").getAsString();
        long bet = event.getOption("bet").getAsLong();

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

        // Flip coin and calculate result
        EmbedBuilder embed = new EmbedBuilder();
        int result = ThreadLocalRandom.current().nextInt(2);
        String winnings = economyHandler.getCurrency() + " " + EconomyHandler.FORMATTER.format(bet*2);
        String losings = economyHandler.getCurrency() + " -" + EconomyHandler.FORMATTER.format(bet);
        if (result == 0) {
            embed.setAuthor("Heads!", null, user.getEffectiveAvatarUrl());
            if (choice.equalsIgnoreCase("heads")) {
                embed.setColor(EmbedColor.SUCCESS.color);
                embed.setDescription("Congratulations, the coin landed on heads!\nYou won " + winnings);
                economyHandler.addMoney(user.getIdLong(), bet*2);
            } else {
                embed.setColor(EmbedColor.ERROR.color);
                embed.setDescription("Sorry, the coin landed on heads.\nYou lost " + losings);
            }
        } else {
            embed.setAuthor("Tails!", null, user.getEffectiveAvatarUrl());
            if (choice.equalsIgnoreCase("tails")) {
                embed.setColor(EmbedColor.SUCCESS.color);
                embed.setDescription("Congratulations, the coin landed on tails!\nYou won " + winnings);
                economyHandler.addMoney(user.getIdLong(), bet*2);
            } else {
                embed.setColor(EmbedColor.ERROR.color);
                embed.setDescription("Sorry, the coin landed on tails.\nYou lost " + losings);
            }
        }

        // Send message response
        event.reply("<a:coinflip:993258934909550592> The coin flips into the air...").queue(msg -> {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    msg.retrieveOriginal().flatMap(hook -> hook.editMessage(" ").setEmbeds(embed.build())).queue();
                }
            }, 2500L);
        });
    }
}
