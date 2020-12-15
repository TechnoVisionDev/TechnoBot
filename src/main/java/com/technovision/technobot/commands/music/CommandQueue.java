package com.technovision.technobot.commands.music;

import com.google.common.collect.Sets;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class CommandQueue extends Command {

    public CommandQueue() {
        super("queue", "Displays a queue of songs", "{prefix}queue", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null) {
            embed.setDescription(":x: There's no song in the queue for me to play. **!play** a song first.");
            embed.setColor(ERROR_EMBED_COLOR);
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }
        List<AudioTrack> tracks = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy();
        if (tracks.size() == 0 || tracks.get(0) == null) {
            embed.setDescription(":x: There's no song in the queue for me to play. **!play** a song first.");
            embed.setColor(ERROR_EMBED_COLOR);
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }

        int totalLength = 0;
        String description = "";
        int finish = tracks.size();
        if (finish > 11) {
            finish = 11;
        }
        for (int i = 0; i < finish; i++) {
            AudioTrack track = tracks.get(i);
            long msPos = track.getInfo().length;
            long minPos = msPos / 60000;
            msPos = msPos % 60000;
            int secPos = (int) Math.floor((float) msPos / 1000f);
            String length = minPos + ":" + ((secPos < 10) ? "0" + secPos : secPos);
            String song = "[" + track.getInfo().title + "](" + track.getInfo().uri + ")";
            if (i == 0) {
                description += "__Now Playing:__";
            } else if (i == 1) {
                description += "\n__Up Next:__";
            }

            if (i == 0) {
                description += String.format("\n%s | `%s`\n", song, length);
            } else {
                description += String.format("\n`%d.` %s | `%s`\n", i, song, length);
            }
            totalLength += track.getInfo().length;
        }
        int minTime = totalLength / 60000;
        totalLength %= 60000;
        int secTime = totalLength / 1000;

        if (tracks.size() > 1) {
            description += "\n**" + (tracks.size() - 1) + " Songs in Queue | " + minTime + ":" + ((secTime < 10) ? "0" + secTime : secTime) + " Total Length**";
        }

        embed.setTitle("Music Queue :musical_note:");
        embed.setColor(EMBED_COLOR);
        embed.setDescription(description);

        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("q");
    }
}
