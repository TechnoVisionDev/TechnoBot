package technobot.commands.suggestions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.SuggestionHandler;

/**
 * Command that responds to suggestions with "Implemented".
 *
 * @author TechnoVision
 */
public class ImplementCommand extends Command {

    public ImplementCommand(TechnoBot bot) {
        super(bot);
        this.name = "implement";
        this.description = "Implements a suggestion on the suggestion board.";
        this.category = Category.SUGGESTIONS;
        this.permission = Permission.MANAGE_SERVER;
        this.args.add(new OptionData(OptionType.INTEGER, "number", "The suggestion number to implement", true));
        this.args.add(new OptionData(OptionType.STRING, "reason", "The reason for being implemented"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int id = event.getOption("number").getAsInt() - 1;
        OptionMapping reason = event.getOption("reason");
        GuildData.get(event.getGuild()).suggestionHandler.respond(event, id, reason, SuggestionHandler.SuggestionResponse.IMPLEMENT);
    }
}
