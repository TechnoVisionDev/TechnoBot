package technobot.commands.economy;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.economy.EconomyHandler;
import technobot.handlers.economy.EconomyReply;
import technobot.util.embeds.EmbedColor;

/**
 * Command that risks losing money for a greater potential reward.
 *
 * @author TechnoVision
 */
public class CrimeCommand extends Command {

    public CrimeCommand(TechnoBot bot) {
        super(bot);
        this.name = "crime";
        this.description = "Commit a crime for a chance at some extra money.";
        this.category = Category.ECONOMY;
    }

    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        long user = event.getUser().getIdLong();
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        Long timeout = economyHandler.getTimeout(user, EconomyHandler.TIMEOUT_TYPE.CRIME);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(event.getUser().getAsTag(), null, event.getUser().getEffectiveAvatarUrl());
        if (timeout != null && System.currentTimeMillis() < timeout) {
            // On timeout
            String timestamp = economyHandler.formatTimeout(timeout);
            embed.setDescription(":stopwatch: You can next commit a crime " + timestamp + ".");
            embed.setColor(EmbedColor.ERROR.color);
        } else {
            // Commit crime
            EconomyReply reply = economyHandler.crime(user);
            int color = reply.isSuccess() ? EmbedColor.SUCCESS.color : EmbedColor.ERROR.color;
            embed.setDescription(reply.getResponse());
            embed.setColor(color);
            embed.setFooter("Reply #" + reply.getId());
        }
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
