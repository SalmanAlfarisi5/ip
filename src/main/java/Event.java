/**
 * A task that runs from one point in time to another,
 * e.g. {@code project meeting} from {@code Mon 2pm} to {@code 4pm}.
 */
public class Event extends Task {

    /**
     * When the event starts. Kept as free text for now, since the chatbot does
     * not yet interpret dates.
     */
    protected String from;

    /** When the event ends, also free text. */
    protected String to;

    /**
     * Creates an event that is initially not done.
     *
     * @param description what the event involves
     * @param from        when the event starts
     * @param to          when the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns this event as it should appear to the user,
     * e.g. {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}.
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
