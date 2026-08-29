package sallman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import sallman.command.AddCommand;
import sallman.command.DeleteCommand;
import sallman.command.ExitCommand;
import sallman.command.ListCommand;
import sallman.command.MarkCommand;
import sallman.command.OnCommand;
import sallman.task.Deadline;
import sallman.task.Event;
import sallman.task.Todo;

/**
 * Tests that {@link Parser} reads well-formed input correctly, and names the
 * specific fault in input that is not.
 */
public class ParserTest {

    @Test
    public void parse_eachKeyword_correctCommandType() throws Exception {
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-10-15"));
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 1"));
        assertInstanceOf(MarkCommand.class, Parser.parse("unmark 1"));
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book"));
    }

    @Test
    public void parse_unknownKeyword_exceptionQuotesIt() {
        SallmanException e = assertThrows(SallmanException.class, () -> Parser.parse("blah"));
        assertEquals("Sorry, I don't know what \"blah\" means.", e.getMessage());
    }

    @Test
    public void parse_extraSpacesBetweenWords_stillUnderstood() throws Exception {
        // The command and its arguments are split on any run of whitespace, so
        // sloppy spacing should not look like a different command.
        assertInstanceOf(AddCommand.class, Parser.parse("todo    read book"));
    }

    @Test
    public void parseTodo_description_taskCreated() throws Exception {
        Todo todo = (Todo) Parser.parseTodo("read book");
        assertEquals("[T][ ] read book", todo.toString());
    }

    @Test
    public void parseTodo_noDescription_exceptionThrown() {
        SallmanException e = assertThrows(SallmanException.class, () -> Parser.parseTodo(""));
        assertEquals("A todo needs a description.", e.getMessage());
    }

    @Test
    public void parseDeadline_descriptionAndDate_taskCreated() throws Exception {
        Deadline deadline = (Deadline) Parser.parseDeadline("return book /by 2019-10-15");
        assertEquals("[D][ ] return book (by: Oct 15 2019)", deadline.toString());
    }

    @Test
    public void parseDeadline_missingBy_exceptionNamesTheDelimiter() {
        SallmanException e = assertThrows(SallmanException.class,
                () -> Parser.parseDeadline("submit report"));
        assertEquals("I couldn't find a /by in that deadline.", e.getMessage());
    }

    @Test
    public void parseDeadline_missingDescription_exceptionNamesTheDescription() {
        // Distinct from the missing-/by case: the delimiter is present, so the
        // user needs to be told which side of it is empty.
        SallmanException e = assertThrows(SallmanException.class,
                () -> Parser.parseDeadline("/by 2019-10-15"));
        assertEquals("That deadline has no description before the /by.", e.getMessage());
    }

    @Test
    public void parseDeadline_missingDate_exceptionNamesTheDate() {
        SallmanException e = assertThrows(SallmanException.class,
                () -> Parser.parseDeadline("return book /by"));
        assertEquals("That deadline has no due date after the /by.", e.getMessage());
    }

    @Test
    public void parseDeadline_unreadableDate_exceptionThrown() {
        assertThrows(SallmanException.class,
                () -> Parser.parseDeadline("return book /by Sunday"));
    }

    @Test
    public void parseEvent_descriptionAndBothDates_taskCreated() throws Exception {
        Event event = (Event) Parser.parseEvent("conference /from 2019-10-14 /to 2019-10-17");
        assertEquals("[E][ ] conference (from: Oct 14 2019 to: Oct 17 2019)", event.toString());
    }

    @Test
    public void parseEvent_sameStartAndEnd_taskCreated() throws Exception {
        // A one-day event is legitimate, so the start-before-end check must
        // allow the two dates to be equal.
        Event event = (Event) Parser.parseEvent("standup /from 2019-10-15 /to 2019-10-15");
        assertEquals("[E][ ] standup (from: Oct 15 2019 to: Oct 15 2019)", event.toString());
    }

    @Test
    public void parseEvent_endBeforeStart_exceptionThrown() {
        SallmanException e = assertThrows(SallmanException.class,
                () -> Parser.parseEvent("oops /from 2019-10-20 /to 2019-10-10"));
        assertEquals("That event ends before it starts.", e.getMessage());
    }

    @Test
    public void parseEvent_missingParts_eachNamedSeparately() {
        assertEquals("I couldn't find a /from in that event.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseEvent("meeting")).getMessage());
        assertEquals("I couldn't find a /to in that event.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseEvent("meeting /from 2019-10-15")).getMessage());
        assertEquals("That event has no description before the /from.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseEvent("/from 2019-10-15 /to 2019-10-16")).getMessage());
        assertEquals("That event has no start time after the /from.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseEvent("meeting /from /to 2019-10-16")).getMessage());
        assertEquals("That event has no end time after the /to.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseEvent("meeting /from 2019-10-15 /to")).getMessage());
    }

    @Test
    public void parseTaskNumber_validNumber_zeroBasedIndexReturned() throws Exception {
        // The user counts from 1, the list counts from 0.
        assertEquals(0, Parser.parseTaskNumber("mark", "1", 3));
        assertEquals(2, Parser.parseTaskNumber("mark", "3", 3));
    }

    @Test
    public void parseTaskNumber_missingNumber_exceptionNamesTheCommand() {
        // The message quotes whichever command was used, since mark, unmark and
        // delete all share this method.
        assertEquals("mark needs a task number.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseTaskNumber("mark", "", 3)).getMessage());
        assertEquals("delete needs a task number.",
                assertThrows(SallmanException.class,
                        () -> Parser.parseTaskNumber("delete", "", 3)).getMessage());
    }

    @Test
    public void parseTaskNumber_notANumber_exceptionQuotesInput() {
        SallmanException e = assertThrows(SallmanException.class,
                () -> Parser.parseTaskNumber("mark", "abc", 3));
        assertEquals("\"abc\" is not a number.", e.getMessage());
    }

    @Test
    public void parseTaskNumber_outsideList_exceptionThrown() {
        // Both ends of the range, plus a negative, since an off-by-one here
        // would reach past the list or before it.
        assertThrows(SallmanException.class, () -> Parser.parseTaskNumber("mark", "0", 3));
        assertThrows(SallmanException.class, () -> Parser.parseTaskNumber("mark", "4", 3));
        assertThrows(SallmanException.class, () -> Parser.parseTaskNumber("mark", "-1", 3));
    }

    @Test
    public void parseTaskNumber_emptyList_exceptionSaysListIsEmpty() {
        SallmanException e = assertThrows(SallmanException.class,
                () -> Parser.parseTaskNumber("mark", "1", 0));
        assertEquals("There is no task 1: your list is empty.", e.getMessage());
    }

    @Test
    public void parseOnDate_validDate_dateReturned() throws Exception {
        assertEquals(LocalDate.of(2019, 10, 15), Parser.parseOnDate("2019-10-15"));
    }

    @Test
    public void parseOnDate_missingDate_exceptionThrown() {
        SallmanException e = assertThrows(SallmanException.class, () -> Parser.parseOnDate(""));
        assertEquals("on needs a date.", e.getMessage());
    }
}
