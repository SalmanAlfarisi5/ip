/**
 * Signals that the user's input could not be carried out, for a reason worth
 * explaining to them.
 * <p>
 * This is a checked exception, so the compiler makes sure every place that can
 * reject input is handled somewhere rather than reaching the user as a stack
 * trace.
 */
public class SallmanException extends Exception {

    /**
     * An example of a correct command, shown under the message so the user can
     * see how to fix the problem. May be {@code null} when no example helps.
     */
    private final String hint;

    /**
     * Creates an exception carrying only an explanation.
     *
     * @param message what went wrong, phrased for the user
     */
    public SallmanException(String message) {
        this(message, null);
    }

    /**
     * Creates an exception carrying an explanation and a suggested fix.
     *
     * @param message what went wrong, phrased for the user
     * @param hint    an example of the correct command
     */
    public SallmanException(String message, String hint) {
        super(message);
        this.hint = hint;
    }

    /**
     * Returns this problem as the lines of a chatbot reply.
     *
     * @return the message, followed by the hint when there is one
     */
    public String[] toLines() {
        return hint == null
                ? new String[] {getMessage()}
                : new String[] {getMessage(), hint};
    }
}
