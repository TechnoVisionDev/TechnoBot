package com.technovision.technobot.listeners;

import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ExtrasEventListener extends ListenerAdapter {
    private static final Map<String, Long> COOLDOWN_MAP = new HashMap<>();

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String authorId = event.getAuthor().getId();
        String msg = event.getMessage().getContentRaw().toLowerCase();
        boolean triggered = false;

        if (COOLDOWN_MAP.containsKey(authorId)) {
            if (COOLDOWN_MAP.get(authorId) + 120000 < System.currentTimeMillis()) { //2 minutes
                return;
            } else {
                COOLDOWN_MAP.remove(authorId);
            }
        }

        if (msg.contains("why no work")) {
            event.getChannel().sendMessage("Please explain your issue. 'why no work' doesn't help!").queue();
            triggered = true;

        } else if (msg.contains("will this work")) {
            event.getChannel().sendMessage("https://tryitands.ee/").queue();
            triggered = true;

        } else if (msg.startsWith("i need help") && event.getMessage().getContentRaw().split(" ").length < 7) {
            event.getChannel().sendMessage("https://dontasktoask.com/").queue();
            triggered = true;

        } else if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
            String reply = "";

            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0:
                    reply = "Uhhh, do you need something?";
                    break;
                case 1:
                    reply = "Hi bestie :P";
                    break;
                case 2:
                    reply = "Gimme a sec I'm busy!";
                    break;
                case 3:
                    reply = "Why do people ping me so much!!! :angry:";
                    break;
            }

            event.getChannel().sendMessage(reply).queue();
            triggered = true;

        } else if (msg.contains("@everyone")) {
            String reply = "";

            switch (ThreadLocalRandom.current().nextInt(4)) {
                case 0:
                    reply = "<@!" + event.getMember().getUser().getId() + ">, did you *really* think that would work?";
                    break;
                case 1:
                    reply = "Nice try, but you have no power here " + "<@!" + event.getMember().getUser().getId() + ">.";
                    break;
                case 2:
                    reply = "That didn't ping anybody genius.";
                    break;
                case 3:
                    reply = "Bet that worked in your head, didn't it " + "<@!" + event.getMember().getUser().getId() + ">?";
                    break;
            }

            event.getChannel().sendMessage(reply).queue();
            event.getMessage().addReaction("😠").queue();
            triggered = true;

        } else if (msg.toLowerCase().contains("forge") && (msg.toLowerCase().contains("tutorials") || msg.toLowerCase().contains("support") || msg.toLowerCase().contains("help"))) {
            MessageEmbed embed = new EmbedBuilder()
                    .setColor(Command.EMBED_COLOR)
                    .setTitle("Forge is Not Supported Here!")
                    .setDescription("The Forge tutorials have been discontinued, and thus no support for Forge will be given. We recommend switching to Fabric as an alternative or joining the official Forge discord for support. Click [HERE](https://discord.com/channels/599343917732986900/739158890104750160/791902360267522068) for more info!")
                    .addField("Official Forge Discord", "https://discord.gg/UvedJ9m", false)
                    .build();
            event.getChannel().sendMessage(embed).queue();
        } else if (msg.equalsIgnoreCase("pog")) event.getMessage().addReaction(":Pog:").queue();
        else if (msg.equalsIgnoreCase("pogu")) event.getMessage().addReaction(":PogU:").queue();

        if (triggered) COOLDOWN_MAP.put(authorId, System.currentTimeMillis());
    }

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        Member member = event.getMember();
        event.getGuild().getTextChannelById(792982347381342248L).upsertPermissionOverride(member)
                .setAllow(Permission.VIEW_CHANNEL)
                .queue();
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        Member member = event.getMember();
        event.getGuild().getTextChannelById(792982347381342248L).upsertPermissionOverride(member)
                .setDeny(Permission.VIEW_CHANNEL)
                .queue();
    }
}
