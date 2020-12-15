package com.technovision.technobot.commands.staff;

import com.google.common.collect.Sets;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.data.Configuration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Set;

public class CommandInfractions extends Command {
    public static final Configuration infractionConfig = new Configuration("data/", "infractions.json");

    public CommandInfractions() {
        super("infractions", "Get the infractions of the specified user", "infractions [user]", Category.STAFF);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        Member executor = event.getMember();
        Member target = null;
        try {
            target = event.getMessage().getMentionedMembers().get(0);
        } catch (Exception e) {
            // there was no mentioned user, using second check
        }

        if (!executor.hasPermission(Permission.MESSAGE_MANAGE)) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(ERROR_EMBED_COLOR);
            embed.setDescription(":x: You do not have permission to do that!");
            event.getChannel().sendMessage(embed.build()).queue();
            return true;
        }


        if (target == null) {
            try {
                target = event.getGuild().getMemberById(args[0]);
            } catch (Exception ignored) {
            }
        }
        if (target == null && args.length > 0) {
            event.getGuild().retrieveMemberById(args[0]).queue(member -> {
                complete(event, member);
            });
            return true;
        }
        if (target == null) {
            target = executor;
        }

        complete(event, target);
        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("warnings", "warns");
    }

    private void complete(MessageReceivedEvent event, Member target) {
        if (!infractionConfig.getJson().has(target.getId())) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor(target.getUser().getAsTag() + " has no infractions", null, target.getUser().getEffectiveAvatarUrl());
            event.getChannel().sendMessage(embed.build()).queue();
            return;
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(target.getUser().getAsTag() + "'s Infractions", null, target.getUser().getEffectiveAvatarUrl())
                .setDescription(infractionConfig.getJson().getJSONArray(target.getId()).length() + " total infractions on account.");

        for (Object o : infractionConfig.getJson().getJSONArray(target.getId())) {
            JSONObject infraction = (JSONObject) o;
            Member m = event.getGuild().getMemberById(infraction.getLong("issuer"));
            if (m != null)
                builder.addField(infraction.getString("type") + " At " + infraction.getString("date"), "Issued By: " + m.getUser().getAsTag() + "\nReason: " + infraction.getString("reason"), false);
            else event.getGuild().retrieveMemberById(infraction.getLong("issuer")).queue(mem -> {
                builder.addField(infraction.getString("type") + " At " + infraction.getString("date"), "Issued By: " + mem + "\nReason: " + infraction.getString("reason"), false);
            });
        }

        event.getChannel().sendMessage(builder.build()).queue();
    }
}
