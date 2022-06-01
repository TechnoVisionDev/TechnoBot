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
import technobot.util.EmbedColor;
import technobot.util.EmbedUtils;

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
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String input = event.getOption("user_id").getAsString();
        if (input.equals(event.getJDA().getSelfUser().getId())) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("Ah yes let me just unban myself...")).queue();
            return;
        }
        try {
            event.getJDA().retrieveUserById(input).queue(user -> {
                // Remove banned user from database and any timer task
                GuildData.get(event.getGuild()).moderationHandler.removeBan(user);

                // Send confirmation message
                event.getHook().sendMessageEmbeds(new EmbedBuilder()
                        .setAuthor(user.getAsTag() + " has been unbanned", null, user.getEffectiveAvatarUrl())
                        .setColor(EmbedColor.DEFAULT.color)
                        .build()
                ).queue();
            }, fail -> {
                event.getHook().sendMessageEmbeds(EmbedUtils.createError("That user does not exist!")).queue();
            });
        } catch (NumberFormatException e) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("That is not a valid user ID!")).queue();
        }
    }
}
