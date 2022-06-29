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

import static technobot.util.Localization.get;

/**
 * Command that responds to suggestions on the suggestion board.
 *
 * @author TechnoVision
 */
public class RespondCommand extends Command {

    public RespondCommand(TechnoBot bot) {
        super(bot);
        this.name = "respond";
        this.description = "Respond to a suggestion on the suggestion board.";
        this.category = Category.SUGGESTIONS;
        this.permission = Permission.MANAGE_SERVER;
        this.args.add(new OptionData(OptionType.STRING, "response", "The response to the suggestion", true)
                .addChoice("Approve", "APPROVE")
                .addChoice("Consider", "CONSIDER")
                .addChoice("Deny", "DENY")
                .addChoice("Implement", "IMPLEMENT"));
        this.args.add(new OptionData(OptionType.INTEGER, "number", "The suggestion number to respond to", true)
                .setMinValue(1)
                .setMaxValue(Integer.MAX_VALUE));
        this.args.add(new OptionData(OptionType.STRING, "reason", "The reason for your response"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String responseString = event.getOption("response").getAsString();
        SuggestionHandler.SuggestionResponse response = SuggestionHandler.SuggestionResponse.valueOf(responseString);

        String responseMessage = switch (response) {
            case APPROVE -> get(s -> s.suggestions.respond.responses.approved);
            case DENY -> get(s -> s.suggestions.respond.responses.denied);
            case CONSIDER -> get(s -> s.suggestions.respond.responses.considered);
            case IMPLEMENT -> get(s -> s.suggestions.respond.responses.implemented);
        };

        int id = event.getOption("number").getAsInt() - 1;
        String reason = event.getOption(
                "reason",
                get(s -> s.suggestions.respond.noReason) + "",
                OptionMapping::getAsString
        );

        GuildData.get(event.getGuild()).suggestionHandler.respond(
                event,
                id,
                reason,
                responseMessage,
                response.color
        );
    }
}
