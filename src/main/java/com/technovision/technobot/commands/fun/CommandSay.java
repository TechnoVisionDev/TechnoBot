package com.technovision.technobot.commands.fun;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandSay extends Command {
    public CommandSay(final TechnoBot bot) {
        super(bot, "say", "Send a message using the bot", "{prefix}say [channel] <message>", Category.FUN);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        if (event.getMessage().mentionsEveryone() || (event.getMessage().getMentions(Message.MentionType.HERE).size() >= 0)) {
            event.getChannel().sendMessage("You cannot mention everyone or here!");

            return true;
        }

        if (args.length > 0) {
            // Check for mentioned channels
            if (!event.getMessage().getMentionedChannels().isEmpty()) {
                // Make sure channel is the first argument
                if (args[0].startsWith("<#") && args[0].endsWith(">")) {

                    // Message has channel but no message
                    if (args.length < 2) {
                        // Argument Error
                        event.getChannel().sendMessage(getArgumentError()).queue();
                        return true;
                    }

                    // Send message to specified channel
                    long channelID = Long.parseLong(args[0].substring(2, args[0].length()-1));
                    TextChannel channel = event.getMessage().getMentionedChannels().get(0);
                    if (channel.getIdLong() == channelID) {
                        String message = buildMessage(1, event.getAuthor(), args);
                        if (message == null) { return true; }
                        if (channel.canTalk(event.getMember())) {
                            channel.sendMessage(message).queue();
                        }
                        return true;
                    }
                }
            }

            // Send in current channel
            String message = buildMessage(0, event.getAuthor(), args);
            if (message == null) { return true; }
            event.getChannel().sendMessage(message).queue();
            return true;
        }

        // Argument Error
        event.getChannel().sendMessage(getArgumentError()).queue();
        return true;
    }

    private boolean isSafe(String msg) {
        msg = msg.toLowerCase();
        if (msg.contains("discord.gg/")) { return false; }
        return !msg.contains("<@" + 595024631438508070L + ">");
    }

    private MessageEmbed getArgumentError() {
        return new EmbedBuilder()
                .setColor(ERROR_EMBED_COLOR)
                .setDescription(":x: Too few arguments given.\n\nUsage:\n`say [channel] <message>`")
                .build();
    }

    private String buildMessage(int startIndex, User author, String[] args) {
        StringBuilder msg = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            msg.append(args[i]).append(" ");
        }

        if (!isSafe(msg.toString())) { return null; }

        msg.append("\n\n- ").append(author.getName());

        return msg.toString();
    }
}
