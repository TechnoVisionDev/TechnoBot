package com.technovision.technobot.commands.economy;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CommandCrime extends Command {

    public CommandCrime() {
        super("crime", "Risk it all for extra cash", "{prefix}crime", Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        JSONObject profile = TechnoBot.getInstance().getEconomy().getProfile(event.getAuthor());
        long timestamp = profile.getLong("crime-timestamp");
        int cooldown = 14400000;
        if (System.currentTimeMillis() >= timestamp + cooldown) {
            Random rand = ThreadLocalRandom.current();
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getEffectiveAvatarUrl());
            if (rand.nextInt(10) > 5) { //40% Success Rate
                int amount = rand.nextInt(450) + 250;
                TechnoBot.getInstance().getEconomy().addMoney(event.getAuthor(), amount, EconManager.Activity.CRIME);
                embed.setColor(EconManager.SUCCESS_COLOR);
                embed.setDescription("You rob the local bank and steal " + EconManager.SYMBOL + amount);
            } else {
                int amount = rand.nextInt(400) + 1;
                TechnoBot.getInstance().getEconomy().removeMoney(event.getAuthor(), amount, EconManager.Activity.CRIME);
                embed.setColor(ERROR_EMBED_COLOR);
                embed.setDescription("You were caught shoplifting. Pay a fine of " + EconManager.SYMBOL + amount);
            }
        } else {
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
            embed.setDescription(":stopwatch: You cannot commit a crime for " + TechnoBot.getInstance().getEconomy().getCooldown(timestamp, cooldown) + ".");
            embed.setColor(EMBED_COLOR);
        }
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }
}
