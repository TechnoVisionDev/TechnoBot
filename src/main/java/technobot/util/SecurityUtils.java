package technobot.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Util methods for securing URLs
 *
 * @author Backendt
 */
public class SecurityUtils {

    private static final List<String> ALLOWED_PROTOCOLS = List.of("http", "https");
    private static final List<String> ALLOWED_DOMAINS = List.of("youtube", "soundcloud", "twitch");

    /**
     * Check if the given url is a whitelisted domain and protocol.
     * @param urlString The url to check. Can be malformed/invalid
     * @return True if the url given is whitelisted
     */
    public static boolean isUrlWhitelisted(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        boolean isValidProtocol = ALLOWED_PROTOCOLS.contains(url.getProtocol());
        String host = url.getHost();
        if(host.equals("youtu.be")) {
            return true;
        }
        String domain = getDomain(host);
        boolean isValidDomain = ALLOWED_DOMAINS.contains(domain);
        return isValidProtocol && isValidDomain;
    }

    /**
     * Return the domain of the given host
     * @param host The host url. Ex: www.youtube.com
     * @return The domain name without subdomains. Ex: youtube
     */
    private static String getDomain(String host) {
        String[] parts = host.split("(\\.|%2E)"); // Match dot or URL-Encoded dot
        int size = parts.length;
        if(size == 1) {
            return host;
        }
        return parts[size - 2];
    }

}
