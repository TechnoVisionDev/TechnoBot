package com.technovision.technobot.listeners;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.logging.AutoModLogger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class AutomodListener extends ListenerAdapter {

    public static final String ADVERTISE_CHANNEL = "advertise";
    private final AutoModLogger LOGGER;

    public AutomodListener(final TechnoBot bot) {
        LOGGER = bot.getAutoModLogger();
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        if (!event.getAuthor().isBot()) {
            if (event.getMessage().getMentionedMembers().size() > 0) {
                // Techno's ID
                long userId = 595024631438508070L;

                if (event.getMessage().getMentionedMembers().contains(event.getGuild().getMemberById(userId))) {
                    if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                        TextChannel channel = event.getGuild().getTextChannelsByName("RULES-AND-INFO", true).get(0);

                        event.getMessage().delete().queue();

                        event.getChannel()
                                .sendMessage(String.format("<@!%d>, do not ping TechnoVision! <#%s>",
                                        event.getAuthor().getIdLong(),
                                        channel.getId()))
                                .queue();

                        LOGGER.log(event.getGuild(),
                                event.getTextChannel(),
                                event.getAuthor(),
                                event.getJDA().getSelfUser(),
                                AutoModLogger.Infraction.PING);
                    }
                }
            } else if (message.toLowerCase().contains("discord.gg/")) {
                if (!event.getChannel().getName().equals(ADVERTISE_CHANNEL)) {
                    event.getMessage().delete().queue();

                    TextChannel channel = event.getGuild().getTextChannelsByName("RULES-AND-INFO", true).get(0);

                    event.getChannel()
                            .sendMessage(
                                    String.format("<@!%s>, you are not allowed to advertise in this channel! <#%s>",
                                            event.getAuthor().getId(),
                                            channel.getId()))
                            .queue();

                    LOGGER.log(event.getGuild(),
                            event.getTextChannel(),
                            event.getAuthor(),
                            event.getJDA().getSelfUser(),
                            AutoModLogger.Infraction.INVITE);
                }
            }
        }
    }
    
    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        String message = event.getMessage().getContentRaw();

        if (!event.getAuthor().isBot()) {
            if (event.getMessage().getMentionedMembers().size() > 0) {
                // Techno's ID
                long userId = 595024631438508070L;

                if (event.getMessage().getMentionedMembers().contains(event.getGuild().getMemberById(userId))) {
                    if (!event.getMember().hasPermission(Permission.KICK_MEMBERS)) {
                        TextChannel channel = event.getGuild().getTextChannelsByName("RULES-AND-INFO", true).get(0);

                        event.getMessage().delete().queue();

                        event.getChannel()
                                .sendMessage(String.format("<@!%d>, do not ping TechnoVision! <#%s>",
                                        event.getAuthor().getIdLong(),
                                        channel.getId()))
                                .queue();

                        LOGGER.log(event.getGuild(),
                                event.getTextChannel(),
                                event.getAuthor(),
                                event.getJDA().getSelfUser(),
                                AutoModLogger.Infraction.PING);
                    }
                }
            } else if (message.toLowerCase().contains("discord.gg/")) {
                if (!event.getChannel().getName().equals(ADVERTISE_CHANNEL)) {
                    event.getMessage().delete().queue();

                    TextChannel channel = event.getGuild().getTextChannelsByName("RULES-AND-INFO", true).get(0);

                    event.getChannel()
                            .sendMessage(
                                    String.format("<@!%s>, you are not allowed to advertise in this channel! <#%s>",
                                            event.getAuthor().getId(),
                                            channel.getId()))
                            .queue();

                    LOGGER.log(event.getGuild(),
                            event.getTextChannel(),
                            event.getAuthor(),
                            event.getJDA().getSelfUser(),
                            AutoModLogger.Infraction.INVITE);
                }
            }
        }
    }
}
