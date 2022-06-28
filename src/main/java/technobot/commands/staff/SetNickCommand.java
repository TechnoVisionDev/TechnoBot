package technobot.commands.staff;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedUtils;

import static technobot.util.Localization.get;

/**
 * Command that changes or resets a user's nickname.
 *
 * @author TechnoVision
 */
public class SetNickCommand extends Command {

    public SetNickCommand(TechnoBot bot) {
        super(bot);
        this.name = "setnick";
        this.description = "Change or reset a user's nickname.";
        this.category = Category.STAFF;
        this.args.add(new OptionData(OptionType.USER, "user", "The user to set nick for", true));
        this.args.add(new OptionData(OptionType.STRING, "nickname", "The new nickname"));
        this.permission = Permission.NICKNAME_MANAGE;
        this.botPermission = Permission.NICKNAME_MANAGE;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member target = event.getOption("user").getAsMember();
        if (target == null) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.setNick.nickForeign)
            )).setEphemeral(true).queue();
            return;
        }

        try {
            String content = "";
            OptionMapping nickOption = event.getOption("nickname");
            if (nickOption != null) {
                String originalName = target.getUser().getName();
                String name = nickOption.getAsString();
                target.modifyNickname(name).queue();
                content = get(s -> s.staff.setNick.set, originalName, name);
            } else {
                String name = target.getUser().getName();
                target.modifyNickname(name).queue();
                content = get(s -> s.staff.setNick.reset, name);
            }
            event.replyEmbeds(EmbedUtils.createDefault(content)).queue();
        } catch (HierarchyException e) {
            event.replyEmbeds(EmbedUtils.createError(
                    get(s -> s.staff.setNick.tooHighRole)
            )).setEphemeral(true).queue();
        }
    }
}
