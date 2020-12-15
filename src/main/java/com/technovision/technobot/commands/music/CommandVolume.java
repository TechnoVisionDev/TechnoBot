package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandVolume extends Command {

    public CommandVolume() {
        super("volume", "Change volume of music", "{prefix}volume <volume>", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null || MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size() == 0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }

        if (Integer.parseInt(args[0]) >= 400 || Integer.parseInt(args[0]) <= 50) {
            event.getChannel().sendMessage("The volume is out of range! [50-400]").queue();
            return true;
        }

        try {
            MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.setVolume(Integer.parseInt(args[0]));
        } catch (IndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Please specify a volume!").queue();
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("That is not a number!").queue();
        }

        event.getChannel().sendMessage("🔈 Set volume to " + args[0] + "!").queue();
        return true;
    }
}
