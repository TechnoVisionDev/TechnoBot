package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

/**
 * Command that prevents users from sending messages in a channel.
 *
 * @author TechnoVision
 */
public class LockCommand extends Command {

    public LockCommand(TechnoBot bot) {
        super(bot);
        this.name = "lock";
        this.description = "Disables @everyone from sending messages in a channel.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.CHANNEL, "channel", "The channel to lock"));
        this.permission = Permission.MANAGE_CHANNEL;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Check that bot has necessary permissions
        Role botRole = event.getGuild().getBotRole();
        if (!botRole.hasPermission(this.permission)) {
            event.getHook().sendMessageEmbeds(EmbedUtils.createError("I couldn't lock that channel. Please check my role and channel permissions.")).queue();
            return;
        }

        OptionMapping channelOption = event.getOption("channel");
        TextChannel channel;

        if (channelOption != null) { channel = channelOption.getAsTextChannel(); }
        else { channel = event.getTextChannel(); }

        if (channel == null) {
            event.replyEmbeds(EmbedUtils.createError("That is not a valid channel!")).queue();
            return;
        }

        channel.upsertPermissionOverride(event.getGuild().getPublicRole()).deny(Permission.MESSAGE_SEND).queue();
        String channelString = "<#"+channel.getId()+">";
        event.replyEmbeds(EmbedUtils.createDefault(":lock: "+channelString+" has been locked.")).queue();
    }
}
