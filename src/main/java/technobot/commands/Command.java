package technobot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a general slash command with properties.
 *
 * @author TechnoVision
 */
public abstract class Command extends ListenerAdapter {

    protected TechnoBot bot;
    protected String name;
    protected String description;
    protected Category category;
    protected List<OptionData> args;

    public Command(TechnoBot bot) {
        this.bot = bot;
        this.args = new ArrayList<>();
    }

    public abstract void execute(SlashCommandInteractionEvent event);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(name)) {
            execute(event);
        }
    }
}
