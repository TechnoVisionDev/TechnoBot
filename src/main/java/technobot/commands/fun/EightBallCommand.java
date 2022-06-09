package technobot.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Command that generates a cute picture from reddit.
 *
 * @author TechnoVision
 */
public class EightBallCommand extends Command {

    private static final List<String> responses = Arrays.asList(
            "I can tell you certainly, no.",
            "I'm not sure but ur def stupid.",
            "It is certain.",
            "Without a doubt.",
            "You may rely on it.",
            "As I see it, yes.",
            "Most likely.",
            "Signs point to yes.",
            "Reply hazy try again.",
            "Better not tell you now.",
            "Hmm imma just let u figure it out.",
            "Don't count on it.",
            "Outlook not so good.",
            "My sources say no.");

    public EightBallCommand(TechnoBot bot) {
        super(bot);
        this.name = "8ball";
        this.description = "Ask the magic 8ball a question.";
        this.category = Category.FUN;
        this.args.add(new OptionData(OptionType.STRING, "question", "The question to ask the 8ball", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String question = event.getOption("question").getAsString();
        if (question.length() > 250) {
            event.replyEmbeds(EmbedUtils.createError("The 8ball doesn't like questions longer than 250 characters!")).queue();
            return;
        }

        int index = ThreadLocalRandom.current().nextInt(14);
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle(question)
                .setDescription(":8ball: " + responses.get(index));
        event.replyEmbeds(embed.build()).queue();
    }
}
