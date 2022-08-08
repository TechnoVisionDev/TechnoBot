package technobot.commands;

import static technobot.util.Localization.get;

/**
 * Category that represents a group of similar commands.
 * Each category has a name and an emoji.
 *
 * @author TechnoVision
 */
public enum Category {
    STAFF(":computer:", get(s -> s.utility.help.categories.staff)),
    LEVELS(":chart_with_upwards_trend:", get(s -> s.utility.help.categories.levels)),
    MUSIC(":musical_note:", get(s -> s.utility.help.categories.music)),
    ECONOMY(":moneybag:", get(s -> s.utility.help.categories.economy)),
    STARBOARD(":star:", get(s -> s.utility.help.categories.starboard)),
    FUN(":smile:", get(s -> s.utility.help.categories.fun)),
    AUTOMATION(":gear:", get(s -> s.utility.help.categories.automation)),
    UTILITY(":tools:", get(s -> s.utility.help.categories.utility)),
    GREETINGS(":wave:", get(s -> s.utility.help.categories.greetings)),
    SUGGESTIONS(":thought_balloon:", get(s -> s.utility.help.categories.suggestions)),
    CASINO(":game_die:", get(s -> s.utility.help.categories.casino)),
    PETS(":dog:", get(s -> s.utility.help.categories.pets));

    public final String emoji;
    public final String name;

    Category(String emoji, String name) {
        this.emoji = emoji;
        this.name = name;
    }
}
