package sallman.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests the task types: how each shows itself to the user, how each is written
 * to the data file, and which dates each falls on.
 */
public class TaskTest {

    @Test
    public void markAsDone_thenNotDone_statusIconFollows() {
        Task task = new Todo("read book");
        assertEquals(" ", task.getStatusIcon());

        task.markAsDone();
        assertEquals("X", task.getStatusIcon());

        task.markAsNotDone();
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    public void toString_eachType_ownIconAndFields() {
        assertEquals("[T][ ] read book", new Todo("read book").toString());
        assertEquals("[D][ ] return book (by: Oct 15 2019)",
                new Deadline("return book", LocalDate.of(2019, 10, 15)).toString());
        assertEquals("[E][ ] conference (from: Oct 14 2019 to: Oct 17 2019)",
                new Event("conference", LocalDate.of(2019, 10, 14),
                        LocalDate.of(2019, 10, 17)).toString());
    }

    @Test
    public void toFileFormat_eachType_savedInInputFormat() {
        // Dates are stored the way they are typed, not the way they are shown,
        // so the file can be read back whatever the display format becomes.
        assertEquals("T | 0 | read book", new Todo("read book").toFileFormat());
        assertEquals("D | 0 | return book | 2019-10-15",
                new Deadline("return book", LocalDate.of(2019, 10, 15)).toFileFormat());
        assertEquals("E | 0 | conference | 2019-10-14 | 2019-10-17",
                new Event("conference", LocalDate.of(2019, 10, 14),
                        LocalDate.of(2019, 10, 17)).toFileFormat());
    }

    @Test
    public void toFileFormat_doneTask_flagIsOne() {
        Task task = new Todo("read book");
        task.markAsDone();
        assertEquals("T | 1 | read book", task.toFileFormat());
    }

    @Test
    public void isOn_todo_neverOnAnyDate() {
        // A todo carries no date, so it must not appear under any day.
        assertFalse(new Todo("read book").isOn(LocalDate.of(2019, 10, 15)));
    }

    @Test
    public void isOn_deadline_onlyItsDueDate() {
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 10, 15));

        assertTrue(deadline.isOn(LocalDate.of(2019, 10, 15)));
        assertFalse(deadline.isOn(LocalDate.of(2019, 10, 14)));
        assertFalse(deadline.isOn(LocalDate.of(2019, 10, 16)));
    }

    @Test
    public void isOn_event_everyDayItSpansIncludingTheEnds() {
        Event event = new Event("conference", LocalDate.of(2019, 10, 14),
                LocalDate.of(2019, 10, 17));

        assertFalse(event.isOn(LocalDate.of(2019, 10, 13)));
        assertTrue(event.isOn(LocalDate.of(2019, 10, 14)));
        assertTrue(event.isOn(LocalDate.of(2019, 10, 16)));
        assertTrue(event.isOn(LocalDate.of(2019, 10, 17)));
        assertFalse(event.isOn(LocalDate.of(2019, 10, 18)));
    }

    @Test
    public void isOn_singleDayEvent_onThatDayOnly() {
        Event event = new Event("standup", LocalDate.of(2019, 10, 15),
                LocalDate.of(2019, 10, 15));

        assertTrue(event.isOn(LocalDate.of(2019, 10, 15)));
        assertFalse(event.isOn(LocalDate.of(2019, 10, 16)));
    }
}
