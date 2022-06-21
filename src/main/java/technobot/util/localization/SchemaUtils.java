package technobot.util.localization;

public class SchemaUtils {
    public record Result(String success, String failure) {
    }

    public record Value(String set, String reset) {
    }
}
