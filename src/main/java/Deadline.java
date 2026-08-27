import java.time.LocalDate;

/**
 * A task that must be done before a given point in time,
 * e.g. {@code submit report} by {@code 2019-10-15}.
 */
public class Deadline extends Task {

    /** When the task is due. */
    protected LocalDate by;

    /**
     * Creates a deadline that is initially not done.
     *
     * @param description what the task involves
     * @param by          when the task is due
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns whether this deadline falls due on the given date.
     *
     * @param date the date being asked about
     * @return true if the task is due that day
     */
    @Override
    public boolean isOn(LocalDate date) {
        return by.equals(date);
    }

    /**
     * Returns this deadline as one line of the data file,
     * e.g. {@code D | 0 | return book | 2019-10-15}.
     */
    @Override
    public String toFileFormat() {
        // Saved in the input format, so the file stays readable and
        // reloadable regardless of how dates are displayed.
        return "D | " + super.toFileFormat() + " | " + by;
    }

    /**
     * Returns this deadline as it should appear to the user,
     * e.g. {@code [D][ ] return book (by: Oct 15 2019)}.
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + TaskDate.format(by) + ")";
    }
}
