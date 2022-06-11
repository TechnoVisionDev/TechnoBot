package technobot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.fun.*;
import technobot.commands.greetings.FarewellCommand;
import technobot.commands.greetings.GreetCommand;
import technobot.commands.greetings.GreetingsCommand;
import technobot.commands.greetings.JoinDMCommand;
import technobot.commands.levels.*;
import technobot.commands.music.*;
import technobot.commands.staff.*;
import technobot.commands.starboard.StarboardCommand;
import technobot.commands.suggestions.*;
import technobot.commands.utility.*;
import technobot.data.GuildData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers, listens, and executes commands.
 *
 * @author TechnoVision
 */
public class CommandRegistry extends ListenerAdapter {

    /** List of commands in the exact order registered */
    public static final List<Command> commands = new ArrayList<>();

    /** Map of command names to command objects */
    public static final Map<String, Command> commandsMap = new HashMap<>();

    /**
     * Adds commands to a global list and registers them as event listener.
     *
     * @param bot An instance of TechnoBot.
     */
    public CommandRegistry(TechnoBot bot) {
        mapCommand(
                //Greeting commands
                new GreetCommand(bot),
                new FarewellCommand(bot),
                new JoinDMCommand(bot),
                new GreetingsCommand(bot),

                //Fun commands
                new JokeCommand(bot),
                new MemeCommand(bot),
                new CuteCommand(bot),
                new NsfwCommand(bot),
                new EightBallCommand(bot),
                new GoogleCommand(bot),
                new RedditCommand(bot),
                new SurpriseCommand(bot),

                //Leveling commands
                new RankCommand(bot),
                new TopCommand(bot),
                new RewardsCommand(bot),
                new RankcardCommand(bot),
                new LevelingCommand(bot),

                //Suggestions commands
                new SuggestCommand(bot),
                new RespondCommand(bot),
                new SuggestionsCommand(bot),

                //Staff commands
                new ClearCommand(bot),
                new KickCommand(bot),
                new BanCommand(bot),
                new UnbanCommand(bot),
                new WarnCommand(bot),
                new WarningsCommand(bot),
                new RemoveWarnCommand(bot),
                new SlowmodeCommand(bot),
                new LockCommand(bot),
                new UnlockCommand(bot),
                new RoleCommand(bot),
                new SetNickCommand(bot),

                //Music commands
                new PlayCommand(bot),
                new SkipCommand(bot),
                new QueueCommand(bot),
                new SeekCommand(bot),
                new PauseCommand(bot),
                new ResumeCommand(bot),
                new NowPlayingCommand(bot),
                new RepeatCommand(bot),
                new StopCommand(bot),
                new VolumeCommand(bot),

                //Starboard commands
                new StarboardCommand(bot),

                //Utility commands
                new PingCommand(bot),
                new AvatarCommand(bot),
                new ServerCommand(bot),
                new UserCommand(bot),
                new RollCommand(bot),
                new RolesCommand(bot),
                new PollCommand(bot),
                new HelpCommand(bot) // The 'help' command MUST come last!!!
        );
    }

    /**
     * Adds a command to the static list and map.
     *
     * @param cmds a spread list of command objects.
     */
    private void mapCommand(Command ...cmds) {
        for (Command cmd : cmds) {
            commandsMap.put(cmd.name, cmd);
            commands.add(cmd);
        }
    }

    public static List<CommandData> unpackCommandData() {
        // Register slash commands
        List<CommandData> commandData = new ArrayList<>();
        for (Command command : commands) {
            SlashCommandData slashCommand = Commands.slash(command.name, command.description).addOptions(command.args);
            if (command.permission != null) {
                slashCommand.setDefaultPermissions(CommandPermissions.enabledFor(command.permission));
            }
            if (!command.subCommands.isEmpty()) {
                slashCommand.addSubcommands(command.subCommands);
            }
            commandData.add(slashCommand);
        }
        return commandData;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String name = event.getName();
        Command cmd = commandsMap.get(name);
        if (cmd != null) {
            cmd.execute(event);
        }
    }

    /**
     * Registers slash commands as guild commands.
     * NOTE: May change to global commands on release.
     *
     * @param event executes when a guild is ready.
     */
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        // Get GuildData from database
        GuildData.get(event.getGuild());
        // Register slash commands
        event.getGuild().updateCommands().addCommands(unpackCommandData()).queue();
    }
}
