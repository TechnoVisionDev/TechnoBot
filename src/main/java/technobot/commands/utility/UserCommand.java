package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.TimeFormat;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.EmbedColor;

import java.time.ZoneOffset;

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
        event.deferReply().queue();
        // Get user
        OptionMapping userOption = event.getOption("user");
        User user = userOption != null ? userOption.getAsUser() : event.getUser();
        Member member = event.getGuild().getMember(user);

        // Create and send embed
        String joinedDiscord = TimeFormat.RELATIVE.format(member.getTimeCreated().toInstant().toEpochMilli());
        String joinedServer = TimeFormat.RELATIVE.format(member.getTimeJoined().toInstant().toEpochMilli());
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .addField("Joined Discord", joinedDiscord, true)
                .addField("Joined Server", joinedServer, true)
                .addField("Discord ID", user.getId(), false)
                .setThumbnail(user.getEffectiveAvatarUrl())
                .setFooter(user.getAsTag(), user.getEffectiveAvatarUrl());
        event.getHook().sendMessageEmbeds(embed.build()).queue();
    }
}
