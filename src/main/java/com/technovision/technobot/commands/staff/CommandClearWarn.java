package com.technovision.technobot.commands.staff;

import com.google.common.collect.Sets;
import com.technovision.technobot.commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CommandClearWarn extends Command {
    public CommandClearWarn() {
        super("clearinfractions", "Clear infractions of specified user", "clearinfractions <user>", Category.STAFF);
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

        if (args.length == 0) {
            event.getChannel().sendMessage("Please specify a user!").queue();
            return true;
        }

        if (CommandInfractions.infractionConfig.getJson().has(target.getId()))
            CommandInfractions.infractionConfig.getJson().remove(target.getId());

        CommandInfractions.infractionConfig.save();

        event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("Success")
                .setDescription("Successfully cleared warnings of <@!" + target.getUser().getId() + ">!").build()).queue();

        return true;
    }

    @Override
    public @NotNull Set<String> getAliases() {
        return Sets.newHashSet("clearwarn", "clearwarns", "clearwarnings");
    }
}
