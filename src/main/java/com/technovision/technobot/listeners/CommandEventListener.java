package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.logging.Logger;
import com.technovision.technobot.util.BotRegistry;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * Command Listener and Executor.
 *
 * @author TechnoVision
 * @author Sparky
 */
public class CommandEventListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        String[] mArray = event.getMessage().getContentRaw().split(" ");
        String command = mArray[0];
        if (command.startsWith(Command.PREFIX)) {
            String[] args = new String[mArray.length - 1];
            for (int i = 0; i < mArray.length; i++) {
                if (i > 0) args[i - 1] = mArray[i];
            }

            BotRegistry registry = TechnoBot.getInstance().getRegistry();

            for (Command cmd : registry.getCommands()) {
                if ((Command.PREFIX + cmd.name).equalsIgnoreCase(command)) {
                    if (!cmd.execute(event, args)) {
                        TechnoBot.getInstance().getLogger().log(Logger.LogLevel.SEVERE, "Command '" + cmd.name + "' failed to execute!");
                    }
                    return;
                }
                if (cmd.getAliases().contains(command.substring(1).toLowerCase())) {
                    if (!cmd.execute(event, args)) {
                        TechnoBot.getInstance().getLogger().log(Logger.LogLevel.SEVERE, "Command '" + cmd.name + "' failed to execute!");
                    }
                    return;
                }
            }
        }
    }
}
