/**
 * A task that must be done before a given point in time,
 * e.g. {@code submit report} by {@code Sunday}.
 */
public class Deadline extends Task {

    /**
     * When the task is due. Kept as free text for now, since the chatbot does
     * not yet interpret dates.
     */
    protected String by;

    /**
     * Creates a deadline that is initially not done.
     *
     * @param description what the task involves
     * @param by          when the task is due
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns this deadline as one line of the data file,
     * e.g. {@code D | 0 | return book | Sunday}.
     */
    @Override
    public String toFileFormat() {
        return "D | " + super.toFileFormat() + " | " + by;
    }

    /**
     * Returns this deadline as it should appear to the user,
     * e.g. {@code [D][ ] return book (by: Sunday)}.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
