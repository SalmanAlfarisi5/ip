import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the task list from disk when the chatbot starts, and writes it back
 * whenever the list changes.
 * <p>
 * Each task occupies one line, with fields separated by {@code " | "}:
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | 2019-10-15
 * E | 0 | project meeting | 2019-10-15 | 2019-10-16
 * </pre>
 * The first field is the task type, the second is 1 when the task is done.
 */
public class Storage {

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
            List<String> lines = new ArrayList<>();
            for (Task task : tasks) {
                lines.add(task.toFileFormat());
            }
            Files.write(file, lines);
        } catch (IOException e) {
            throw new SallmanException("I couldn't save your tasks to " + file + ".",
                    "The change is still in this session, but may be lost when you exit.");
        }
    }

    /**
     * Rebuilds one task from its saved line.
     *
     * @param line one non-blank line of the data file
     * @return the task that line describes
     * @throws SallmanException if the line is not in the expected format; the
     *                          message is a fragment naming the specific fault,
     *                          for the caller to report against a line number
     */
    private static Task parseLine(String line) throws SallmanException {
        String[] parts = line.split(" \\| ");
        if (parts.length < 3) {
            throw new SallmanException("expected at least 3 fields, found " + parts.length);
        }
        String type = parts[0];
        String doneFlag = parts[1];
        String description = parts[2].trim();
        if (!doneFlag.equals("0") && !doneFlag.equals("1")) {
            throw new SallmanException("the done flag should be 0 or 1, found \"" + doneFlag + "\"");
        }
        if (description.isEmpty()) {
            throw new SallmanException("the description is empty");
        }
        Task task = switch (type) {
        case "T" -> new Todo(description);
        case "D" -> {
            if (parts.length < 4 || parts[3].isBlank()) {
                throw new SallmanException("a deadline needs a due date");
            }
            yield new Deadline(description, parseStoredDate(parts[3].trim(), "due date"));
        }
        case "E" -> {
            if (parts.length < 5 || parts[3].isBlank() || parts[4].isBlank()) {
                throw new SallmanException("an event needs both a start and an end");
            }
            yield new Event(description, parseStoredDate(parts[3].trim(), "start date"),
                    parseStoredDate(parts[4].trim(), "end date"));
        }
        default -> throw new SallmanException("unknown task type \"" + type + "\"");
        };
        if (doneFlag.equals("1")) {
            task.markAsDone();
        }
        return task;
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
