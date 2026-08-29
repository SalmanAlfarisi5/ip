package sallman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sallman.task.Deadline;
import sallman.task.Event;
import sallman.task.Task;
import sallman.task.Todo;

/**
 * Tests that {@link Storage} writes a task list that it can read back, and that
 * a damaged data file costs the user only the damaged lines.
 * <p>
 * Every test writes inside a temporary folder supplied by JUnit, so none of
 * them can touch the real task list.
 */
public class StorageTest {

    @TempDir
    private Path folder;

    @Test
    public void loadThenSave_everyTaskType_roundTripsUnchanged() throws Exception {
        Storage storage = new Storage(folder.resolve("tasks.txt").toString());
        Todo todo = new Todo("read book");
        todo.markAsDone();

        storage.save(List.of(todo,
                new Deadline("return book", LocalDate.of(2019, 10, 15)),
                new Event("conference", LocalDate.of(2019, 10, 14),
                        LocalDate.of(2019, 10, 17))));
        List<Task> loaded = storage.load();

        // Comparing the display form checks the type, description, done status
        // and dates all survived the trip through the file.
        assertEquals(3, loaded.size());
        assertEquals("[T][X] read book", loaded.get(0).toString());
        assertEquals("[D][ ] return book (by: Oct 15 2019)", loaded.get(1).toString());
        assertEquals("[E][ ] conference (from: Oct 14 2019 to: Oct 17 2019)",
                loaded.get(2).toString());
    }

    @Test
    public void load_fileDoesNotExist_emptyListAndNoError() throws Exception {
        // The first run on a new computer has no data file, which is not a fault.
        Storage storage = new Storage(folder.resolve("never-written.txt").toString());
        assertTrue(storage.load().isEmpty());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    @Test
    public void save_folderDoesNotExist_folderCreated() throws Exception {
        Path nested = folder.resolve("data").resolve("tasks.txt");
        assertFalse(Files.exists(nested.getParent()));

        new Storage(nested.toString()).save(List.of(new Todo("read book")));

        assertTrue(Files.exists(nested));
    }

    @Test
    public void save_emptyList_fileEmptiedNotLeftStale() throws Exception {
        // Deleting the last task must clear the file, or it would come back.
        Storage storage = new Storage(folder.resolve("tasks.txt").toString());
        storage.save(List.of(new Todo("read book")));
        storage.save(List.of());

        assertTrue(storage.load().isEmpty());
    }

    @Test
    public void loadThenSave_descriptionContainsFieldSeparator_survives() throws Exception {
        // " | " separates the fields, so a description containing it could be
        // mistaken for extra fields and silently truncated.
        Storage storage = new Storage(folder.resolve("tasks.txt").toString());
        storage.save(List.of(new Todo("read | book"),
                new Deadline("pay a | b", LocalDate.of(2019, 10, 15))));

        List<Task> loaded = storage.load();

        assertEquals("[T][ ] read | book", loaded.get(0).toString());
        assertEquals("[D][ ] pay a | b (by: Oct 15 2019)", loaded.get(1).toString());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    @Test
    public void load_corruptedLines_goodLinesKeptAndBadOnesReported() throws Exception {
        Storage storage = write(
                "T | 1 | read book",
                "GARBAGE LINE",
                "D | 0 | return book | 2019-10-15",
                "X | 0 | alien task",
                "T | 7 | bad flag",
                "D | 0 | broken date | not-a-date");

        List<Task> loaded = storage.load();

        assertEquals(2, loaded.size());
        assertEquals("[T][X] read book", loaded.get(0).toString());
        assertEquals("[D][ ] return book (by: Oct 15 2019)", loaded.get(1).toString());

        List<String> skipped = storage.getSkippedLines();
        assertEquals(4, skipped.size());
        // Each report names the line number, so a hand-edited file can be fixed.
        assertEquals("line 2: expected at least 3 fields, found 1", skipped.get(0));
        assertEquals("line 4: unknown task type \"X\"", skipped.get(1));
        assertEquals("line 5: the done flag should be 0 or 1, found \"7\"", skipped.get(2));
        assertEquals("line 6: the due date \"not-a-date\" is not a date", skipped.get(3));
    }

    @Test
    public void load_blankLines_skippedSilently() throws Exception {
        // A trailing newline is normal, so blank lines must not be reported.
        Storage storage = write("T | 0 | read book", "", "   ");

        assertEquals(1, storage.load().size());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    @Test
    public void load_calledTwice_reportsAreNotCumulative() throws Exception {
        // The skipped lines are remembered between calls, so they must be
        // cleared each time or the same faults would be reported twice.
        Storage storage = write("GARBAGE LINE");

        storage.load();
        storage.load();

        assertEquals(1, storage.getSkippedLines().size());
    }

    /**
     * Writes the given lines to a fresh data file inside the temporary folder.
     *
     * @param lines contents of the file, one line each
     * @return storage backed by that file
     */
    private Storage write(String... lines) throws IOException {
        Path file = folder.resolve("tasks.txt");
        Files.write(file, List.of(lines));
        return new Storage(file.toString());
    }
}
