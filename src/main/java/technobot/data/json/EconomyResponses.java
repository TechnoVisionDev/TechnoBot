package technobot.data.json;

/**
 * Represents list of responses to economy commands.
 * Used by OkHttp and Gson to convert JSON to java code.
 *
 * @author TechnoVision
 */
public class EconomyResponses {

    private final String[] work;
    private final String[] crimeSuccess;
    private final String[] crimeFail;

    public EconomyResponses(String[] work, String[] crimeSuccess, String[] crimeFail) {
        this.work = work;
        this.crimeSuccess = crimeSuccess;
        this.crimeFail = crimeFail;
    }

    public String[] getWork() {
        return work;
    }

    public String[] getCrimeSuccess() {
        return crimeSuccess;
    }

    public String[] getCrimeFail() {
        return crimeFail;
    }
}
