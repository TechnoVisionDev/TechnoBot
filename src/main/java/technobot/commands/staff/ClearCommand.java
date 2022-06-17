package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

/**
 * Purges a channel of a specified number of messages.
 *
 * @author TechnoVision
 */
public class ClearCommand extends Command {

    public ClearCommand(TechnoBot bot) {
        super(bot);
        this.name = "clear";
        this.description = "Purges messages in the current channel.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.INTEGER, "amount", "Number of messages to clear", true)
                .setMinValue(1)
                .setMaxValue(100));
        this.permission = Permission.MANAGE_SERVER;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        int amount = event.getOption("amount").getAsInt();
        event.getChannel().getHistory().retrievePast(Math.min(amount + 1, 100)).queue(messages -> {
            try {
                // Delete messages and notify user
                ((TextChannel) event.getChannel()).deleteMessages(messages).queue(result -> {
                    String text = ":ballot_box_with_check: I have deleted `%d messages!`".formatted(amount);
                    event.getHook().sendMessage(text).queue();
                });
            } catch (IllegalArgumentException e) {
                // Messages were older than 2 weeks
                String text = "You cannot clear messages older than 2 weeks!";
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(text)).queue();
            }
        });
    }
}
