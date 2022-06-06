package technobot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import technobot.TechnoBot;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a general slash command with properties.
 *
 * @author TechnoVision
 */
public abstract class Command {

    public TechnoBot bot;
    public String name;
    public String description;
    public Category category;
    public List<OptionData> args;
    public List<SubcommandData> subCommands;
    public Permission permission;

    public Command(TechnoBot bot) {
        this.bot = bot;
        this.args = new ArrayList<>();
        this.subCommands = new ArrayList<>();
    }

    public abstract void execute(SlashCommandInteractionEvent event);
}
