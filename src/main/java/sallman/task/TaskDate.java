package sallman.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import sallman.SallmanException;


/**
 * Converts between the date format the user types and the one shown back to
 * them.
 * <p>
 * Dates are entered as {@code yyyy-mm-dd} and displayed as
 * {@code MMM dd yyyy}, e.g. {@code 2019-10-15} is shown as {@code Oct 15 2019}.
 * Keeping both conversions here means the two formats are defined once, rather
 * than once per task type.
 */
public final class TaskDate {

    /** The input format, named here so error messages can quote it. */
    public static final String INPUT_FORMAT = "yyyy-mm-dd";

    /** An example date in the input format, for use in error hints. */
    public static final String EXAMPLE = "2019-10-15";

    /** How a date is shown to the user. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy");

    /** Not meant to be instantiated: this class only holds static helpers. */
    private TaskDate() {
    }

    /**
     * Reads a date the user typed.
     *
     * @param text what the user entered where a date was expected
     * @return the date it represents
     * @throws SallmanException if the text is not a date in the input format
     */
    public static LocalDate parse(String text) throws SallmanException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            // Translate Java's exception into one that names the format wanted.
            throw new SallmanException("I couldn't read \"" + text + "\" as a date.",
                    "Use " + INPUT_FORMAT + ", e.g. " + EXAMPLE + ".");
        }
    }

    /**
     * Renders a date for display.
     *
     * @param date the date to show
     * @return the date in {@code MMM dd yyyy} form, e.g. {@code Oct 15 2019}
     */
    public static String format(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }
}
