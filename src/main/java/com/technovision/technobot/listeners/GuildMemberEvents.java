package com.technovision.technobot.listeners;

import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Member Event Listener.
 *
 * @author TechnoVision
 */
public class GuildMemberEvents extends ListenerAdapter {

    public static String JOIN_MESSAGE;
    public static long JOIN_CHANNEL = 739158625800683591L;
    public static long JOIN_ROLE = 599348242538430494L;

    public static void loadJoinMessage() {
        StringBuilder msg = new StringBuilder();
        try {
            File file = new File("data/JoinMessage.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            Scanner s = new Scanner(file);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if (!line.isEmpty()) {
                    msg.append(line);
                }
                msg.append("\n");
            }
            s.close();
        } catch (IOException ignored) {
        }
        JOIN_MESSAGE = msg.toString();
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        // Join Message
        TextChannel channel = event.getGuild().getTextChannelById(JOIN_CHANNEL);
        User user = event.getUser();
        EmbedBuilder embed = new EmbedBuilder()
                .setDescription("**" + user.getAsTag() + "** has joined the server!")
                .setColor(EconManager.SUCCESS_COLOR);
        channel.sendMessage(embed.build()).queue();

        //Give Member Role
        if (user.isBot()) {
            return;
        }
        event.getGuild().addRoleToMember(event.getMember().getId(), event.getGuild().getRoleById(JOIN_ROLE)).queue();
        user.openPrivateChannel().queue((dm) -> dm.sendMessage(JOIN_MESSAGE).queue());
    }

    @Override
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        TextChannel channel = event.getGuild().getTextChannelById(JOIN_CHANNEL);
        EmbedBuilder embed = new EmbedBuilder()
                .setDescription("**" + event.getUser().getAsTag() + "** has left the server!")
                .setColor(Command.ERROR_EMBED_COLOR);
        channel.sendMessage(embed.build()).queue();
    }
}
