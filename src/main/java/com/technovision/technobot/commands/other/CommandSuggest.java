package com.technovision.technobot.commands.other;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSuggest extends Command {
    public CommandSuggest() {
        super("suggest", "Suggest a feature or idea related to the server", "{prefix}suggest [content]", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (args.length > 0) {
            EmbedBuilder embed = new EmbedBuilder();
            StringBuilder msg = new StringBuilder();
            for (String arg : args) {
                msg.append(arg).append(" ");
            }
            embed.setAuthor(event.getAuthor().getAsTag(), null, event.getAuthor().getAvatarUrl());
            embed.setTitle("Suggestion #" + (TechnoBot.getInstance().getSuggestionManager().getAmount() + 1));
            embed.setDescription(msg.toString());
            embed.setColor(EMBED_COLOR);
            TextChannel channel = event.getGuild().getTextChannelsByName("SUGGESTIONS", true).get(0);
            channel.sendMessage(embed.build()).queue(message -> {
                message.addReaction("\u2B06\uFE0F").queue();
                message.addReaction("\u2B07\uFE0F").queue();
                TechnoBot.getInstance().getSuggestionManager().addSuggestion(message.getId());
            });
            EmbedBuilder response = new EmbedBuilder()
                    .setColor(EMBED_COLOR)
                    .setDescription("Your suggestion has been added to <#" + channel.getId() + ">!");
            event.getChannel().sendMessage(response.build()).queue();
        } else {
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(ERROR_EMBED_COLOR)
                    .setDescription(":x: You must write out your suggestion!");
            event.getChannel().sendMessage(embed.build()).queue();
        }
        return true;
    }
}
