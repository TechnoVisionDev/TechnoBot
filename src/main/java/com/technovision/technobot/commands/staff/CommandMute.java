package com.technovision.technobot.commands.staff;

import com.google.common.collect.Sets;
import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.data.Configuration;
import com.technovision.technobot.logging.AutoModLogger;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class CommandMute extends Command {
    private final Configuration muteTracker = new Configuration("data/", "muteTracker.json") {
        @Override
        public void load() {
            super.load();

            if (!getJson().has("users")) getJson().put("users", new JSONArray());
        }
    };

    private final String MUTE_ROLE_NAME = "Muted";
    private Role mute_role;

    public CommandMute() {
        super("mute", "Mutes the specified user", "mute <user>", Command.Category.STAFF);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                List<Integer> toRemove = new ArrayList<Integer>();

                int i = 0;

                for (Object o : muteTracker.getJson().getJSONArray("users")) {
                    JSONObject userJSON = (JSONObject) o;

                    if (System.currentTimeMillis() >= userJSON.getLong("timeEnded")) {
                        toRemove.add(i);
                        mute_role = TechnoBot.getInstance().getJDA().getGuildById(userJSON.getLong("guild")).getRolesByName(MUTE_ROLE_NAME, true).get(0);
                        if (mute_role == null) {
                            TechnoBot.getInstance().getLogger().log(Logger.LogLevel.WARNING, "Mute role does not exist!");
                            return;
                        }

                        Guild guild = TechnoBot.getInstance().getJDA().getGuildById(userJSON.getLong("guild"));
                        if (guild == null) {
                            throw new RuntimeException("Could not find guild by ID " + userJSON.getLong("guild"), new NullPointerException("Local field 'guild' is null!"));
                        }
                        guild.removeRoleFromMember(userJSON.getLong("userId"), mute_role).queue();
                    }

                    i++;
                }
                for (int remove : toRemove) {
                    muteTracker.getJson().getJSONArray("users").remove(remove);
                }
                muteTracker.save();
            }
        }, 60000L, 60000L); // 1 Minute Timer
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

        if (!executor.hasPermission(Permission.KICK_MEMBERS)) {
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
            event.getChannel().sendMessage("You can't mute yourself \uD83E\uDD26\u200D").queue();
            return true;
        }
        if (!executor.canInteract(target)) {
            event.getChannel().sendMessage("You can't mute that user!").queue();
            return true;
        }

        if (args.length == 0) {
            event.getChannel().sendMessage("Please specify a user, time and reason!").queue();
            return true;
        }

        String reason = "Unspecified";
        long timeMs = System.currentTimeMillis();
        String toParse = "";
        try {
            toParse = args[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            event.getChannel().sendMessage("Please enter a valid date! Examples (each is equivalent to 1 day):\n" +
                                           "`1440m`\n`24h`\n`1d`").queue();
            return true;
        }

        if (toParse.endsWith("m")) {
            timeMs += Long.parseLong(toParse.substring(0, toParse.length() - 1)) * 60 * 1000;
        } else if (toParse.endsWith("h")) {
            timeMs += Long.parseLong(toParse.substring(0, toParse.length() - 1)) * 60 * 60 * 1000;
        } else if (toParse.endsWith("d")) {
            timeMs += Long.parseLong(toParse.substring(0, toParse.length() - 1)) * 24 * 60 * 60 * 1000;
        } else {
            event.getChannel().sendMessage("Please enter a valid date! Examples (each is equivalent to 1 day):\n" +
                                           "`1440m`\n`24h`\n`1d`").queue();
            return true;
        }

        if (args.length > 2) {
            reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length, String[].class));
        }

        try {
            mute_role = event.getGuild().getRolesByName(MUTE_ROLE_NAME, true).get(0);
        } catch (IndexOutOfBoundsException ignored) {
        }
        if (mute_role == null) {
            final Member exec = executor;
            final Member t = target;
            final String re = reason;
            final long ms = timeMs;
            event.getGuild().createRole()
                    .setName(MUTE_ROLE_NAME)
                    .setColor(Color.DARK_GRAY)
                    .setMentionable(false)
                    .setPermissions(Permission.EMPTY_PERMISSIONS)
                    .setPermissions(Permission.MESSAGE_HISTORY)
                    .queue(r -> {
                        mute_role = r;
                        complete(event, exec, t, re, ms);
                    });
            event.getGuild().getTextChannels().forEach(tc -> tc.createPermissionOverride(mute_role).deny(Permission.MESSAGE_WRITE).queue());
        } else complete(event, executor, target, reason, timeMs);

        return true;
    }

    private void complete(MessageReceivedEvent event, Member executor, Member target, String reason, final long timeMs) {
        event.getGuild().addRoleToMember(target, mute_role).queue();

        muteTracker.getJson().getJSONArray("users").put(new JSONObject() {{
            put("timeEnded", timeMs);
            put("guild", event.getGuild().getIdLong());
            put("userId", target.getIdLong());
        }});

        muteTracker.save();

        final String r = reason;
        if (!CommandInfractions.infractionConfig.getJson().has(target.getId()))
            CommandInfractions.infractionConfig.getJson().put(target.getId(), new JSONArray());
        CommandInfractions.infractionConfig.getJson().getJSONArray(target.getId()).put(new JSONObject() {{
            put("type", "Mute");
            put("date", new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime()));
            put("reason", r);
            put("issuer", executor.getIdLong());
        }});

        CommandInfractions.infractionConfig.save();

        event.getChannel().sendMessage(new EmbedBuilder()
                .setAuthor(target.getUser().getAsTag() + " has been muted", null, target.getUser().getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason.replaceAll("`", "")).build()).queue();

        TechnoBot.getInstance().getAutoModLogger().log(event.getGuild(), event.getTextChannel(), target.getUser(), event.getAuthor(), AutoModLogger.Infraction.MUTE, reason);
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("tempmute");
    }
}
