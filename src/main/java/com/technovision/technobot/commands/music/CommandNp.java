package com.technovision.technobot.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandNp extends Command {

    public CommandNp() {
        super("np", "Displays the currently playing song and its duration/position", "{prefix}np", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null || MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size() == 0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        AudioTrack currentPlaying = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().get(0);
        String[] posString = new String[] {"⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯", "⎯",};
        try {
            posString[(int) Math.floor((float) currentPlaying.getPosition() / (float) currentPlaying.getDuration() * 30F)] = "~~◉~~";
        } catch (Exception e) {
            e.printStackTrace();
        }

        long msPos = currentPlaying.getPosition();
        long minPos = msPos / 60000;
        msPos = msPos % 60000;
        int secPos = (int) Math.floor((float) msPos / 1000f);

        long msDur = currentPlaying.getDuration();
        long minDur = msDur / 60000;
        msDur = msDur % 60000;
        int secDur = (int) Math.floor((float) msDur / 1000f);

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Now Playing :musical_note:", currentPlaying.getInfo().uri)
                .setDescription(currentPlaying.getInfo().title)
                .setColor(EMBED_COLOR)
                .addField("Position", String.join("", posString), false)
                .addField("Progress", minPos + ":" + ((secPos < 10) ? "0" + secPos : secPos) + " / " + minDur + ":" + ((secDur < 10) ? "0" + secDur : secDur), false);
        event.getChannel().sendMessage(builder.build()).queue();
        return true;
    }
}
