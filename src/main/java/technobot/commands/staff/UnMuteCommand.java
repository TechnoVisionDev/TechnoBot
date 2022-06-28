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

import static technobot.util.Localization.get;

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
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.unmute.unmuteForeign)
            )).setEphemeral(true).queue();
            return;
        } else if (target.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.unmute.unmuteBot)
            )).setEphemeral(true).queue();
            return;
        }

        // Check target role position
        ModerationHandler moderationHandler = GuildData.get(event.getGuild()).moderationHandler;
        if (!moderationHandler.canTargetMember(target)) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.unmute.tooHighRole)
            )).setEphemeral(true).queue();
            return;
        }

        // Check that muted role is valid and user has it
        Role muteRole = GuildData.get(event.getGuild()).moderationHandler.getMuteRole();
        if (muteRole == null) {
            String text = get(s -> s.staff.unmute.noRole);
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }
        if (!target.getRoles().contains(muteRole)) {
            String text = get(s -> s.staff.unmute.notMuted);
            event.replyEmbeds(EmbedUtils.createError(text)).setEphemeral(true).queue();
            return;
        }
        int botPos = event.getGuild().getBotRole().getPosition();
        if (muteRole.getPosition() >= botPos) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.unmute.tooHighRole)
            )).setEphemeral(true).queue();
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
                .setAuthor(get(s -> s.staff.unmute.message, user.getAsTag()), null, user.getEffectiveAvatarUrl())
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
