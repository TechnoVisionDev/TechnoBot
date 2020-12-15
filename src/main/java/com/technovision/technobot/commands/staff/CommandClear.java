package com.technovision.technobot.commands.staff;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.logging.AutoModLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CommandClear extends Command {

    public CommandClear() {
        super("clear", "purges a channel of messages", "{prefix}clear <amount>", Category.STAFF);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        TextChannel channel = event.getTextChannel();

        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(ERROR_EMBED_COLOR);
            embed.setDescription(":x: You do not have permission to do that!");
            channel.sendMessage(embed.build()).queue();
            return true;
        }

        if (args.length < 1) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(ERROR_EMBED_COLOR);
            embed.setDescription(":x: Please specify how many messages to clear!");
            channel.sendMessage(embed.build()).queue();
            return true;
        }
        try {
            int amt = Integer.parseInt(args[0]);
            if (amt < 2 || amt > 100) {
                throw new NumberFormatException();
            }
            OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);
            new Thread(() -> {
                event.getMessage().delete().queue();
                List<Message> messages = channel.getHistory().retrievePast(amt).complete();
                messages.removeIf(m -> m.getTimeCreated().isBefore(twoWeeksAgo));
                channel.deleteMessages(messages).complete();
                try {
                    Message m = channel.sendMessage(String.format(":ballot_box_with_check: I have deleted `%d messages`!", amt)).complete();
                    Thread.sleep(3000);
                    m.delete().queue();
                    TechnoBot.getInstance().getAutoModLogger().log(event.getGuild(), event.getTextChannel(), event.getAuthor(), event.getAuthor(), AutoModLogger.Infraction.CLEAR);
                } catch (InterruptedException ignored) {
                }
            }).start();
        } catch (NumberFormatException e) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(ERROR_EMBED_COLOR);
            embed.setDescription(":x: Please use a number between 2-100");
            channel.sendMessage(embed.build()).queue();
        }
        return true;
    }
}
