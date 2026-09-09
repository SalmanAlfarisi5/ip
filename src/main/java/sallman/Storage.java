package sallman;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import sallman.task.Deadline;
import sallman.task.Event;
import sallman.task.Task;
import sallman.task.Todo;


/**
 * Loads the task list from disk when the chatbot starts, and writes it back
 * whenever the list changes.
 * <p>
 * Each task occupies one line, with fields separated by {@code " | "}:
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | 2019-10-15
 * E | 0 | project meeting | 2019-10-15 | 2019-10-16
 * T | 0 | read book | #fun,books
 * </pre>
 * The first field is the task type, the second is 1 when the task is done.
 * A line may end with a tag field, recognised by its {@code #} prefix; a task
 * with no tags simply leaves it out, so files written before tags existed are
 * still read correctly.
 */
public class Storage {

    /**
     * What separates the fields on one line of the data file. Taken from the
     * task classes that write those lines, so the reader and the writer cannot
     * disagree about the format.
     */
    private static final String SEPARATOR = Task.SEPARATOR;

    /**
     * The separator as a regular expression, for splitting a line into fields.
     * Quoted rather than written out by hand, so it keeps matching SEPARATOR
     * even if that is ever changed to something containing regex characters.
     */
    private static final String SEPARATOR_PATTERN = Pattern.quote(SEPARATOR);

    /** Where the task list is kept, relative to the folder the app runs in. */
    private final Path file;

    /**
     * Descriptions of any lines the last {@link #load()} could not understand.
     * Kept so the caller can tell the user what was dropped, rather than
     * failing the whole load because of one bad line.
     */
    private final List<String> skippedLines = new ArrayList<>();

    /**
     * Creates storage backed by the given file.
     *
     * @param filePath path to the data file, written with {@code /} separators;
     *                 {@link Path#of} converts them to whatever the current OS
     *                 uses, so the same string works everywhere
     */
    public Storage(String filePath) {
        this.file = Path.of(filePath);
    }

