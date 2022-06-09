package technobot.util;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

/**
 * Helpful methods to be used in slash commands.
 *
 * @author TechnoVision
 */
public class CommandUtils {

    /**
     * Checks if a bot has a certain permission or is administrator.
     *
     * @param botRole the role of the bot.
     * @param permission the permission to check.
     * @return true if bot has permission, otherwise false.
     */
    public static boolean hasPermission(Role botRole, Permission permission) {
        return botRole.hasPermission(permission) || botRole.hasPermission(Permission.ADMINISTRATOR);
    }
}
