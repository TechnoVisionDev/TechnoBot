package com.technovision.technobot.logging;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.GuildLogEventListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Date;

public class AutoModLogger {

    private final String log_channel;
    private final EmbedBuilder embed;

    public AutoModLogger() {
        log_channel = "auto-moderation";
        embed = new EmbedBuilder();
    }

    public void log(Guild guild, TextChannel channel, User offender, User moderator, Infraction infraction, String reason) {
        String desc = "";
        switch (infraction) {
            case BAN:
                embed.setColor(GuildLogEventListener.RED);
                embed.setTitle("Banned " + offender.getAsTag());
                desc += "**Reason:** " + reason;
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
            case KICK:
                embed.setColor(GuildLogEventListener.RED);
                embed.setTitle("Kicked " + offender.getAsTag());
                desc += "**Reason:** " + reason;
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
            case WARN:
                embed.setColor(Command.EMBED_COLOR);
                embed.setTitle("Warned " + offender.getAsTag());
                desc += "**Channel:** <#" + channel.getIdLong() + ">";
                desc += "\n**Reason:** " + reason;
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
            case MUTE:
                embed.setColor(GuildLogEventListener.RED);
                embed.setTitle("Muted " + offender.getAsTag());
                desc += "**Reason:** " + reason;
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
        }
        embed.setThumbnail(offender.getEffectiveAvatarUrl());
        embed.setDescription(desc);
        embed.setTimestamp(new Date().toInstant());
        guild.getTextChannelsByName(log_channel, true).get(0).sendMessage(embed.build()).queue();
        embed.clear();
    }


    public void log(Guild guild, TextChannel channel, User offender, User moderator, Infraction infraction) {
        embed.setColor(Command.EMBED_COLOR);
        String desc = "";
        switch (infraction) {
            case PING:
                embed.setTitle("Ping TechnoVision");
                desc += "**Channel:** <#" + channel.getIdLong() + ">";
                desc += "\n**Offender:** " + offender.getAsTag() + " <@!" + offender.getIdLong() + ">";
                desc += "\n**Reason:** Automatic action carried out for pinging TechnoVision.";
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
            case INVITE:
                embed.setTitle("Invalid Advertisement");
                desc += "**Channel:** <#" + channel.getIdLong() + ">";
                desc += "\n**Offender:** " + offender.getAsTag() + " <@!" + offender.getIdLong() + ">";
                desc += "\n**Reason:** Automatic action carried out for posting an invite.";
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
            case SWEAR:
                embed.setTitle("Racism & Profanity");
                desc += "**Channel:** <#" + channel.getIdLong() + ">";
                desc += "\n**Offender:** " + offender.getAsTag() + " <@!" + offender.getIdLong() + ">";
                desc += "\n**Reason:** Automatic action carried out for using a blacklisted word.";
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
            case CLEAR:
                embed.setTitle("Channel Purge");
                desc += "**Channel:** <#" + channel.getIdLong() + ">";
                desc += "\n**Moderator:** " + offender.getAsTag();
                break;
            case UNMUTE:
                embed.setColor(Command.EMBED_COLOR);
                embed.setTitle("Un-Muted " + offender.getAsTag());
                embed.setThumbnail(offender.getEffectiveAvatarUrl());
                desc += "\n**Moderator:** " + moderator.getAsTag();
                break;
        }
        embed.setDescription(desc);
        embed.setTimestamp(new Date().toInstant());
        guild.getTextChannelsByName(log_channel, true).get(0).sendMessage(embed.build()).queue();
        embed.clear();
    }

    public enum Infraction {
        PING, INVITE, SWEAR, BAN, KICK, WARN, CLEAR, MUTE, UNMUTE
    }
}
