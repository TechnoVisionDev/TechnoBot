package technobot.util.embeds;

/**
 * Utility enum storing color codes and helpful methods.
 *
 * @author TechnoVision
 */
public enum EmbedColor {
    DEFAULT(0x57a2ff),
    ERROR(0xdd5f53),
    SUCCESS(0x77b255),
    WARNING(0xff8c03);

    public final int color;

    EmbedColor(int hexCode) {
        this.color = hexCode;
    }
}
