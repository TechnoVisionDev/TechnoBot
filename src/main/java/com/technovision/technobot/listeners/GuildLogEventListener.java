package com.technovision.technobot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.technovision.technobot.TechnoBot;
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * Bot Log Handler.
 * Listens to various actions and
 * logs them to a specified channel.
 *
 * @author Sparky
 */
public class GuildLogEventListener extends ListenerAdapter {

    public static final int RED = 0xdd5f53;
    public static final int GREEN = 0x53ddac;

    private final WebhookClient webhook = new WebhookClientBuilder(TechnoBot.getInstance().getBotConfig().getJson().getString("guildlogs-webhook")).build();

    @Override
    public void onRoleCreate(@Nonnull RoleCreateEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("New Role Created", null));
            setColor(GREEN);
            setDescription("**Name:** " + event.getRole().getName() +
                           "\n**Color:** #" + event.getRole().getColorRaw() +
                           "\n**Mentionable:** " + event.getRole().isMentionable() +
                           "\n**Displayed Separately:** " + event.getRole().isHoisted());
            setFooter(new WebhookEmbed.EmbedFooter("Role ID: " + event.getRole().getIdLong(), null));
            setTimestamp(new Date().toInstant());
        }}.build());
    }

    @Override
    public void onRoleDelete(@Nonnull RoleDeleteEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Role \"" + event.getRole().getName() + "\" Removed", null));
            setColor(RED);
            setDescription("**Name:** " + event.getRole().getName() +
                           "\n**Color:** #" + event.getRole().getColorRaw() +
                           "\n**Mentionable:** " + event.getRole().isMentionable() +
                           "\n**Displayed Separately:** " + event.getRole().isHoisted() +
                           "\n**Position:** " + event.getRole().getPositionRaw());
            setFooter(new WebhookEmbed.EmbedFooter("Role ID: " + event.getRole().getIdLong(), null));
            setTimestamp(new Date().toInstant());
        }}.build());
    }

    @Override
    public void onTextChannelDelete(@Nonnull TextChannelDeleteEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Text Channel Deleted", null));
            setColor(RED);
            setDescription("**Name:** " + event.getChannel().getName() +
                           "\n**Category:** " + event.getChannel().getParent().getName());
            setFooter(new WebhookEmbed.EmbedFooter("Channel ID: " + event.getChannel().getIdLong(), null));
            setTimestamp(new Date().toInstant());
        }}.build());
    }

    @Override
    public void onTextChannelCreate(@Nonnull TextChannelCreateEvent event) {
        webhook.send(new WebhookEmbedBuilder() {{
            setTitle(new WebhookEmbed.EmbedTitle("Text Channel Created", null));
            setColor(GREEN);
            setDescription("**Name:** " + event.getChannel().getName() +
                           "\n**Category:** " + event.getChannel().getParent().getName());
            setFooter(new WebhookEmbed.EmbedFooter("Channel ID: " + event.getChannel().getIdLong(), null));
            setTimestamp(new Date().toInstant());
        }}.build());
    }
}
