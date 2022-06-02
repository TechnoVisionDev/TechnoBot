package technobot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.music.*;
import technobot.commands.staff.*;
import technobot.commands.starboard.StarboardCommand;
import technobot.commands.suggestions.*;
import technobot.commands.utility.*;
import technobot.data.GuildData;

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
        //Suggestions commands
        commands.add(new SuggestCommand(bot));
        commands.add(new ApproveCommand(bot));
        commands.add(new DenyCommand(bot));
        commands.add(new ConsiderCommand(bot));
        commands.add(new ImplementCommand(bot));
        commands.add(new SuggestionsCommand(bot));

        //Staff commands
        commands.add(new ClearCommand(bot));
        commands.add(new KickCommand(bot));
        commands.add(new BanCommand(bot));
        commands.add(new UnbanCommand(bot));
        commands.add(new WarnCommand(bot));
        commands.add(new WarningsCommand(bot));
        commands.add(new RemoveWarnCommand(bot));
        commands.add(new SlowmodeCommand(bot));
        commands.add(new LockCommand(bot));
        commands.add(new UnlockCommand(bot));
        commands.add(new SetNickCommand(bot));

        //Music commands
        commands.add(new PlayCommand(bot));
        commands.add(new SkipCommand(bot));
        commands.add(new QueueCommand(bot));
        commands.add(new SeekCommand(bot));
        commands.add(new PauseCommand(bot));
        commands.add(new ResumeCommand(bot));
        commands.add(new NowPlayingCommand(bot));
        commands.add(new RepeatCommand(bot));
        commands.add(new StopCommand(bot));
        commands.add(new VolumeCommand(bot));

        //Starboard commands
        commands.add(new StarboardCommand(bot));

        //Utility commands
        commands.add(new PingCommand(bot));
        commands.add(new AvatarCommand(bot));
        commands.add(new ServerCommand(bot));
        commands.add(new UserCommand(bot));
        commands.add(new RollCommand(bot));
        commands.add(new RolesCommand(bot));
        commands.add(new HelpCommand(bot)); // The 'help' command MUST come last!!!

        //Register commands as listeners
        for (Command command : commands) {
            bot.shardManager.addEventListener(command);
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
        List<CommandData> commandData = new ArrayList<>();
        for (Command command : commands) {
            SlashCommandData slashCommand = Commands.slash(command.name, command.description).addOptions(command.args);
            if (command.permission != null) {
                slashCommand.setDefaultPermissions(CommandPermissions.enabledFor(command.permission));
            }
        }
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }
}
