package sallman.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sallman.SallmanException;
import sallman.Storage;
import sallman.TaskList;
import sallman.Ui;
import sallman.task.Todo;

/**
 * Tests that commands refuse requests that would change nothing, and that a
 * refused command leaves no step behind for undo.
 */
public class CommandTest {

    @TempDir
    private Path folder;

    private TaskList tasks;
    private Ui ui;
    private Storage storage;

    @BeforeEach
    public void setUp() {
        tasks = new TaskList();
        ui = new Ui(false);
        storage = new Storage(folder.resolve("tasks.txt").toString());
    }

    @Test
    public void add_sameTaskAgain_refusedAndNamesTheExistingOne() throws Exception {
        new AddCommand(new Todo("read book")).execute(tasks, ui, storage);

        AddCommand duplicate = new AddCommand(new Todo("Read Book"));
        SallmanException e = assertThrows(SallmanException.class, () -> duplicate.execute(tasks, ui, storage));

        assertEquals("Great minds think alike! You already have that task, so I didn't add it again:", e.getMessage());
        assertEquals("  1.[T][ ] read book", e.toLines()[1]);
        assertEquals(1, tasks.size());
    }

    @Test
    public void add_refusedDuplicate_leavesNoUndoStep() throws Exception {
        new AddCommand(new Todo("read book")).execute(tasks, ui, storage);
        int undoStepsBefore = tasks.getUndoCount();

        AddCommand duplicate = new AddCommand(new Todo("read book"));
        assertThrows(SallmanException.class, () -> duplicate.execute(tasks, ui, storage));

        assertEquals(undoStepsBefore, tasks.getUndoCount());
    }

    @Test
    public void mark_taskAlreadyDone_refusedAndLeavesNoUndoStep() throws Exception {
        new AddCommand(new Todo("read book")).execute(tasks, ui, storage);
        new MarkCommand(true, "1").execute(tasks, ui, storage);
        int undoStepsBefore = tasks.getUndoCount();

        MarkCommand markAgain = new MarkCommand(true, "1");
        SallmanException e = assertThrows(SallmanException.class, () -> markAgain.execute(tasks, ui, storage));

        assertEquals("Great news! Task 1 is already marked as done:", e.getMessage());
        assertEquals(undoStepsBefore, tasks.getUndoCount());
    }

    @Test
    public void unmark_taskNotDone_refused() throws Exception {
        new AddCommand(new Todo("read book")).execute(tasks, ui, storage);

        MarkCommand unmark = new MarkCommand(false, "1");
        SallmanException e = assertThrows(SallmanException.class, () -> unmark.execute(tasks, ui, storage));

        assertTrue(e.getMessage().startsWith("It looks like task 1 isn't marked as done"));
    }
}
