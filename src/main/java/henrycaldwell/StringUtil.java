package henrycaldwell;

/**
 * Utility class providing string formatting functions with ANSI codes.
 */
public class StringUtil {

    public static final String ANSI_RESET = "\u001B[0m"; // The ANSI code for reset.
    public static final String ANSI_ITALIC = "\u001B[3m"; // The ANSI code for italic.
    public static final String ANSI_RED = "\u001B[31m"; // The ANSI code for red.
    public static final String ANSI_GREEN = "\u001B[32m"; // The ANSI code for green.

    /**
     * Applies ANSI codes to a given text.
     * @param text The text to format.
     * @param color The ANSI code to apply.
     * @return The formatted text string.
     */
    public static String formatText(String text, String format) {
        return format + text + ANSI_RESET;
    }
}
