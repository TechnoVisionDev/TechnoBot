package technobot.listeners;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import technobot.commands.CommandRegistry;
import technobot.data.GuildData;

/**
 * Listens for guild event
 * Only really used for guild command registration at the moment.
 *
 * @author TechnoVision
 */
public class GuildListener extends ListenerAdapter {

    /**
     * Registers slash commands as guild commands to guilds that join after startup.
     * NOTE: May change to global commands on release.
     *
     * @param event executes when a guild is ready.
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        // Get GuildData from database
        GuildData.get(event.getGuild());
        // Register slash commands
        event.getGuild().updateCommands().addCommands(CommandRegistry.unpackCommandData()).queue();
    }

    /**
     * Persists mute roles on guild member join.
     * Used to prevent members from mute evading by kicking themselves.
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GuildData data = GuildData.get(event.getGuild());
        data.moderationHandler.persistMuteRole(event.getMember());
    }
}
