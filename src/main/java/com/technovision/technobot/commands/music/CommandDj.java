package com.technovision.technobot.commands.music;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.MusicManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandDj extends Command {

    public CommandDj() {
        super("dj", "Opens the DJ Panel", "{prefix}dj", Command.Category.MUSIC);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()) == null || MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler.getQueueCopy().size() == 0) {
            event.getChannel().sendMessage("There are no songs playing.").queue();
            return true;
        }
        MusicManager.TrackScheduler sch = MusicManager.getInstance().handlers.get(event.getGuild().getIdLong()).trackScheduler;
        EmbedBuilder emb = new EmbedBuilder()
                .setTitle("DJ Panel")
                .setDescription("<@!" + event.getAuthor().getId() + ">'s DJ Panel")
                .setColor(EMBED_COLOR);
        emb = MusicManager.getInstance().assembleEmbed(emb.build(), sch);
        event.getMessage().delete().complete();
        if (MusicManager.getInstance().djMessages.containsKey(event.getAuthor()))
            MusicManager.getInstance().djMessages.get(event.getAuthor()).delete().complete();
        Message msg = event.getChannel().sendMessage(emb.build()).complete();
        msg.addReaction("⏯").queue();
        msg.addReaction("\uD83D\uDD02").queue();
        msg.addReaction("⏭").queue();
        msg.addReaction("\uD83D\uDD01").queue();
        msg.addReaction("\uD83D\uDD00").queue();

        MusicManager.getInstance().djMessages.put(event.getAuthor(), msg);
        return true;
    }
}
