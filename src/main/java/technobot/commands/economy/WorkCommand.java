package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.util.embeds.EmbedColor;

/**
 * Command that adds money to your balance.
 *
 * @author TechnoVision
 */
public class WorkCommand extends Command {

    public WorkCommand(TechnoBot bot) {
        super(bot);
        this.name = "work";
        this.description = "Work for some money.";
        this.category = Category.ECONOMY;
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long user = event.getUser().getIdLong();
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        Long timeout = economyHandler.getTimeout(user, EconomyHandler.TIMEOUT_TYPE.WORK);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl());
        if (timeout != null && System.currentTimeMillis() < timeout) {
            // On timeout
            String timestamp = economyHandler.getTimeoutFormatted(user, EconomyHandler.TIMEOUT_TYPE.WORK);
            embed.setDescription(":stopwatch: You can next work in " + timestamp + ".");
            embed.setColor(EmbedColor.ERROR.color);
        } else {
            // Work
            int amount = economyHandler.work(user);
            embed.setDescription("You work for the day and receive " + EconomyHandler.DEFAULT_CURRENCY + " " + amount + ".");
            embed.setColor(EmbedColor.SUCCESS.color);
        }
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
