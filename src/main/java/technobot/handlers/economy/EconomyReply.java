package technobot.handlers.economy;

/**
 * Represents a reply to an economy command.
 * Used in embeds to display information about the command run.
 *
 * @author TechnoVision
 */
public class EconomyReply {

    private final String response;
    private final int id;
    private final boolean isSuccess;

    public EconomyReply(String response, int id) {
        this.response = response;
        this.id = id;
        this.isSuccess = true;
    }

    public EconomyReply(String response, int id, boolean isSuccess) {
        this.response = response;
        this.id = id;
        this.isSuccess = isSuccess;
    }

    public String getResponse() {
        return response;
    }

    public int getId() {
        return id;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
}
