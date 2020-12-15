package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSeek extends Command {

    public CommandSeek() {
        super("seek", "Seek to a position in the currently playing song", "{prefix}seek <seconds>", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null || MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size() == 0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        try {
            MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).setPosition(Math.min(Integer.parseInt(args[0]) * 1000, MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).getDuration()));
        } catch (IndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Please specify a time to seek to!").queue();
            return true;
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("Please specify a *number*!").queue();
            return true;
        }
        event.getChannel().sendMessage("Seeked to " + args[0] + " seconds on song `" + MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0).getInfo().title + "`!").queue();
        return true;
    }
}
