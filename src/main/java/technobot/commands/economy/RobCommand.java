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
import technobot.handlers.economy.EconomyReply;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.localization.Localization.get;

/**
 * Command that steals money from another user.
 *
 * @author TechnoVision
 */
public class RobCommand extends Command {

    public RobCommand(TechnoBot bot) {
        super(bot);
        this.name = "rob";
        this.description = "Attempt to steal money from another user.";
        this.category = Category.ECONOMY;
        this.args.add(new OptionData(OptionType.USER, "user", "The user you want to rob.", true));
    }

    public void execute(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        User target = event.getOption("user").getAsUser();
        EmbedBuilder embed = new EmbedBuilder().setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl());
        if (user.getIdLong() == target.getIdLong()) {
            // Check for invalid target
            embed.setDescription(get(s -> s.economy().rob().robSelf()));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }
        if (target.isBot()) {
            // Check if target is a bot
            embed.setDescription(get(s -> s.economy().rob().robBots()));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        // Check for timeout
        EconomyHandler economyHandler = GuildData.get(event.getGuild()).economyHandler;
        Long timeout = economyHandler.getTimeout(user.getIdLong(), EconomyHandler.TIMEOUT_TYPE.ROB);
        if (timeout != null && System.currentTimeMillis() < timeout) {
            // On timeout
            String timestamp = economyHandler.formatTimeout(timeout);
            embed.setDescription(get(s -> s.economy().rob().timeout(), timestamp));
            embed.setColor(EmbedColor.ERROR.color);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            // Rob target
            EconomyReply reply = economyHandler.rob(user.getIdLong(), target.getIdLong());
            embed.setColor(reply.isSuccess() ? EmbedColor.SUCCESS.color : EmbedColor.ERROR.color);
            embed.setDescription(reply.getResponse());
            event.replyEmbeds(embed.build()).queue();
        }
    }
}
