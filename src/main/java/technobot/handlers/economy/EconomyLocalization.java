package technobot.handlers.economy;

import technobot.util.localization.Localization;
import technobot.util.localization.LocalizationSchema;

import java.util.concurrent.ThreadLocalRandom;

import static technobot.util.localization.Localization.format;

/**
 * Handles localized responses to economy commands.
 *
 * @author TechnoVision
 */
public class EconomyLocalization {

    private final String[] work;
    private final String[] crimeSuccess;
    private final String[] crimeFailure;

    /**
     * Reads economy responses into local memory
     */
    public EconomyLocalization() {
        LocalizationSchema.Economy responses = Localization.get(LocalizationSchema::economy);
        work = responses.work().success();
        crimeSuccess = responses.crime().success();
        crimeFailure = responses.crime().failure();
    }

    /**
     * Get a reply from the list of 'work' responses.
     *
     * @param amount the amount of money earned.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply getWorkResponse(long amount) {
        int index = ThreadLocalRandom.current().nextInt(work.length);

        return new EconomyReply(format(work[index], amount), index + 1);
    }

    /**
     * Get a reply from the list of 'crimeSuccess' responses.
     *
     * @param amount the amount of money earned.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply getCrimeSuccessResponse(long amount) {
        int index = ThreadLocalRandom.current().nextInt(crimeSuccess.length);

        return new EconomyReply(
                format(crimeSuccess[index], amount),
                index + 1,
                true
        );
    }

    /**
     * Get a reply from the list of 'crimeFail' responses.
     *
     * @param amount the amount of money list.
     * @return an EconomyReply object with response and ID number.
     */
    public EconomyReply getCrimeFailResponse(long amount) {
        int index = ThreadLocalRandom.current().nextInt(crimeFailure.length);

        return new EconomyReply(
                format(crimeFailure[index], amount),
                index + 1,
                false
        );
    }
}
