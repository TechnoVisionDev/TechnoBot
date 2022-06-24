package technobot.handlers;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.lang.Nullable;
import kotlin.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.bson.conversions.Bson;
import technobot.TechnoBot;
import technobot.data.cache.moderation.Ban;
import technobot.data.cache.moderation.Moderation;
import technobot.data.cache.moderation.Warning;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles moderation and warnings. Interfaces with POJO objects.
 *
 * @author TechnoVision
 */
public class ModerationHandler {

    private Moderation moderation;
    private final ScheduledExecutorService unbanScheduler;
    private final Bson filter;

    private final Guild guild;
    private final TechnoBot bot;

    /**
     * Sets up POJO objects in database.
     *
     * @param bot instance of TechnoBot.
     * @param guild ID of the guild this data is for.
     */
    public ModerationHandler(TechnoBot bot, Guild guild) {
        this.bot = bot;
        this.guild = guild;
        this.unbanScheduler = Executors.newScheduledThreadPool(10);

        // Get POJO objects from database
        filter = Filters.eq("guild", guild.getIdLong());
        moderation = bot.database.moderation.find(filter).first();
        if (moderation == null) {
            moderation = new Moderation(guild.getIdLong());
            bot.database.moderation.insertOne(moderation);
        }

        // Schedule and manage unbans
        for (Ban ban : moderation.getBans().values()) {
            bot.shardManager.retrieveUserById(ban.getUser()).queue(target -> {
                if (ban.getTimestamp() + TimeUnit.DAYS.toMillis(ban.getDays()) <= System.currentTimeMillis()) {
                    removeBan(target);
                } else {
                    unbanScheduler.schedule(() -> removeBan(target), ban.getDays(), TimeUnit.DAYS);
                }
            });
        }
    }

    /**
     * Creates a private message embed for one of the moderation commands with reason.
     *
     * @param moderatorID the moderator who ran the command.
     * @param action the command action (Kick, Ban, Warn, etc).
     * @param reason the reason for the action.
     * @param color the color to display on the embed.
     * @return embed custom to this case.
     */
    public MessageEmbed createCaseMessage(long moderatorID, String action, String reason, int color) {
        return caseMessageHelper(moderatorID, action, reason, null, color);
    }

    /**
     * Creates a private message embed for one of the moderation commands with reason and duration.
     *
     * @param moderatorID the moderator who ran the command.
     * @param action the command action (Kick, Ban, Warn, etc).
     * @param reason the reason for the action.
     * @param duration the duration of the action (days, weeks, months, etc).
     * @param color the color to display on the embed.
     * @return embed custom to this case.
     */
    public MessageEmbed createCaseMessage(long moderatorID, String action, String reason, String duration, int color) {
        return caseMessageHelper(moderatorID, action, reason, duration, color);
    }

    /**
     * Creates a private message embed for one of the moderation commands.
     *
     * @param moderatorID the moderator who ran the command.
     * @param action the command action (Kick, Ban, Warn, etc).
     * @param color the color to display on the embed.
     * @return embed custom to this case.
     */
    public MessageEmbed createCaseMessage(long moderatorID, String action, int color) {
        return caseMessageHelper(moderatorID, action, null, null, color);
    }

    /**
     * Creates the actual embed for createCaseMessage(), ignoring null values.
     */
    private MessageEmbed caseMessageHelper(long moderatorID, String action, String reason, String duration, int color) {
        String text = "**Server:** " + guild.getName();
        text += "\n**Actioned by:** <@!" + moderatorID + ">";
        text += "\n**Action:** " + action;
        if (duration != null) text += "\n**Duration:** " + duration;
        if (reason != null) text += "\n**Reason:** " + reason;
        return new EmbedBuilder().setColor(color).setDescription(text).setTimestamp(new Date().toInstant()).build();
    }

    /**
     * Checks if bot can run staff command against this member.
     *
     * @param target the member targeted by this command.
     * @return true if bot can target this member, otherwise false.
     */
    public boolean canTargetMember(Member target) {
        // Check that target is not the owner of the guild
        if (target == null) return true;
        if (target.isOwner()) return false;

        // Check if bot has a higher role than user
        int botPos = guild.getBotRole().getPosition();
        for (Role role : target.getRoles()) {
            if (role.getPosition() >= botPos) {
                return false;
            }
        }
        return true;
    }

    /**
     * Schedules a user to be unbanned after a set number of days.
     * Also adds this information to the database.
     *
     * @param guild the guild this user was banned from.
     * @param target the user to unban.
     * @param days the days to wait before unbanning.
     */
    public void scheduleUnban(Guild guild, User target, int days) {
        // Add banned user and timestamp to database
        Ban ban = new Ban(target.getIdLong(), System.currentTimeMillis(), days);
        moderation.addBan(target.getId(), ban);
        Bson addBan = Updates.set("bans."+target.getId(), ban);
        bot.database.moderation.updateOne(filter, addBan);

        // Start scheduled task
        unbanScheduler.schedule(() -> removeBan(target), days, TimeUnit.DAYS);
    }

