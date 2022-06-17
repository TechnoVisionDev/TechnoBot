package technobot.data.json;

/**
 * Represents an anime emote retrieved from an API.
 * Used by OkHttp and Gson to convert JSON to java code.
 *
 * @author TechnoVision
 */
public class Emote {

    private String url;

    public Emote(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
