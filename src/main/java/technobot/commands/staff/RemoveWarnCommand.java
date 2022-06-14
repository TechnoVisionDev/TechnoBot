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

import static technobot.util.embeds.EmbedUtils.GREEN_TICK;

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
        this.permission = Permission.MANAGE_ROLES;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        GuildData data = GuildData.get(event.getGuild());
        OptionMapping userOption = event.getOption("user");
        OptionMapping idOption = event.getOption("id");

        MessageEmbed embed;
        if (idOption != null) {
            // Remove warning with this ID
            int count = data.moderationHandler.removeWarning(idOption.getAsInt());
            if (count == 1) {
                embed = EmbedUtils.createDefault(GREEN_TICK + " 1 warning has been removed.");
            } else {
                embed = EmbedUtils.createError("Unable to find a warning with that ID!");
            }
            event.getHook().sendMessageEmbeds(embed).queue();
        } else if (userOption != null) {
            // Remove all warnings from user
            User target = userOption.getAsUser();
            int count = data.moderationHandler.clearWarnings(target.getIdLong());
            if (count > 1) {
                embed = EmbedUtils.createDefault(GREEN_TICK+" "+count+" warnings have been removed.");
            } else if (count == 1) {
                embed = EmbedUtils.createDefault(GREEN_TICK+" 1 warning has been removed.");
            } else {
                embed = EmbedUtils.createError("That user does not have any warnings!");
            }
            event.getHook().sendMessageEmbeds(embed).queue();
        } else {
            // No user or ID specified
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("You must specify a user or a warning ID!")).queue();
        }
    }
}
