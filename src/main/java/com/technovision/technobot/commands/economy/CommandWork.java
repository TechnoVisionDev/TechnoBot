package com.technovision.technobot.commands.economy;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.concurrent.ThreadLocalRandom;

public class CommandWork extends Command {

    public CommandWork() {
        super("work", "Work for some cash", "{prefix}work", Command.Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        JSONObject profile = TechnoBot.getInstance().getEconomy().getProfile(event.getAuthor());
        long timestamp = profile.getLong("work-timestamp");
        int cooldown = 14400000;
        if (System.currentTimeMillis() >= timestamp + cooldown) {
            int amount = ThreadLocalRandom.current().nextInt(230) + 20;
            TechnoBot.getInstance().getEconomy().addMoney(event.getAuthor(), amount, EconManager.Activity.WORK);
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());
            embed.setDescription("You work for the day and receive " + EconManager.SYMBOL + amount);
            embed.setColor(EconManager.SUCCESS_COLOR);
        } else {
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
            embed.setDescription(":stopwatch: You cannot work for " + TechnoBot.getInstance().getEconomy().getCooldown(timestamp, cooldown) + ".");
            embed.setColor(EMBED_COLOR);
        }
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
