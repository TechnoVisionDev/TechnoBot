package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandResume extends Command {

    public CommandResume() {
        super("resume", "Resumes the player", "{prefix}resume", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null || MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size() == 0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        MusicManager.TrackScheduler sch = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler;
        sch.setPaused(false);
        event.getChannel().sendMessage(":arrow_forward: Unpaused the Player!").queue();
        return true;
    }
}
