package technobot.commands.utility;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import java.text.DecimalFormat;

import static technobot.util.Localization.get;

/**
 * Solves expressions like a calculator.
 *
 * @author TechnoVision
 */
public class MathCommand extends Command {

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.####");

    public MathCommand(TechnoBot bot) {
        super(bot);
        this.name = "math";
        this.description = "Calculate a mathematical expression.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.STRING, "expression", "The math expression to solve.", true));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String expression = event.getOption("expression").getAsString();
        try {
            Double result = new DoubleEvaluator().evaluate(expression);
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .addField(get(s -> s.utility.math.expression), "`" + expression + "`", false)
                    .addField(get(s -> s.utility.math.result), FORMATTER.format(result), false);
            event.replyEmbeds(embed.build()).queue();
        } catch (IllegalArgumentException e) {
            event.replyEmbeds(EmbedUtils.createError(get(
                    s -> s.utility.math.failure,
                    "(2 ^ 3 - 1) * sin(pi / 4) / ln(pi ^ 2)"
            ))).setEphemeral(true).queue();
        }
    }
}
