package sallman.task;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A single task tracked by the chatbot, together with whether it is done.
 */
public class Task {

    /**
     * What separates the fields of one saved line. Defined here because the
     * task classes are what write those lines; {@link sallman.Storage} reads
     * the same constant back, so the two cannot drift apart.
     */
    public static final String SEPARATOR = " | ";

    /**
     * Marks the tag field at the end of a saved line. Without it, a line's
     * last field could not be told apart from a date, since a task may have
     * tags, dates, both or neither.
     */
    public static final String TAG_FIELD_PREFIX = "#";

    /** What separates one tag from the next inside the saved tag field. */
    private static final String TAG_SEPARATOR = ",";

    /** Description of what the task involves, as typed by the user. */
    protected String description;

    /** Whether the task has been completed. */
    protected boolean isDone;

    /**
     * Labels the user has attached to this task, kept in the order they were
     * added so the task shows them the same way every time. Stored in lower
     * case, so that {@code #Fun} and {@code #fun} are one tag rather than two.
     */
    private final Set<String> tags = new LinkedHashSet<>();

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
     * Returns whether this task's description contains the given keyword.
     * <p>
     * The comparison ignores case, so searching for "book" also finds "Book".
     *
     * @param keyword the text being searched for
     * @return true if the description contains it
     */
    public boolean hasKeyword(String keyword) {
        String wanted = keyword.toLowerCase();
        return description.toLowerCase().contains(wanted)
                || tags.stream().anyMatch(tag -> tag.contains(wanted));
    }

    /**
     * Attaches a tag to this task, ignoring case and doing nothing if it is
     * already there.
     *
     * @param tag the label to attach, without its leading {@code #}
     * @return true if the tag was not already on this task
     */
    public boolean addTag(String tag) {
        return tags.add(tag.toLowerCase());
    }

    /**
     * Removes a tag from this task, ignoring case.
     *
     * @param tag the label to remove, without its leading {@code #}
     * @return true if the task had that tag
     */
    public boolean removeTag(String tag) {
        return tags.remove(tag.toLowerCase());
    }

    /**
     * Returns the tags on this task, in the order they were added.
     *
     * @return the tags, which cannot be changed through the returned set
     */
    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns the tags as they are shown after a task, e.g. {@code " #fun"}.
     *
     * @return the tags each prefixed with {@code #}, or an empty string
     */
    private String tagSuffix() {
        if (tags.isEmpty()) {
            return "";
        }
        return " " + tags.stream()
                .map(tag -> TAG_FIELD_PREFIX + tag)
                .collect(Collectors.joining(" "));
    }

    /**
     * Returns the tags as the final field of a saved line.
     *
     * @return the separator and tag field, or an empty string when untagged
     */
    private String tagField() {
        if (tags.isEmpty()) {
            return "";
        }
        return SEPARATOR + TAG_FIELD_PREFIX + String.join(TAG_SEPARATOR, tags);
    }

    /**
     * Returns the fields a subclass saves between the description and the
     * tags, each already prefixed with a separator.
     * <p>
     * Dates are written here rather than by an overridden
     * {@link #toFileFormat()}, so that the tag field stays last on the line
     * whichever kind of task is being saved.
     *
     * @return the extra fields, empty for a task that holds no dates
     */
    protected String dateFields() {
        return "";
    }

    /**
     * Returns what a subclass shows after the description, e.g. a due date.
     * <p>
     * Kept separate from {@link #toString()} for the same reason as
     * {@link #dateFields()}: it lets the tags stay at the end of the line.
     *
     * @return the extra detail, empty for a task that holds no dates
     */
    protected String details() {
        return "";
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
        return (isDone ? "1" : "0") + SEPARATOR + description + dateFields() + tagField();
    }

    /**
     * Returns this task as it should appear to the user, e.g. {@code [X] read book}.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description + details() + tagSuffix();
    }
}
