package technobot.commands.greetings;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.GreetingHandler;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that configures auto greetings.
 *
 * @author TechnoVision
 */
public class GreetCommand extends Command {

    public GreetCommand(TechnoBot bot) {
        super(bot);
        this.name = "greet";
        this.description = "Set a greeting to be sent to the welcome channel when a member joins.";
        this.category = Category.GREETINGS;
        this.args.add(new OptionData(OptionType.STRING, "message", "The message to send as a greeting"));
        this.permission = Permission.MANAGE_SERVER;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GreetingHandler greetingHandler = GuildData.get(event.getGuild()).greetingHandler;
        OptionMapping greetingOption = event.getOption("message");

        // Remove greeting message
        if (greetingOption == null) {
            greetingHandler.removeGreet();
            String text = EmbedUtils.BLUE_X + " Greeting message successfully removed!";
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }

        // Set greeting message
        greetingHandler.setGreet(greetingOption.getAsString());
        String text = EmbedUtils.BLUE_TICK + " Greeting message successfully updated!";
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
