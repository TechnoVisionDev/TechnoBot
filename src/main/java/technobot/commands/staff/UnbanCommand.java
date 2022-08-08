package technobot.commands.staff;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.data.GuildData;
import technobot.util.embeds.EmbedColor;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that unbans a user from the guild.
 *
 * @author TechnoVision
 */
public class UnbanCommand extends Command {

    public UnbanCommand(TechnoBot bot) {
        super(bot);
        this.name = "unban";
        this.description = "Unbans a user from your server.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.STRING, "user_id", "The ID of the user to unban", true));
        this.permission = Permission.BAN_MEMBERS;
        this.botPermission = Permission.BAN_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String input = event.getOption("user_id").getAsString();
        if (input.equals(event.getJDA().getSelfUser().getId())) {
            event.replyEmbeds(EmbedUtils.createError(get(s -> s.staff.unban.unbanSelf)
            )).setEphemeral(true).queue();
            return;
        }
        try {
            event.getJDA().retrieveUserById(input).queue(user -> {
                // Remove banned user from database and any timer task
                GuildData.get(event.getGuild()).moderationHandler.removeBan(user);

                // Send confirmation message
                event.replyEmbeds(new EmbedBuilder()
                        .setAuthor(get(s -> s.staff.unban.success, user.getAsTag()), null, user.getEffectiveAvatarUrl())
                        .setColor(EmbedColor.DEFAULT.color)
                        .build()
                ).queue();
            }, fail -> event.replyEmbeds(EmbedUtils.createError(get(s -> s.staff.unban.noUser))).setEphemeral(true).queue());
        } catch (NumberFormatException e) {
            event.replyEmbeds(EmbedUtils.createError(get(s -> s.staff.unban.invalidUser))).setEphemeral(true).queue();
        }
    }
}
