package com.technovision.technobot.commands.staff;

import com.technovision.technobot.TechnoBot;
import com.technovision.technobot.commands.Command;
import com.technovision.technobot.logging.AutoModLogger;
import com.technovision.technobot.logging.Logger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandUnmute extends Command {

    private final String MUTE_ROLE_NAME = "Muted";

    public CommandUnmute() {
        super("unmute", "Un-mutes the specified user", "unmute <user>", Command.Category.STAFF);
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
            event.getChannel().sendMessage("Please specify a user!").queue();
            return true;
        }

        try {
            Role mute_role = event.getGuild().getRolesByName(MUTE_ROLE_NAME, true).get(0);
            Member member = event.getMessage().getMentionedMembers().get(0);
            event.getGuild().removeRoleFromMember(member, mute_role).queue();
            event.getChannel().sendMessage(new EmbedBuilder()
                    .setAuthor(target.getUser().getAsTag() + " has been un-muted", null, target.getUser().getEffectiveAvatarUrl()).build()).queue();
            TechnoBot.getInstance().getAutoModLogger().log(event.getGuild(), event.getTextChannel(), target.getUser(), event.getAuthor(), AutoModLogger.Infraction.UNMUTE);
        } catch (IndexOutOfBoundsException e) {
            TechnoBot.getInstance().getLogger().log(Logger.LogLevel.WARNING, "Mute role does not exist!");
        }
        return true;
    }
}
