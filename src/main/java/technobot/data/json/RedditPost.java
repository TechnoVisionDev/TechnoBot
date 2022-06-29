package technobot.data.json;

/**
 * Represents a reddit post retrieved from an API.
 * Used by OkHttp and Gson to convert JSON to java code.
 *
 * @author TechnoVision
 */
public class RedditPost {

    public static final String UPVOTE_EMOJI = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/120/sony/336/thumbs-up_1f44d.png";

    private final String title;
    private final String postLink;
    private final String url;
    private final int ups;

    public RedditPost(String title, String postLink, String url, int ups) {
        this.title = title;
        this.postLink = postLink;
        this.url = url;
        this.ups = ups;
    }

    public String getTitle() {
        return title;
    }

    public String getPostLink() {
        return postLink;
    }

    public String getImageUrl() {
        return url;
    }

    public String getUpvotes() {
        return String.valueOf(ups);
    }
}
