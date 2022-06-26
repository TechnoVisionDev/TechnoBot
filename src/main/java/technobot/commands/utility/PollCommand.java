package technobot.commands.utility;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Command that creates a quick poll with options and reactions.
 *
 * @author TechnoVision
 */
public class PollCommand extends Command {

    private static final List<String> NUMBER_EMOJIS = Arrays.asList(
            "\u0031\u20E3",
            "\u0032\u20E3",
            "\u0033\u20E3",
            "\u0034\u20E3",
            "\u0035\u20E3",
            "\u0036\u20E3",
            "\u0037\u20E3",
            "\u0038\u20E3",
            "\u0039\u20E3",
            "\uD83D\uDD1F");

    public PollCommand(TechnoBot bot) {
        super(bot);
        this.name = "poll";
        this.description = "Create a quick poll with multiple options.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.STRING, "question", "The question to ask", true));
        this.args.add(new OptionData(OptionType.STRING, "choices", "Poll choices separated by spaces"));
    }

    public void execute(SlashCommandInteractionEvent event) {
        // Get user
        event.deferReply().queue();
        String question = event.getOption("question").getAsString();
        StringBuilder poll = new StringBuilder("**" + event.getUser().getName() + " asks:** " + question);

        OptionMapping choicesOption = event.getOption("choices");
        if (choicesOption != null) {
            // Create multi-choice poll
            String[] choices = choicesOption.getAsString().strip().split("\\s+");
            if (choices.length > 10) {
                event.getHook().sendMessageEmbeds(EmbedUtils.createError("You cannot have more than 10 choices!")).queue();
                return;
            }
            poll.append("\n");
            for (int i = 0; i < choices.length; i++) {
                poll.append("\n").append(NUMBER_EMOJIS.get(i)).append(": ").append(choices[i]);
            }
            event.getHook().sendMessage(poll.toString()).queue(msg -> {
                for (int i = 0; i < choices.length; i++) {
                    msg.addReaction(NUMBER_EMOJIS.get(i)).queue();
                }
            });
        } else {
            // Create simply upvote/downvote poll
            event.getHook().sendMessage(poll.toString()).queue(msg -> {
                try {
                    msg.addReaction("\uD83D\uDC4D").queue();
                    msg.addReaction("\uD83D\uDC4E").queue();
                } catch (InsufficientPermissionException ignored) { }
            });
        }
    }
}