    /**
     * Check if a user currently has a timed ban.
     *
     * @param userID the string ID of the user to check.
     * @return true if user has timed ban, otherwise false.
     */
    public boolean hasTimedBan(String userID) {
        return moderation.getBans().containsKey(userID);
    }

    /**
     * Lifts a ban from local cache, server, and database.
     *
     * @param target the user to unban.
     */
    public void removeBan(User target) {
        // Unban user
        String userID = target.getId();
        guild.unban(target).queue(success -> {}, fail -> {});
        moderation.removeBan(userID);

        // Remove ban from database
        Bson removeBan = Updates.unset("bans."+userID);
        bot.database.moderation.updateOne(filter, removeBan);
    }

    /**
     * Adds a warning to the local cache and database.
     *
     * @param reason the reason for this warning.
     * @param target the ID of the user receiving this warning.
     * @param staff the ID of the staff giving this warning.
     */
    public void addWarning(String reason, long target, long staff) {
        // Update local cache
        int total = moderation.getTotal() + 1;
        int count = moderation.getCount() + 1;
        Warning warning = new Warning(total, System.currentTimeMillis(), reason, target, staff);
        moderation.setTotal(total);
        moderation.setCount(count);
        moderation.addWarning(target, warning);

        // Update MongoDB database
        Bson update = Updates.push("warnings."+target, warning);
        Bson update2 = Updates.inc("count", 1);
        Bson update3 = Updates.inc("total", 1);
        bot.database.moderation.updateOne(filter, Filters.and(update, update2, update3));
    }

    /**
     * Retrieves the list of warnings for a specified user.
     *
     * @param target the user to get warning for.
     * @return a list of warning objects for this user.
     */
    public List<Warning> getWarnings(String target) {
        return moderation.getWarnings().get(target);
    }

    /**
     * Clear all warnings for a specified user.
     *
     * @param target the ID of the user to target.
     * @return the number of warnings cleared.
     */
    public int clearWarnings(long target) {
        int count = moderation.clearWarnings(target);
        if (count > 0) {
            Bson update = Updates.unset("warnings."+target);
            Bson update2 = Updates.set("count", moderation.getCount());
            bot.database.moderation.updateOne(filter, Filters.and(update, update2));
        }
        return count;
    }

    /**
     * Removes a warning with a specific ID.
     *
     * @param id the ID of the warning to target.
     * @return the number of warnings cleared.
     */
    public int removeWarning(int id) {
        Pair<String, Integer> result = moderation.removeWarning(id);
        int index = result.getSecond();
        String target = result.getFirst();
        if (index >= 0) {
            Bson update = Updates.unset("warnings."+target+"."+index);
            Bson update2 = Updates.set("count", moderation.getCount());
            bot.database.moderation.updateOne(filter, Filters.and(update, update2));
            bot.database.moderation.updateOne(filter, Updates.pull("warnings."+target, null));
            return 1;
        }
        return 0;
    }

    /**
     * Sets the mute role for this guild.
     *
     * @param roleID the ID of the mute role to set.
     */
    public void setMuteRole(long roleID) {
        moderation.setMuteRole(roleID);
        bot.database.moderation.updateOne(filter, Updates.set("mute_role", roleID));
    }

    /**
     * Get the mute role for this guild if set.
     *
     * @return the set mute role, or null if not set or invalid.
     */
    public @Nullable Role getMuteRole() {
        Long roleID = moderation.getMuteRole();
        if (roleID == null) return null;
        return guild.getRoleById(roleID);
    }

    /**
     * Add a user to the list of mutes (for role persists).
     *
     * @param userID the ID of the user to mute.
     */
    public void muteUser(long userID) {
        moderation.addMute(userID);
        bot.database.moderation.updateOne(filter, Updates.addToSet("mutes", userID));
    }

    /**
     * Removes a user from the list of mutes (for role persists).
     *
     * @param userID the ID of the user to unmute.
     */
    public void unMuteUser(long userID) {
        moderation.removeMute(userID);
        bot.database.moderation.updateOne(filter, Updates.pull("mutes", userID));
    }

    /**
     * Add the mute role to a member (to be used for role persists)
     *
     * @param member the member to get muted.
     */
    public void persistMuteRole(Member member) {
        if (moderation.getMutes().contains(member.getIdLong())) {
            Role muteRole = guild.getRoleById(moderation.getMuteRole());
            if (muteRole != null) {
                guild.addRoleToMember(member, muteRole).queue();
            }
        }
    }
}
