package technobot.handlers.economy;

/**
 * Represents a reply to an economy command.
 * Used in embeds to display information about the command run.
 *
 * @author TechnoVision
 */
public record EconomyReply(String response, int id, boolean isSuccess) {
    public EconomyReply(String response, int id) {
        this(response, id, true);
    }
}
