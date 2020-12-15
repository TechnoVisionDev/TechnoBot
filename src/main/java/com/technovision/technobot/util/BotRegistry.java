package com.technovision.technobot.util;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.logging.Loggable;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bot registry maintaining listeners and commands.
 *
 * @author Sparky
 */
@Loggable(display = "BotRegistry")
public class BotRegistry {
    private final Logger logger = new Logger(this);
    private final List<ListenerAdapter> eventListeners = new ArrayList<ListenerAdapter>();
    private final List<Command> commands = new ArrayList<Command>();

    public BotRegistry() {
    }

    public void registerEventListener(@NotNull ListenerAdapter listener) {
        eventListeners.add(listener);
    }

    public void registerEventListeners(@NotNull ListenerAdapter... listeners) {
        Arrays.asList(listeners).forEach(this::registerEventListener);
    }

    public void addListeners(@NotNull JDA jda) {
        logger.log(Logger.LogLevel.INFO, "Beginning EventListener Initialization Process");

        for (ListenerAdapter listener : eventListeners) {
            logger.log(Logger.LogLevel.INFO, "Adding EventListener: " + listener.getClass().getName());
            jda.addEventListener(listener);
        }

        logger.log(Logger.LogLevel.INFO, "Finished EventListener Initialization Process");
    }

    public void registerCommands(Command... commands) {
        logger.log(Logger.LogLevel.INFO, "Beginning Command Registry Protocol for " + commands.length + " commands.");
        for (Command c : commands) {
            logger.log(Logger.LogLevel.INFO, "Registering Command: " + c.name);
        }
        this.commands.addAll(Arrays.asList(commands));
        logger.log(Logger.LogLevel.INFO, "Finished Command Registry Protocol");
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(commands);
    }
}
