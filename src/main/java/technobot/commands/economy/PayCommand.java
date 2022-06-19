package technobot.commands.economy;

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

import static technobot.util.localization.Localization.get;

/**
 * Command that transfer cash from one user to another.
 *
 * @author TechnoVision
 */
public class PayCommand extends Command {

    public PayCommand(TechnoBot bot) {
        super(bot);
        this.name = "pay";
        this.description = "Send money to another user.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.USER, "user", "The user you want to send money to.", true));
        this.args.add(new OptionData(OptionType.INTEGER, "amount", "The amount of money to send.", true).setMinValue(1));
    }

    public void execute(SlashCommandInteractionEvent event) {
        // Get command data
        User user = event.getUser();
        User target = event.getOption("user").getAsUser();
        EmbedBuilder embed = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        if (user.getIdLong() == target.getIdLong()) {
            // Check for invalid target
            embed.setDescription(get(s -> s.economy().pay().paySelf()));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if (target.isBot()) {
            // Check if target is a bot
            embed.setDescription(get(s -> s.economy().pay().payBots()));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        long amount = event.getOption("amount").getAsLong();
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;

        // Check that user has necessary funds
        long balance = economyHandler.getBalance(user.getIdLong());
        if (amount > balance) {
            embed.setDescription(get(
                    s -> s.economy().pay().notEnough(),
                    EconomyHandler.FORMATTER.format(balance)
            ));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        // Pay target
        economyHandler.pay(user.getIdLong(), target.getIdLong(), amount);

        // Send embed message
        embed.setDescription(get(
                s -> s.economy().pay().success(),
                target.getId(),
                EconomyHandler.FORMATTER.format(amount)
        ));
        embed.setColor(EmbedColor.SUCCESS.color);
        event.replyEmbeds(embed.build()).queue();
    }
}
