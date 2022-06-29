package technobot.commands.utility;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.listeners.AfkListener;

import java.util.Date;

/**
 * Command that allows a user to set their AFK status message.
 *
 * @author TechnoVision
 */
public class AfkCommand extends Command {

    public AfkCommand(TechnoBot bot) {
        super(bot);
        this.name = "afk";
        this.description = "Set an afk message to be sent to users who ping you.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.STRING, "message", "Your AFK message"));
    }

    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping option = event.getOption("message");
        String message = option != null ? option.getAsString() : "";
        AfkListener.AFK_MESSAGES.put(event.getMember(), new AfkListener.AfkStatus(message, new Date().toInstant()));
        event.reply(":wave: | **"+event.getMember().getEffectiveName()+"** has gone AFK. See you later!").queue();
    }
}
