package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
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
 * Command that kicks a user from the guild.
 *
 * @author TechnoVision
 */
public class KickCommand extends Command {

    public KickCommand(TechnoBot bot) {
        super(bot);
        this.name = "kick";
        this.description = "Kicks a user from your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to kick", true));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the kick"));
        this.permission = Permission.KICK_MEMBERS;
        this.botPermission = Permission.KICK_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.kick.kickForeign)
            )).setEphemeral(true).queue();
            return;
        } else if (target.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.kick.kickBot)
            )).setEphemeral(true).queue();
            return;
        }

        // Check target role position
        ModerationHandler moderationHandler = GuildData.get(event.getGuild()).moderationHandler;
        if (!moderationHandler.canTargetMember(target)) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.kick.tooHighRole)
            )).setEphemeral(true).queue();
            return;
        }

        // Kick user from guild
        String reason = event.getOption(
                "reason",
                get(s -> s.staff.reasonUnspecified) + "",
                OptionMapping::getAsString
        );
        user.openPrivateChannel().queue(privateChannel -> {
            // Private message user with reason for kick
            MessageEmbed msg = moderationHandler.createCaseMessage(event.getUser().getIdLong(), get(s -> s.staff.cases.actions.kick), reason, EmbedColor.WARNING.color);
            privateChannel.sendMessageEmbeds(msg).queue(
                    message -> target.kick(reason).queue(),
                    failure -> target.kick(reason).queue()
            );
        }, fail -> target.kick(reason).queue());

        // Send confirmation message
        event.replyEmbeds(new EmbedBuilder()
                .setAuthor(get(s -> s.staff.kick.message, user.getAsTag()), null, user.getEffectiveAvatarUrl())
                .setDescription(get(s -> s.staff.cases.reason, reason))
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
