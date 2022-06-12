package technobot.handlers.economy;

/**
 * Represents a reply to an economy command.
 * Used in embeds to display information about the command run.
 *
 * @author TechnoVision
 */
public record EconomyReply(String response, int id) {

    public String getResponse() {
        return response;
    }

    public int getId() {
        return id;
    }
}
