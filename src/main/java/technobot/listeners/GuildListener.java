package technobot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import technobot.TechnoBot;
import technobot.commands.CommandRegistry;
import technobot.commands.automation.AutoRoleCommand;
import technobot.data.GuildData;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Listens for guild events.
 * Handles command registry, auto roles, and top.gg updates.
 *
 * @author TechnoVision
 */
public class GuildListener extends ListenerAdapter {

    private static final long STATS_UPDATE_PERIOD = 3600000; //1 hour in millis

    private final TechnoBot bot;
    private final Timer timer;

    public GuildListener(TechnoBot bot) {
        this.bot = bot;
        this.timer = new Timer();
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
        // Get Top.GG bearer token from .env
        String TOPGG_TOKEN = bot.config.get("TOPGG_TOKEN");
        if (TOPGG_TOKEN != null) {
            // Update bot statistics at a fixed rate
            timer.scheduleAtFixedRate(new StatUpdateTask(event.getJDA(), TOPGG_TOKEN), 0, STATS_UPDATE_PERIOD);
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

    /**
     * Timer task that updates bot statistics to Top.GG
     * Use in OnReady event every hour to send post request.
     */
    private class StatUpdateTask extends TimerTask {

        private final JDA jda;
        private final String TOPGG_TOKEN;

        public StatUpdateTask(JDA jda, String TOPGG_TOKEN) {
            this.jda = jda;
            this.TOPGG_TOKEN = TOPGG_TOKEN;
        }

        /**
         * Sends a post request to top.gg to update stats.
         */
        @Override
        public void run() {
            // Build post request to update server count
            RequestBody formBody = new FormBody.Builder()
                    .add("server_count", String.valueOf(jda.getGuilds().size()))
                    .add("shard_count", String.valueOf(jda.getShardManager().getShardsTotal()))
                    .build();
            String url = "https://top.gg/api/bots/" + jda.getSelfUser().getId() + "/stats";
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .addHeader("Authorization", "Bearer " + TOPGG_TOKEN)
                    .build();
            Call call = bot.httpClient.newCall(request);

            // Execute post request
            try {
                Response response = call.execute();
                if (response.code() != 200) {
                    System.err.println("Warning: Unable to update Top.GG statistics! [code=" + response.code() + "]");
                }
                response.close();
            } catch (IOException e) {
                System.err.println("ERROR IOException: Unable to update Top.GG statistics!");
            }
        }
    }
}
