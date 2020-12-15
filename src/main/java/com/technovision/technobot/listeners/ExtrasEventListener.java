package com.technovision.technobot.listeners;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
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
        } else if (msg.contains("1.12") || msg.contains("1.13") || msg.contains("1.14")) {
            event.getChannel().sendMessage("This version of Forge is outdated and you will not be given help for it. This is due to forge ending LTS for the version. Update to 1.15+!").queue();
            triggered = true;
        } else if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
            event.getChannel().sendMessage("Uhhh, do you need something?").queue();
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
            event.getMessage().addReaction("\uD83D\uDE20").queue();
            triggered = true;
        }

        if (triggered) COOLDOWN_MAP.put(authorId, System.currentTimeMillis());
    }
}
