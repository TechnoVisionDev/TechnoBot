package com.technovision.technobot.commands.economy;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.listeners.managers.EconManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class CommandBalance extends Command {

    public CommandBalance(final TechnoBot bot) {
        super(bot, "balance", "View your account balance", "{prefix}balance", Command.Category.ECONOMY);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {

        User user = event.getAuthor();
        List<Member> mentions = event.getMessage().getMentionedMembers();

        if (args.length > 1) {
            EmbedBuilder errorEmbed = new EmbedBuilder();
            errorEmbed.setColor(ERROR_EMBED_COLOR);
            errorEmbed.setDescription(":x: Too many arguments given.\n\nUsage:\n`bal [user]`");

            event.getChannel().sendMessage(errorEmbed.build()).queue();

            return true;
        }

        if (mentions.size() > 0) {
            user = mentions.get(0).getUser();
        }

        if (user == event.getAuthor()) {
            try {
                user = event.getGuild().getMemberById(args[0]).getUser();
            } catch (Exception ignored) {
                EmbedBuilder errorEmbed = new EmbedBuilder();
                errorEmbed.setColor(ERROR_EMBED_COLOR);
                errorEmbed.setDescription(":x: Invalid `[user]` argument given.\n\nUsage:\n`bal [user]`");

                event.getChannel().sendMessage(errorEmbed.build()).queue();

                return true;
            }
        }

        Pair<Long, Long> profile = bot.getEconomy().getBalance(user);

        EmbedBuilder embed = new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
                .addField("Cash:", EconManager.SYMBOL + EconManager.FORMATTER.format(profile.getLeft()), true)
                .addField("Bank:", EconManager.SYMBOL + EconManager.FORMATTER.format(profile.getRight()), true)
                .addField("Net Worth:", EconManager.SYMBOL + EconManager.FORMATTER.format((profile.getLeft() + profile.getRight())), true)
                .setTimestamp(new Date().toInstant())
                .setColor(EMBED_COLOR);
        event.getChannel().sendMessage(embed.build()).queue();
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("bal", "money");
    }
}
