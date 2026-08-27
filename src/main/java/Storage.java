import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the task list from disk when the chatbot starts, and writes it back
 * whenever the list changes.
 * <p>
 * Each task occupies one line, with fields separated by {@code " | "}:
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | June 6th
 * E | 0 | project meeting | Aug 6th 2pm | 4pm
 * </pre>
 * The first field is the task type, the second is 1 when the task is done.
 */
public class Storage {

    /** Where the task list is kept, relative to the folder the app runs in. */
    private final Path file;

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
        for (String line : lines) {
            tasks.add(parseLine(line));
        }
        return tasks;
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
     * @param line one line of the data file
     * @return the task that line describes
     */
    private static Task parseLine(String line) {
        String[] parts = line.split(" \\| ");
        Task task = switch (parts[0]) {
        case "T" -> new Todo(parts[2]);
        case "D" -> new Deadline(parts[2], parts[3]);
        case "E" -> new Event(parts[2], parts[3], parts[4]);
        default -> throw new IllegalArgumentException("Unknown task type: " + parts[0]);
        };
        if (parts[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }
}
