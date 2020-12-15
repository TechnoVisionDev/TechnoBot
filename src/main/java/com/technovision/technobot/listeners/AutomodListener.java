package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.logging.AutoModLogger;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class AutomodListener extends ListenerAdapter {

    public static final String ADVERTISE_CHANNEL = "advertise";
    public static final AutoModLogger LOGGER = TechnoBot.getInstance().getAutoModLogger();

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.getMessage().getMentionedMembers().size() > 0) {
            if (event.getMessage().getMentionedMembers().contains(event.getGuild().getMemberById(595024631438508070L))) {
                TextChannel channel = event.getGuild().getTextChannelsByName("RULES-AND-INFO", true).get(0);
                event.getMessage().delete().queue();
                event.getChannel().sendMessage("<@!" + event.getAuthor().getIdLong() + ">, do not ping TechnoVision! <#" + channel.getId() + ">").queue();
                LOGGER.log(event.getGuild(), event.getTextChannel(), event.getAuthor(), event.getJDA().getSelfUser(), AutoModLogger.Infraction.PING);
            }
        } else if (message.toLowerCase().contains("discord.gg/")) {
            if (!event.getChannel().getName().equals(ADVERTISE_CHANNEL)) {
                event.getMessage().delete().queue();
                TextChannel channel = event.getGuild().getTextChannelsByName("RULES-AND-INFO", true).get(0);
                event.getChannel().sendMessage("<@!" + event.getAuthor().getId() + ">, " + "you are not allowed to advertise in this channel! <#" + channel.getId() + ">").queue();
                LOGGER.log(event.getGuild(), event.getTextChannel(), event.getAuthor(), event.getJDA().getSelfUser(), AutoModLogger.Infraction.INVITE);
            }
        }
    }
}
