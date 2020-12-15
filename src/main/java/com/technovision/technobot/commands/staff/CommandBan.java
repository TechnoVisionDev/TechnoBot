package com.technovision.technobot.commands.staff;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.logging.AutoModLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CommandBan extends Command {


    public CommandBan() {
        super("ban", "Bans the specified user for specified reason", "{prefix}ban <user> [reason]", Command.Category.STAFF);
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

        if (!executor.hasPermission(Permission.BAN_MEMBERS)) {
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
        if (target == null) {
            event.getChannel().sendMessage("Could not find user!").queue();
            return true;
        }
        if (executor.getUser().getId().equalsIgnoreCase(target.getUser().getId())) {
            event.getChannel().sendMessage("You can't ban yourself \uD83E\uDD26\u200D").queue();
            return true;
        }
        if (!executor.canInteract(target)) {
            event.getChannel().sendMessage("You can't ban that user!").queue();
            return true;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage("Please specify a user and reason!").queue();
            return true;
        }

        String reason = "Unspecified";

        if (args.length > 1) {
            reason = String.join(" ", args);
            reason = reason.substring(reason.indexOf(" "));
        }

        target.ban(0, reason).queue();

        final String r = reason;
        if (!CommandInfractions.infractionConfig.getJson().has(target.getId()))
            CommandInfractions.infractionConfig.getJson().put(target.getId(), new JSONArray());
        CommandInfractions.infractionConfig.getJson().getJSONArray(target.getId()).put(new JSONObject() {{
            put("type", "Ban");
            put("date", new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime()));
            put("reason", r);
            put("issuer", executor.getIdLong());
        }});
        CommandInfractions.infractionConfig.save();

        event.getChannel().sendMessage(new EmbedBuilder()
                .setAuthor(target.getUser().getAsTag() + " has been banned", null, target.getUser().getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason.replaceAll("`", "")).build()).queue();

        TechnoBot.getInstance().getAutoModLogger().log(event.getGuild(), event.getTextChannel(), target.getUser(), event.getAuthor(), AutoModLogger.Infraction.BAN, reason);

        return true;
    }
}
