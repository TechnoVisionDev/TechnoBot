package technobot.handlers.economy;

import technobot.util.Localization;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static technobot.util.Localization.format;

/**
 * Handles localized responses to economy commands.
 *
 * @author TechnoVision
 */
public class EconomyLocalization {

    private final List<String> work;
    private final List<String> crimeSuccess;
    private final List<String> crimeFailure;

    /**
     * Reads economy responses into local memory
     */
    public EconomyLocalization() {
        var responses = Localization.get(s -> s.economy);
        work = responses.work.success;
        crimeSuccess = responses.crime.success;
        crimeFailure = responses.crime.failure;
    }

    /**
     * Get a reply from the list of 'work' responses.
     *
     * @param amount the amount of money earned.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply getWorkResponse(long amount) {
        int index = ThreadLocalRandom.current().nextInt(work.size());

        return new EconomyReply(format(work.get(index), amount), index + 1);
    }

    /**
     * Get a reply from the list of 'crimeSuccess' responses.
     *
     * @param amount the amount of money earned.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply getCrimeSuccessResponse(long amount) {
        int index = ThreadLocalRandom.current().nextInt(crimeSuccess.size());

        return new EconomyReply(
                format(crimeSuccess.get(index), amount),
                index + 1
        );
    }

    /**
     * Get a reply from the list of 'crimeFail' responses.
     *
     * @param amount the amount of money list.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply getCrimeFailResponse(long amount) {
        int index = ThreadLocalRandom.current().nextInt(crimeFailure.size());

        return new EconomyReply(
                format(crimeFailure.get(index), amount),
                index + 1,
                false
        );
    }
}
