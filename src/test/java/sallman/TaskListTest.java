package sallman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import sallman.task.Deadline;
import sallman.task.Event;
import sallman.task.Task;
import sallman.task.Todo;

/**
 * Tests that {@link TaskList} keeps its contents in order as tasks are added
 * and removed, and selects the right tasks for a given date.
 */
public class TaskListTest {

    @Test
    public void remove_fromTheMiddle_laterTasksMoveUp() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("first"));
        tasks.add(new Todo("second"));
        tasks.add(new Todo("third"));

        Task removed = tasks.remove(1);

        // The gap must close, or later task numbers would point at the wrong
        // tasks for the rest of the session.
        assertEquals("[T][ ] second", removed.toString());
        assertEquals(2, tasks.size());
        assertEquals("[T][ ] first", tasks.get(0).toString());
        assertEquals("[T][ ] third", tasks.get(1).toString());
    }

    @Test
    public void remove_theOnlyTask_listBecomesEmpty() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("only"));

        tasks.remove(0);

        assertEquals(0, tasks.size());
    }

    @Test
    public void constructor_fromExistingTasks_contentsCopied() {
        // Loading from disk hands over a plain list; the TaskList must hold its
        // own copy so later changes to either do not affect the other.
        List<Task> loaded = new ArrayList<>(List.of(new Todo("read book")));
        TaskList tasks = new TaskList(loaded);

        loaded.clear();

        assertEquals(1, tasks.size());
    }

    @Test
    public void asList_modified_originalUnaffected() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        tasks.asList().clear();

        assertEquals(1, tasks.size());
    }

    @Test
    public void tasksOn_deadlineDueThatDay_matched() {
        TaskList tasks = new TaskList();
        tasks.add(new Deadline("return book", LocalDate.of(2019, 10, 15)));

        assertEquals(1, tasks.tasksOn(LocalDate.of(2019, 10, 15)).size());
        assertTrue(tasks.tasksOn(LocalDate.of(2019, 10, 16)).isEmpty());
    }

    @Test
    public void tasksOn_dateInsideAnEvent_matched() {
        // An event covers every day it spans, so a date in the middle counts.
        // This is the case most likely to be got wrong.
        TaskList tasks = new TaskList();
        tasks.add(new Event("conference", LocalDate.of(2019, 10, 14),
                LocalDate.of(2019, 10, 17)));

        assertEquals(1, tasks.tasksOn(LocalDate.of(2019, 10, 14)).size());
        assertEquals(1, tasks.tasksOn(LocalDate.of(2019, 10, 15)).size());
        assertEquals(1, tasks.tasksOn(LocalDate.of(2019, 10, 17)).size());
        assertTrue(tasks.tasksOn(LocalDate.of(2019, 10, 13)).isEmpty());
        assertTrue(tasks.tasksOn(LocalDate.of(2019, 10, 18)).isEmpty());
    }

    @Test
    public void tasksOn_todoWithNoDate_neverMatched() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));

        assertTrue(tasks.tasksOn(LocalDate.of(2019, 10, 15)).isEmpty());
    }

    @Test
    public void tasksOn_severalMatches_returnedInListOrder() {
        TaskList tasks = new TaskList();
        tasks.add(new Deadline("first", LocalDate.of(2019, 10, 15)));
        tasks.add(new Todo("no date"));
        tasks.add(new Event("second", LocalDate.of(2019, 10, 15),
                LocalDate.of(2019, 10, 15)));

        List<Task> matches = tasks.tasksOn(LocalDate.of(2019, 10, 15));

        assertEquals(2, matches.size());
        assertEquals("[D][ ] first (by: Oct 15 2019)", matches.get(0).toString());
        assertEquals("[E][ ] second (from: Oct 15 2019 to: Oct 15 2019)",
                matches.get(1).toString());
    }
}