    /**
     * Reads the saved tasks.
     *
     * @return the saved tasks, or an empty list when there is no data file yet
     * @throws SallmanException if the file exists but cannot be read
     */
    public List<Task> load() throws SallmanException {
        List<Task> tasks = new ArrayList<>();
        skippedLines.clear();
        if (!Files.exists(file)) {
            // First run on this computer: no data file is not an error.
            return tasks;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            throw new SallmanException("I couldn't read your saved tasks from " + file + ".",
                    "Starting with an empty list this time.");
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(parseLine(line));
            } catch (SallmanException e) {
                // One damaged line should not cost the user every other task,
                // so record it and carry on with the rest of the file.
                skippedLines.add("line " + (i + 1) + ": " + e.getMessage());
            }
        }
        return tasks;
    }

    /**
     * Returns what went wrong with any lines skipped by the last load.
     *
     * @return one description per skipped line, empty when the file was clean
     */
    public List<String> getSkippedLines() {
        return skippedLines;
    }

    /**
     * Writes the tasks to disk, creating the containing folder if needed.
     *
     * @param tasks the tasks to save
     * @throws SallmanException if the file cannot be written
     */
    public void save(List<Task> tasks) throws SallmanException {
        try {
            Path folder = file.getParent();
            if (folder != null) {
                // Creating the folder is harmless when it already exists.
                Files.createDirectories(folder);
            }
            Files.write(file, tasks.stream()
                    .map(Task::toFileFormat)
                    .toList());
        } catch (IOException e) {
            throw new SallmanException("I couldn't save your tasks to " + file + ".",
                    "The change is still in this session, but may be lost when you exit.");
        }
    }

    /**
     * Rebuilds one task from its saved line.
     * <p>
     * Only the type and done flag are fixed fields. A description may itself
     * contain the separator, so it is taken as everything left over once the
     * trailing date fields have been split off from the right.
     *
     * @param line one non-blank line of the data file
     * @return the task that line describes
     * @throws SallmanException if the line is not in the expected format; the
     *                          message is a fragment naming the specific fault,
     *                          for the caller to report against a line number
     */
    private static Task parseLine(String line) throws SallmanException {
        String[] head = line.split(SEPARATOR_PATTERN, 3);
        if (head.length < 3) {
            throw new SallmanException("expected at least 3 fields, found " + head.length);
        }
        // The check above rules out fewer than three fields, and the split was
        // capped at three, so exactly three is all that is left.
        assert head.length == 3 : "expected 3 fields, found " + head.length;
        String type = head[0];
        String doneFlag = head[1];
        String rest = head[2];
        if (!doneFlag.equals("0") && !doneFlag.equals("1")) {
            throw new SallmanException("the done flag should be 0 or 1, found \""
                    + doneFlag + "\"");
        }

        String[] fieldsAndTags = splitOffTags(rest);
        Task task = readTask(type, fieldsAndTags[0]);
        applyTags(task, fieldsAndTags[1]);
        if (doneFlag.equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Splits the optional tag field off the end of a line's remaining fields.
     * <p>
     * The field is recognised by its {@code #} prefix rather than by its
     * position, because a task may carry dates, tags, both or neither, and
     * because a file saved before tags existed has no such field at all.
     *
     * @param rest everything on the line after the done flag
     * @return the remaining fields at index 0, and the tag field without its
     *         prefix at index 1, the latter empty when there is no tag field
     */
    private static String[] splitOffTags(String rest) {
        int cut = rest.lastIndexOf(SEPARATOR);
        if (cut < 0) {
            return new String[] {rest, ""};
        }
        String last = rest.substring(cut + SEPARATOR.length()).trim();
        if (!last.startsWith(Task.TAG_FIELD_PREFIX)) {
            return new String[] {rest, ""};
        }
        return new String[] {rest.substring(0, cut),
                last.substring(Task.TAG_FIELD_PREFIX.length())};
    }

    /**
     * Attaches the tags read from a saved line to the task built from it.
     *
     * @param task     the task that line describes
     * @param tagField the tag field without its prefix, possibly empty
     */
    private static void applyTags(Task task, String tagField) {
        if (tagField.isEmpty()) {
            return;
        }
        Arrays.stream(tagField.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .forEach(task::addTag);
    }

    /**
     * Builds the task named by a type marker from the fields that follow the
     * done flag.
     *
     * @param type the type marker from the start of the line
     * @param rest everything on the line after the done flag
     * @return the task those fields describe
     * @throws SallmanException if the type is unknown, or its fields are not
     *                          in the expected shape
     */
    private static Task readTask(String type, String rest) throws SallmanException {
        return switch (type) {
            case "T" -> new Todo(requireDescription(rest));
            case "D" -> readDeadline(rest);
            case "E" -> readEvent(rest);
            default -> throw new SallmanException("unknown task type \"" + type + "\"");
        };
    }

    /**
     * Builds a deadline from its saved fields.
     *
     * @param rest the description followed by the due date
     * @return the deadline that describes
     * @throws SallmanException if the due date is missing or is not a date
     */
    private static Task readDeadline(String rest) throws SallmanException {
        int cut = rest.lastIndexOf(SEPARATOR);
        if (cut < 0) {
            throw new SallmanException("a deadline needs a due date");
        }
        return new Deadline(requireDescription(rest.substring(0, cut)),
                parseStoredDate(after(rest, cut), "due date"));
    }

    /**
     * Builds an event from its saved fields.
     *
     * @param rest the description followed by the start and end dates
     * @return the event that describes
     * @throws SallmanException if either date is missing or is not a date
     */
    private static Task readEvent(String rest) throws SallmanException {
        int endCut = rest.lastIndexOf(SEPARATOR);
        int startCut = endCut < 0 ? -1 : rest.lastIndexOf(SEPARATOR, endCut - 1);
        if (startCut < 0) {
            throw new SallmanException("an event needs both a start and an end");
        }
        return new Event(requireDescription(rest.substring(0, startCut)),
                parseStoredDate(rest.substring(startCut + SEPARATOR.length(), endCut).trim(),
                        "start date"),
                parseStoredDate(after(rest, endCut), "end date"));
    }

    /**
     * Returns the text following the separator at the given position.
     *
     * @param text  the field being split
     * @param index where the separator starts
     * @return the trimmed remainder after that separator
     */
    private static String after(String text, int index) {
        return text.substring(index + SEPARATOR.length()).trim();
    }

    /**
     * Checks that a saved description is not blank.
     *
     * @param text the description field as read from the file
     * @return the trimmed description
     * @throws SallmanException if nothing is left after trimming
     */
    private static String requireDescription(String text) throws SallmanException {
        String description = text.trim();
        if (description.isEmpty()) {
            throw new SallmanException("the description is empty");
        }
        return description;
    }

    /**
     * Reads a date stored in the data file.
     *
     * @param text  the saved field
     * @param field what the field represents, for the error message
     * @return the date it represents
     * @throws SallmanException if the field is not a valid date
     */
    private static LocalDate parseStoredDate(String text, String field)
            throws SallmanException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new SallmanException("the " + field + " \"" + text + "\" is not a date");
        }
    }
}
