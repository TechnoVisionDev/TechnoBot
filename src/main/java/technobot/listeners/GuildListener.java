package technobot.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.discordbots.api.client.DiscordBotListAPI;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.CommandRegistry;
import technobot.commands.automation.AutoRoleCommand;
import technobot.data.GuildData;

/**
 * Listens for guild event
 * Only really used for guild command registration at the moment.
 *
 * @author TechnoVision
 */
public class GuildListener extends ListenerAdapter {

    private final TechnoBot bot;

    public GuildListener(TechnoBot bot) {
        this.bot = bot;
    }

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
     * Updates <a href="https://top.gg">...</a> with the bot server count after startup.
     *
     * @param event executes after all guilds are loaded.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        String TOPGG_TOKEN = bot.config.get("TOPGG_TOKEN");
        if (TOPGG_TOKEN != null) {
            DiscordBotListAPI api = new DiscordBotListAPI.Builder()
                    .token(TOPGG_TOKEN)
                    .botId(event.getJDA().getSelfUser().getId())
                    .build();
            api.setStats(event.getGuildTotalCount());
        }
    }

    /**
     * Add roles on guild member join. Used for role persists and auto-roles.
     */
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Guild guild = event.getGuild();
        Member member = event.getMember();
        GuildData data = GuildData.get(guild);

        // Persist mute role
        data.moderationHandler.persistMuteRole(member);

        // Give auto roles
        int count = 0;
        int max = data.configHandler.isPremium() ? AutoRoleCommand.MAX_AUTO_ROLES : 1;
        for (long roleID : data.configHandler.getConfig().getAutoRoles()) {
            Role role = guild.getRoleById(roleID);
            if (role != null) {
                guild.addRoleToMember(member, role).queue();
                count++;
                if (count == max) break;
            }
        }
    }
}
