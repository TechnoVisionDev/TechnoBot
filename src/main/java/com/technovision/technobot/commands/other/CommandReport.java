package com.technovision.technobot.commands.other;

import com.google.api.client.util.ArrayMap;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommandReport extends Command {

    private static final Map<Member, Long> coolDown = new ArrayMap<>();

    public CommandReport() {
        super("report", "Report a user to a staff", "{prefix}report", Command.Category.OTHER);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Report");
        embed.setColor(ERROR_EMBED_COLOR);
        if (canReport(event.getMember(), embed) && hasMentionMember(event.getMessage(), embed) && args.length > 0) {
            coolDown.put(event.getMember(), new Date().getTime());
            Guild guild = event.getGuild();
            List<Member> staffList = guild.getMembersWithRoles(guild.getRoleById("599344898856189984"));
            Member staffMember = getOnlineStaff(staffList);
            String message = String.join(" ", args);
            embed.setColor(EMBED_COLOR);
            embed.setFooter("report by " + event.getAuthor().getAsTag());
            embed.setDescription(message);
            staffMember.getUser().openPrivateChannel().complete().sendMessage(embed.build()).queue();
            embed.setDescription("Successfully sent report to " + staffMember.getUser().getAsTag());
        } else if (args.length == 0) {
            embed.setDescription("Not enough arguments!");
        }
        event.getTextChannel().sendMessage(embed.build()).queue();
        return true;
    }

    //Check for the nearest online staff member
    private Member getOnlineStaff(List<Member> staffs) {
        for (Member staff : staffs) {
            if (staff.getOnlineStatus() != OnlineStatus.OFFLINE) return staff;
        }
        //If no staff online, send report to Techno.
        return staffs.get(0).getGuild().getMemberById("595024631438508070");
    }

    //Check if user can make another report after cooldown
    private boolean canReport(Member reporter, EmbedBuilder embed) {
        if (!coolDown.containsKey(reporter)) return true;
        Date date = new Date();
        long newTime = date.getTime();
        long oldTime = coolDown.get(reporter);
        long time = newTime - oldTime;
        if (time >= (long) 10800000) {
            return true;
        } else {
            embed.setDescription(String.format("Please wait until %s", new Timestamp(oldTime + 10800000)));
            return false;
        }
    }

    //Check if user has mentioned a member
    private boolean hasMentionMember(Message message, EmbedBuilder embed) {
        if (message.getMentionedMembers().isEmpty()) {
            embed.setDescription("Please mention a member");
            return false;
        }
        return true;
    }
}
