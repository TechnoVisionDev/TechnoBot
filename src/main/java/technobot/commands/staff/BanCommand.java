package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that bans a user from the guild.
 *
 * @author TechnoVision
 */
public class BanCommand extends Command {

    public BanCommand(TechnoBot bot) {
        super(bot);
        this.name = "ban";
        this.description = "Bans a user from your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to ban", true));
        this.args.add(new OptionData(OptionType.INTEGER, "days", "Time duration for the ban in days").setMinValue(1).setMaxValue(1825));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the ban"));
        this.permission = Permission.BAN_MEMBERS;
        this.botPermission = Permission.BAN_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member member = event.getOption("user").getAsMember();
        if (user.getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            event.replyEmbeds(EmbedUtils.createError("Did you seriously expect me to ban myself?")).setEphemeral(true).queue();
            return;
        }

        // Check target role position
        Guild guild = event.getGuild();
        GuildData data = GuildData.get(guild);
        if (!data.moderationHandler.canTargetMember(member)) {
            event.replyEmbeds(EmbedUtils.createError("This member cannot be banned. I need my role moved higher than theirs.")).setEphemeral(true).queue();
            return;
        }

        // Get command line options
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Unspecified";
        OptionMapping daysOption = event.getOption("days");
        final boolean isTempBan;
        String duration = null;
        if (daysOption != null) {
            duration = daysOption.getAsInt()+" Days";
            isTempBan = true;
        } else {
            isTempBan = false;
        }

        // Start unban timer if temp ban specified
        String content = user.getAsTag() + " has been banned";
        if (daysOption != null) {
            int days = daysOption.getAsInt();
            content += " for " + days + " day";
            if (days > 1) content += "s";
            data.moderationHandler.scheduleUnban(guild, user,  days);
        } else if (data.moderationHandler.hasTimedBan(user.getId())) {
            // Remove timed ban in favor of permanent ban
            data.moderationHandler.removeBan(user);
        }

        // Ban user from guild
        String finalDuration = duration;
        user.openPrivateChannel().queue(privateChannel -> {
            // Private message user with reason for Ban
            MessageEmbed msg;
            if (isTempBan) {
                msg = data.moderationHandler.createCaseMessage(event.getUser().getIdLong(), "Ban", reason, finalDuration, EmbedColor.ERROR.color);
            } else {
                msg = data.moderationHandler.createCaseMessage(event.getUser().getIdLong(), "Ban", reason, EmbedColor.ERROR.color);
            }
            privateChannel.sendMessageEmbeds(msg).queue(
                    message -> guild.ban(user, 7, reason).queue(),
                    failure -> guild.ban(user, 7, reason).queue()
            );
        }, fail -> guild.ban(user, 7, reason).queue());

        // Send confirmation message
        event.replyEmbeds(new EmbedBuilder()
                .setAuthor(content, null, user.getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason)
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
