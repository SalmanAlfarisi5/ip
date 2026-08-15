/**
 * A task with no date or time attached to it, e.g. {@code visit new theme park}.
 */
public class Todo extends Task {

    /**
     * Creates a todo that is initially not done.
     *
     * @param description what the task involves
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Returns this todo as it should appear to the user,
     * e.g. {@code [T][ ] borrow book}.
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
