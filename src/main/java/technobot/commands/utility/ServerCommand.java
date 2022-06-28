package technobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import technobot.TechnoBot;
import technobot.commands.Category;
import technobot.commands.Command;
import technobot.util.embeds.EmbedColor;

import static technobot.util.Localization.get;

/**
 * Command that displays relevant server information.
 *
 * @author TechnoVision
 */
public class ServerCommand extends Command {

    public ServerCommand(TechnoBot bot) {
        super(bot);
        this.name = "server";
        this.description = "Display information about this discord server.";
        this.category = Category.UTILITY;
    }

    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        String guildTime = TimeFormat.RELATIVE.format(guild.getTimeCreated().toInstant().toEpochMilli());

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(EmbedColor.DEFAULT.color)
                .setTitle(guild.getName())
                .addField(get(s -> s.utility.server.id), guild.getId(), true)
                .addField(get(s -> s.utility.server.createdOn), guildTime, true)
                .addField(get(s -> s.utility.server.owner), "<@" + guild.getOwnerId() + ">", true);

        embed.addField(
                get(s -> s.utility.server.members, guild.getMemberCount()),
                get(s -> s.utility.server.boosts, guild.getBoostCount()),
                true
        );

        int textChannels = guild.getTextChannels().size();
        int voiceChannels = guild.getVoiceChannels().size();
        embed.addField(
                get(s -> s.utility.server.channels, textChannels + voiceChannels),
                get(s -> s.utility.server.channelTypes, textChannels, voiceChannels),
                true
        );

        embed.addField(
                get(s -> s.utility.server.other),
                get(s -> s.utility.server.verification, guild.getVerificationLevel().getKey()),
                true
        );

        embed.addField(
                get(s -> s.utility.server.roles, guild.getRoles().size()),
                get(s -> s.utility.server.rolesList),
                true
        );

        event.replyEmbeds(embed.build()).queue();
    }
}
