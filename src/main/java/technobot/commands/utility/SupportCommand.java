package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;
import technobot.util.localization.Support;

import static technobot.util.Localization.format;
import static technobot.util.Localization.get;

/**
 * Command that generates a link to the support server.
 *
 * @author TechnoVision
 */
public class SupportCommand extends Command {

    public SupportCommand(TechnoBot bot) {
        super(bot);
        this.name = "support";
        this.description = "Get an invite to the support server.";
        this.category = Category.UTILITY;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Support supportText = get(s -> s.utility.support);
        event.replyEmbeds(new EmbedBuilder()
                .setTitle(supportText.title)
                .setDescription(format(supportText.description, event.getUser().getName()))
                .addField(
                        supportText.serverTitle,
                        format(supportText.askAQuestion, "https://discord.com/invite/2TKJqfUQas"),
                        true
                )
                .addField(
                        supportText.bugReportTitle,
                        format(supportText.askAQuestion, "https://github.com/TechnoVisionDev/TechnoBot/issues"),
                        true
                )
                .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setColor(EmbedColor.DEFAULT.color)
                .build()
        ).queue();
    }
}
