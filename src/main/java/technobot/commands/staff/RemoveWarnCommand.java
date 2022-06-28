package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
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
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that removes a warning by user or id.
 *
 * @author TechnoVision
 */
public class RemoveWarnCommand extends Command {

    public RemoveWarnCommand(TechnoBot bot) {
        super(bot);
        this.name = "remove-warn";
        this.description = "Remove warnings by user or ID.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "User to clear ALL warnings for"));
        this.args.add(new OptionData(OptionType.INTEGER, "id", "ID number for the warning to remove").setMinValue(1));
        this.permission = Permission.MODERATE_MEMBERS;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        GuildData data = GuildData.get(event.getGuild());
        OptionMapping userOption = event.getOption("user");
        OptionMapping idOption = event.getOption("id");

        MessageEmbed embed;
        if (idOption != null) {
            // Remove warning with this ID
            int count = data.moderationHandler.removeWarning(idOption.getAsInt());
            if (count == 1) {
                embed = EmbedUtils.createDefault(get(
                        s -> s.staff.removeWarn.successId,
                        idOption.getAsInt()
                ));
            } else {
                embed = EmbedUtils.createError(
                        get(s -> s.staff.removeWarn.failureId)
                );
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(embed).queue();
        } else if (userOption != null) {
            // Remove all warnings from user
            User target = userOption.getAsUser();
            int count = data.moderationHandler.clearWarnings(target.getIdLong());
            if (count > 1) {
                embed = EmbedUtils.createDefault(get(
                        s -> s.staff.removeWarn.successUser,
                        target.getId()
                ));
            } else {
                embed = EmbedUtils.createError(get(s -> s.staff.removeWarn.failureUser));
                event.replyEmbeds(embed).setEphemeral(true).queue();
                return;
            }
            event.replyEmbeds(embed).queue();
        } else {
            // No user or ID specified
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.removeWarn.failure)
            )).setEphemeral(true).queue();
        }
    }
}
