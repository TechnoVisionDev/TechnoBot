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

import static technobot.util.localization.Localization.get;

/**
 * Command that configures auto farewells.
 *
 * @author TechnoVision
 */
public class FarewellCommand extends Command {

    public FarewellCommand(TechnoBot bot) {
        super(bot);
        this.name = "farewell";
        this.description = "Set a farewell to be sent to the welcome channel when a member leaves.";
        this.category = Category.GREETINGS;
        this.args.add(new OptionData(OptionType.STRING, "message", "The message to send as a farewell"));
        this.permission = Permission.MANAGE_SERVER;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GreetingHandler greetingHandler = GuildData.get(event.getGuild()).greetingHandler;
        OptionMapping farewellOption = event.getOption("message");

        // Remove farewell message
        if (farewellOption == null) {
            greetingHandler.removeFarewell();
            String text = get(s -> s.greeting().farewell().removed());
            event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
            return;
        }

        // Set greeting message
        greetingHandler.setFarewell(farewellOption.getAsString());
        String text = get(s -> s.greeting().farewell().set());
        event.getHook().sendMessageEmbeds(EmbedUtils.createDefault(text)).queue();
    }
}
