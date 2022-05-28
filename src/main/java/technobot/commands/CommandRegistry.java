package technobot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.music.*;
import technobot.commands.utility.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers, listens, and executes commands.
 *
 * @author TechnoVision
 */
public class CommandRegistry extends ListenerAdapter {

    public static final ArrayList<Command> commands = new ArrayList<>();

    /**
     * Adds commands to a global list and registers them as event listener.
     *
     * @param bot An instance of CivBot.
     */
    public CommandRegistry(TechnoBot bot) {
        //Music commands
        commands.add(new PlayCommand(bot));
        commands.add(new SkipCommand(bot));
        commands.add(new QueueCommand(bot));
        commands.add(new SeekCommand(bot));
        commands.add(new PauseCommand(bot));
        commands.add(new ResumeCommand(bot));
        commands.add(new NowPlayingCommand(bot));

        //Utility commands
        commands.add(new PingCommand(bot));
        commands.add(new AvatarCommand(bot));
        commands.add(new ServerCommand(bot));
        commands.add(new RollCommand(bot));
        commands.add(new HelpCommand(bot)); // The 'help' command MUST come last!!!

        //Register commands as listeners
        for (Command command : commands) {
            bot.shardManager.addEventListener(command);
        }
    }

    /**
     * Registers slash commands as guild commands.
     * TEMPORARY! CHANGE TO GLOBAL COMMANDS ON RELEASE!
     *
     * @param event executes when a guild is ready.
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        for (Command command : commands) {
            commandData.add(Commands.slash(command.name, command.description).addOptions(command.args));
        }
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
