package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.EmbedColor;

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
        this.args.add(new OptionData(OptionType.INTEGER, "days", "Time duration for the ban in days").setMinValue(1).setMaxValue(365));
        this.args.add(new OptionData(OptionType.STRING, "reason", "Reason for the ban"));
        this.permission = Permission.BAN_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        // Get command and member data
        User user = event.getOption("user").getAsUser();
        Member target = event.getGuild().getMemberById(user.getIdLong());
        OptionMapping reasonOption = event.getOption("reason");
        String reason = reasonOption != null ? reasonOption.getAsString() : "Unspecified";

        // Kick user from guild
        user.openPrivateChannel().queue(privateChannel -> {
            // Private message user with reason for kick
            String guild = event.getGuild().getName();
            String staff = event.getUser().getAsTag();
            String text = "You got banned from **%s** by **%s**\n\n `Reason: %s`".formatted(guild, staff, reason);
            privateChannel.sendMessage(text).queue(
                    message -> event.getGuild().ban(user, 7, reason).queue(),
                    failure -> event.getGuild().ban(user, 7, reason).queue());
            }
        );

        OptionMapping timeOption = event.getOption("time");
        if (timeOption != null) {
            // TODO: Add banned user to database and start scheduled executor service to unban
        }

        // Send confirmation message
        event.getHook().sendMessageEmbeds(new EmbedBuilder()
                .setAuthor(user.getAsTag() + " has been banned", null, user.getEffectiveAvatarUrl())
                .setDescription("**Reason:** " + reason)
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
