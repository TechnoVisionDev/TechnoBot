package technobot.commands.utility;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import java.util.Random;

import static technobot.util.Localization.get;

/**
 * Command to roll a die for random number generation.
 *
 * @author TechnoVision
 */
public class RollCommand extends Command {

    private final Random random;

    public RollCommand(TechnoBot bot) {
        super(bot);
        this.name = "roll";
        this.description = "Roll a dice.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.INTEGER, "dice", "The number of sides on the dice").setMinValue(1).setMaxValue(1000000));
        this.random = new Random();
    }

    public void execute(SlashCommandInteractionEvent event) {
        int bound = event.getOption("dice", 6, OptionMapping::getAsInt);
        if (bound == 0) bound = 1;
        int result = random.nextInt(bound) + 1;
        event.replyEmbeds(EmbedUtils.createDefault(
                get(s -> s.utility.roll, bound, result)
        )).queue();
    }
}
