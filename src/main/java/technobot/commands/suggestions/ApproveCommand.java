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
 * Command that responds to suggestions with "Approved".
 *
 * @author TechnoVision
 */
public class ApproveCommand extends Command {

    public ApproveCommand(TechnoBot bot) {
        super(bot);
        this.name = "approve";
        this.description = "Approves a suggestion on the suggestion board.";
        this.category = Category.SUGGESTIONS;
        this.permission = Permission.MANAGE_SERVER;
        this.args.add(new OptionData(OptionType.INTEGER, "number", "The suggestion number to approve", true));
        this.args.add(new OptionData(OptionType.STRING, "reason", "The reason for approval"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        int id = event.getOption("number").getAsInt() - 1;
        OptionMapping reason = event.getOption("reason");
        GuildData.get(event.getGuild()).suggestionHandler.respond(event, id, reason, SuggestionHandler.SuggestionResponse.APPROVE);
    }
}
