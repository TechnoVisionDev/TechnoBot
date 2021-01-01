package com.technovision.technobot.commands.economy;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import com.technovision.technobot.util.exceptions.InvalidBalanceException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class CommandRob extends Command {
    private final TechnoBot bot;

    public CommandRob(final TechnoBot bot) {
        super("rob", "Steal cash from other members", "{prefix}rob [user]", Category.ECONOMY);
        this.bot = bot;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        if (args.length < 1) {
            embed.setColor(ERROR_EMBED_COLOR);
            embed.setDescription(":x: Too few arguments given.\n\nUsage:\n`rob [user]`");
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }

        User victim = null;
        List<Member> mentions = event.getMessage().getMentionedMembers();
        if (mentions.size() > 0) {
            victim = mentions.get(0).getUser();
        }

        if (victim == null) {
            try {
                victim = event.getGuild().getMemberById(args[0]).getUser();
            } catch (Exception ignored) {
            }
        }

        if (victim == null) {
            embed.setColor(ERROR_EMBED_COLOR);
            embed.setDescription(":x: Invalid `[user]` argument given\n\nUsage:\n`rob [user]`");
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }

        JSONObject victimProfile = bot.getEconomy().getProfile(victim);
        JSONObject robberProfile = bot.getEconomy().getProfile(event.getAuthor());
        long timestamp = robberProfile.getLong("rob-timestamp");
        int cooldown = 86400000;
        embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());
        embed.setColor(ERROR_EMBED_COLOR);
        if (System.currentTimeMillis() >= timestamp + cooldown) {
            Random rand = ThreadLocalRandom.current();
            if (rand.nextInt(10) > 5) { //40% Success Rate
                try {
                    long amount = bot.getEconomy().rob(robberProfile, victimProfile);
                    embed.setColor(EconManager.SUCCESS_COLOR);
                    embed.setDescription("You quickly swipe " + EconManager.SYMBOL + amount + " from " + args[0]);
                } catch (InvalidBalanceException e) {
                    embed.setDescription("That member does not have any money in their account!");
                }
            } else {
                int amount = rand.nextInt(400) + 1;
                bot.getEconomy().removeMoney(event.getAuthor(), amount, EconManager.Activity.NULL);
                embed.setDescription("You were caught and fined " + EconManager.SYMBOL + amount + " for theft!");
            }
        } else {
            embed.setDescription(":stopwatch: You cannot attempt to rob another member for " + bot.getEconomy().getCooldown(timestamp, cooldown) + ".");
        }
        robberProfile.put("rob-timestamp", System.currentTimeMillis());
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("steal");
    }
}
