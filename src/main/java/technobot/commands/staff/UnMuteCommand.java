package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.handlers.ModerationHandler;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that removes a muted role from a user in the guild.
 *
 * @author TechnoVision
 */
public class UnMuteCommand extends Command {

    public UnMuteCommand(TechnoBot bot) {
        super(bot);
        this.name = "unmute";
        this.description = "Unmutes a user in your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to unmute", true));
        this.permission = Permission.MODERATE_MEMBERS;
        this.botPermission = Permission.MANAGE_ROLES;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.replyEmbeds(EmbedUtils.createError("That user is not in this server!")).setEphemeral(true).queue();
            return;
        } else if (target.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.replyEmbeds(EmbedUtils.createError("Do you seriously expect me to unmute myself?")).setEphemeral(true).queue();
            return;
        }

        // Check target role position
        ModerationHandler moderationHandler = GuildData.get(event.getGuild()).moderationHandler;
        if (!moderationHandler.canTargetMember(target)) {
            event.replyEmbeds(EmbedUtils.createError("This member cannot be unmuted. I need my role moved higher than theirs.")).setEphemeral(true).queue();
            return;
        }

        // Check that muted role is valid and user has it
        Role muteRole = GuildData.get(event.getGuild()).moderationHandler.getMuteRole();
        if (muteRole == null) {
            String text = "This server does not have a mute role, use `/mute-role <role>` to set one or `/mute-role create [name]` to create one.";
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }
        if (!target.getRoles().contains(muteRole)) {
            String text = "That user is not muted!";
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }
        int botPos = event.getGuild().getBotRole().getPosition();
        if (muteRole.getPosition() >= botPos) {
            event.replyEmbeds(EmbedUtils.createError("This member cannot be unmuted. I need my role moved higher than the mute role.")).setEphemeral(true).queue();
            return;
        }

        // Remove muted role to user
        event.getGuild().removeRoleFromMember(target, muteRole).queue();
        moderationHandler.unMuteUser(target.getIdLong());
        user.openPrivateChannel().queue(privateChannel -> {
            // Private message user with reason for kick
            MessageEmbed msg = moderationHandler.createCaseMessage(event.getUser().getIdLong(),"Un-Mute", EmbedColor.SUCCESS.color);
            privateChannel.sendMessageEmbeds(msg).queue();
        }, fail -> {});

        // Send confirmation message
        event.replyEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " has been unmuted", null, user.getEffectiveAvatarUrl())
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
