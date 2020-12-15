package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandShuffle extends Command {

    public CommandShuffle() {
        super("shuffle", "Shuffles queue", "{prefix}shuffle", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null || MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size() == 0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.shuffle();
        event.getChannel().sendMessage("\uD83D\uDD00 Shuffled Queue!").queue();
        return true;
    }
}
