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
        event.deferReply().queue();
        // Get command data
        User user = event.getUser();
        User target = event.getOption("user").getAsUser();
        long amount = event.getOption("amount").getAsLong();
        EmbedBuilder embed = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        String currency = economyHandler.getCurrency();

        // Check that user has necessary funds
        long balance = economyHandler.getBalance(user.getIdLong());
        if (amount > balance) {
            String value = currency + " " + EconomyHandler.FORMATTER.format(balance);
            String text = "You don't have that much money to give. You currently have " + value + " on hand.";
            embed.setDescription(EmbedUtils.RED_X + text);
            embed.setColor(EmbedColor.ERROR.color);
            event.getHook().sendMessageEmbeds(embed.build()).queue();
            return;
        }

        // Pay target
        economyHandler.pay(user.getIdLong(), target.getIdLong(), amount);
        String value = currency + " " + EconomyHandler.FORMATTER.format(amount);

        // Send embed message
        embed.setDescription(EmbedUtils.GREEN_TICK + " <@" + target.getId() + "> has received your " + value);
        embed.setColor(EmbedColor.SUCCESS.color);
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
