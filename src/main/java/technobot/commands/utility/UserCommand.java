package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;

import static technobot.util.Localization.get;

/**
 * Command that displays information about a user.
 *
 * @author TechnoVision
 */
public class UserCommand extends Command {

    public UserCommand(TechnoBot bot) {
        super(bot);
        this.name = "user";
        this.description = "Shows info about yourself or another user.";
        this.category = Category.UTILITY;
        this.args.add(new OptionData(OptionType.USER, "user", "User to get info about"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get user
        User user = event.getOption("user", event.getUser(), OptionMapping::getAsUser);

        event.getGuild().retrieveMember(user).queue(member -> {
            // Create and send embed
            String joinedDiscord = TimeFormat.RELATIVE.format(member.getTimeCreated().toInstant().toEpochMilli());
            String joinedServer = TimeFormat.RELATIVE.format(member.getTimeJoined().toInstant().toEpochMilli());
            EmbedBuilder embed = new EmbedBuilder()
                    .setColor(EmbedColor.DEFAULT.color)
                    .addField(get(s -> s.utility.user.joinedDiscord), joinedDiscord, true)
                    .addField(get(s -> s.utility.user.joinedServer), joinedServer, true)
                    .addField(get(s -> s.utility.user.discordId), user.getId(), false)
                    .setThumbnail(user.getEffectiveAvatarUrl())
                    .setFooter(user.getAsTag(), user.getEffectiveAvatarUrl());
            event.replyEmbeds(embed.build()).queue();
        });
    }
}
