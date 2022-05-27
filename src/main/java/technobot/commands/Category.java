package technobot.commands;

/**
 * Category that represents a group of similar commands.
 * Each category has a name and an emoji.
 *
 * @author TechnoVision
 */
public enum Category {
    STAFF(":computer:", "Staff"),
    LEVELS(":chart_with_upwards_trend:", "Levels"),
    MUSIC(":musical_note:", "Music"),
    ECONOMY(":moneybag:", "Economy"),
    TICKETS(":tickets:", "Tickets"),
    STARBOARD(":star:", "Starboard"),
    FUN(":smile:", "Fun"),
    CONFIG(":gear:", "Config"),
    UTILITY(":tools:", "Utility"),
    GREETINGS(":wave:", "Greetings"),
    SUGGESTIONS(":thought_balloon:", "Suggestions"),
    ROLES(":scroll:", "Roles");

    public final String emoji;
    public final String name;

    Category(String emoji, String name) {
        this.emoji = emoji;
        this.name = name;
    }
}
