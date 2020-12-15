package com.technovision.technobot.commands.levels;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Set;

public class CommandLeaderboard extends Command {

    private final DecimalFormat formatter;

    public CommandLeaderboard() {
        super("leaderboard", "Shows the level Leaderboard", "{prefix}leaderboard <page>", Command.Category.LEVELS);
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        int usersPerPage = 20;
        int start = 0;
        LinkedList<Document> leaderboard = TechnoBot.getInstance().getLevelManager().getLeaderboard();

        if (args.length > 0) {
            try {
                int page = Integer.parseInt(args[0]);
                if (page > 1) {
                    int comparison = (leaderboard.size() / usersPerPage) + 1;
                    if (leaderboard.size() % usersPerPage != 0) {
                        comparison++;
                    }
                    if (page >= comparison) {
                        EmbedBuilder embed = new EmbedBuilder()
                                .setColor(ERROR_EMBED_COLOR)
                                .setDescription(":x: That page doesn't exist!");
                        event.getChannel().sendMessage(embed.build()).queue();
                        return true;
                    }
                    start = (usersPerPage * (page - 1)) - 1;
                }
            } catch (NumberFormatException e) {
                EmbedBuilder embed = new EmbedBuilder()
                        .setColor(ERROR_EMBED_COLOR)
                        .setDescription(":x: That is not a valid page number!");
                event.getChannel().sendMessage(embed.build()).queue();
                return true;
            }
        }

        String msg = "";
        int finish = start + usersPerPage;
        if (start != 0) {
            finish++;
        }
        if (start != 0) {
            start++;
        }
        for (int i = start; i < finish; i++) {
            try {
                Document doc = leaderboard.get(i);
                int totalXP = doc.getInteger("totalXP");
                int lvl = doc.getInteger("level");
                long id = doc.getLong("id");
                msg += (i + 1) + ". <@!" + id + "> " + formatter.format(totalXP) + "xp " + "lvl " + lvl + "\n";
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(":trophy: Rank Leaderboard");
        builder.setColor(EMBED_COLOR);
        builder.setDescription(msg);
        int maxPage = leaderboard.size() / usersPerPage;
        if (maxPage * usersPerPage != leaderboard.size()) {
            maxPage++;
        }
        if (maxPage == 0) {
            maxPage++;
        }
        builder.setFooter("Page " + (1 + (start / usersPerPage)) + "/" + maxPage);
        event.getChannel().sendMessage(builder.build()).queue();
        return true;
    }


    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("ranks", "lvls", "leaderboards", "lb");
    }
}
