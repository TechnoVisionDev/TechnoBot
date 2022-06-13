package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
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

/**
 * Command that shows your current cash and bank balance on the server.
 *
 * @author TechnoVision
 */
public class BalanceCommand extends Command {

    public BalanceCommand(TechnoBot bot) {
        super(bot);
        this.name = "balance";
        this.description = "Check your current balance.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.USER, "user", "See another user's balance"));
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Get user
        OptionMapping userOption = event.getOption("user");
        User user = (userOption != null) ? userOption.getAsUser() : event.getUser();

        // Get balance and bank values
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        Economy profile = economyHandler.getProfile(user.getIdLong());
        Long balance;
        Long bank;
        long total;
        if (profile != null) {
            balance = profile.getBalance();
            if (balance == null) balance = 0L;
            bank = profile.getBank();
            if (bank == null) bank = 0L;
        } else {
            balance = 0L;
            bank = 0L;
        }
        total = balance + bank;

        // Send embed message
        String currency = economyHandler.getCurrency();
        EmbedBuilder embed = new EmbedBuilder()
            .setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
            .setDescription("Leaderboard Rank: #" + economyHandler.getRank(user.getIdLong()))
            .addField("Cash:", currency + " " + balance, true)
            .addField("Bank:", currency + " " + bank, true)
            .addField("Total:", currency + " " + total, true)
            .setColor(EmbedColor.DEFAULT.color);
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
