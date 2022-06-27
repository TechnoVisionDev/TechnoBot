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

import java.util.concurrent.ThreadLocalRandom;

import static technobot.util.Localization.get;

/**
 * Command that generates a cute picture from reddit.
 *
 * @author TechnoVision
 */
public class EightBallCommand extends Command {

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
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.fun.eightBall.tooLong)
            )).queue();
            return;
        }

        var responses = get(s -> s.fun.eightBall.responses);

        int index = ThreadLocalRandom.current().nextInt(responses.size());
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle(question)
                .setDescription(":8ball: " + responses.get(index));
        event.replyEmbeds(embed.build()).queue();
    }
}
