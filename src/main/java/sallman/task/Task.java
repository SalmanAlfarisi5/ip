package sallman.task;

import java.time.LocalDate;

/**
 * A single task tracked by the chatbot, together with whether it is done.
 */
public class Task {

    /** Description of what the task involves, as typed by the user. */
    protected String description;

    /** Whether the task has been completed. */
    protected boolean isDone;

    /**
     * Creates a task that is initially not done.
     *
     * @param description what the task involves
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the icon shown inside the status box of this task.
     *
     * @return {@code "X"} if the task is done, a space otherwise
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /** Marks this task as done. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not done yet. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns whether this task falls on the given date.
     * <p>
     * A plain task carries no date, so it never does. Subclasses that hold
     * dates override this.
     *
     * @param date the date being asked about
     * @return true if this task is scheduled on that date
     */
    public boolean isOn(LocalDate date) {
        return false;
    }

    /**
     * Returns this task as one line of the data file, without a type marker.
     * Subclasses prepend their marker and append any dates they hold.
     *
     * @return the done flag and description, separated by {@code " | "}
     */
    public String toFileFormat() {
        return (isDone ? "1" : "0") + " | " + description;
    }

    /**
     * Returns this task as it should appear to the user, e.g. {@code [X] read book}.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
