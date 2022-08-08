package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;

import static technobot.util.Localization.get;

/**
 * Command that displays a user's avatar and link.
 *
 * @author TechnoVision
 */
public class AvatarCommand extends Command {

    public AvatarCommand(TechnoBot bot) {
        super(bot);
        this.name = "avatar";
        this.description = "Display your avatar or someone else's avatar.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.USER, "user", "See another user's avatar"));
    }

    public void execute(SlashCommandInteractionEvent event) {
        // Get user
        User user = event.getOption("user", event.getUser(), OptionMapping::getAsUser);

        // Create and send embed
        String avatarUrl = user.getEffectiveAvatarUrl() + "?size=1024";
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setAuthor(user.getAsTag(), null, avatarUrl)
                .setTitle(get(s -> s.utility.avatar), avatarUrl)
                .setImage(avatarUrl);
        event.replyEmbeds(embed.build()).queue();
    }
}
