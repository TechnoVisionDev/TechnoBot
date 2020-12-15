package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLeave extends Command {

    public CommandLeave() {
        super("leave", "Leaves the voice channel", "{prefix}leave", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (event.getMember() == null || event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inVoiceChannel() || event.getMember().getVoiceState().getChannel() == null) {
            event.getChannel().sendMessage("You are not in a voice channel!").queue();
            return true;
        }
        MusicManager.getInstance().leaveVoiceChannel(event.getGuild(), event.getMember().getVoiceState().getChannel());
        event.getChannel().sendMessage("Left voice channel!").queue();
        return true;
    }
}
