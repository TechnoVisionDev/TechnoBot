package technobot.util.enums;

/**
 * Represents blackjack playing cards with a value and emoji.
 *
 * @author TechnoVision
 */
public enum Cards {
    TWO_HEARTS(2, "<:2H:992579076600643725>"),
    THREE_HEARTS(3, "<:3H:992579077863125013>"),
    FOUR_HEARTS(4, "<:4H:992579079347896350>"),
    FIVE_HEARTS(5, "<:5H:992579080656519218>"),
    SIX_HEARTS(6, "<:6H:992579081453449339>"),
    SEVEN_HEARTS(7, "<:7H:992579082871132250>"),
    EIGHT_HEARTS(8, "<:8H:992579084020367380>"),
    NINE_HEARTS(9, "<:9H:992579084955697233>"),
    TEN_HEARTS(10, "<:10H:992579086746669106>"),
    JACK_HEARTS(10, "<:jH:992579088994795620>"),
    KING_HEARTS(10, "<:kH:992579090106306681>"),
    QUEEN_HEARTS(10, "<:qH:992579091389743134>"),
    ACE_HEARTS(11, "<:aH:992579087858143322>", true),

    TWO_SPADES(2, "<:2S:992579185207947304>"),
    THREE_SPADES(3, "<:3S:992579186537545738>"),
    FOUR_SPADES(4, "<:4S:992579187867136100>"),
    FIVE_SPADES(5, "<:5S:992579188945072220>"),
    SIX_SPADES(6, "<:6S:992579190190772254>"),
    SEVEN_SPADES(7, "<:7S:992579191272898650>"),
    EIGHT_SPADES(8, "<:8S:992579192329883719>"),
    NINE_SPADES(9, "<:9S:992579193558802522>"),
    TEN_SPADES(10, "<:10S:992579195156840478>"),
    JACK_SPADES(10, "<:jS:992579197983793242>"),
    KING_SPADES(10, "<:kS:992579198856216677>"),
    QUEEN_SPADES(10, "<:qS:992579200542310400>"),
    ACE_SPADES(11, "<:aS:992579196859719700>", true),

    TWO_CLUBS(2, "<:2C:992579284587790486>"),
    THREE_CLUBS(3, "<:3C:992579286269689979>"),
    FOUR_CLUBS(4, "<:4C:992579287653810186>"),
    FIVE_CLUBS(5, "<:5C:992579288584953917>"),
    SIX_CLUBS(6, "<:6C:992579290308817067>"),
    SEVEN_CLUBS(7, "<:7C:992579291856511106>"),
    EIGHT_CLUBS(8, "<:8C:992579293555195964>"),
    NINE_CLUBS(9, "<:9C:992579294658318397>"),
    TEN_CLUBS(10, "<:10C:992579295853682728>"),
    JACK_CLUBS(10, "<:jC:992579299217510400>"),
    KING_CLUBS(10, "<:kC:992579300341592136>"),
    QUEEN_CLUBS(10, "<:qC:992579301625057330>"),
    ACE_CLUBS(11, "<:aC:992579297493659719>", true),

    TWO_DIAMONDS(2, "<:2D:992579377407733780>"),
    THREE_DIAMONDS(3, "<:3D:992579378846388224>"),
    FOUR_DIAMONDS(4, "<:4D:992579380255658014>"),
    FIVE_DIAMONDS(5, "<:5D:992579381388120214>"),
    SIX_DIAMONDS(6, "<:6D:992579382633828462>"),
    SEVEN_DIAMONDS(7, "<:7D:992579383871144058>"),
    EIGHT_DIAMONDS(8, "<:8D:992579385045549206>"),
    NINE_DIAMONDS(9, "<:9D:992579386383548416>"),
    TEN_DIAMONDS(10, "<:10D:992579387625054278>"),
    JACK_DIAMONDS(10, "<:jD:992611802489815130>"),
    KING_DIAMONDS(10, "<:kD:992612076801495160>"),
    QUEEN_DIAMONDS(10, "<:qD:992612077917184100>"),
    ACE_DIAMONDS(11, "<:aD:992579388690415717>", true);

    public final int value;
    public final String emoji;
    public final boolean isAce;

    /**
     * Constructor for making non-ace playing card
     * @param value the value that this card holds.
     * @param emoji the custom emoji for this card.
     */
    Cards(int value, String emoji) {
        this.value = value;
        this.emoji = emoji;
        this.isAce = false;
    }

    /**
     * Constructor for making an ace playing card
     * @param value the value that this card holds.
     * @param emoji the custom emoji for this card.
     * @param isAce whether this card is an ace.
     */
    Cards(int value, String emoji, boolean isAce) {
        this.value = value;
        this.emoji = emoji;
        this.isAce = isAce;
    }
}
