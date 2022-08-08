package technobot.commands.utility;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;
import technobot.util.localization.Poll;

import java.util.Arrays;
import java.util.List;

import static technobot.util.Localization.format;
import static technobot.util.Localization.get;

/**
 * Command that creates a quick poll with options and reactions.
 *
 * @author TechnoVision
 */
public class PollCommand extends Command {

    private static final List<String> NUMBER_EMOJIS = Arrays.asList(
            "1⃣", "2⃣", "3⃣", "4⃣", "5⃣", "6⃣", "7⃣", "8⃣", "9⃣", "🔟"
    );

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

        Poll pollText = get(s -> s.utility.poll);
        StringBuilder poll = new StringBuilder(format(pollText.message, event.getUser().getName()));

        OptionMapping choicesOption = event.getOption("choices");
        if (choicesOption != null) {
            // Create multi-choice poll
            String[] choices = choicesOption.getAsString().strip().split("\\s+");
            if (choices.length > 10) {
                event.getHook().sendMessageEmbeds(EmbedUtils.createError(
                        pollText.tooManyChoices
                )).queue();
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
                    msg.addReaction("👍").queue();
                    msg.addReaction("👎").queue();
                } catch (InsufficientPermissionException ignored) {
                }
            });
        }
    }
}
