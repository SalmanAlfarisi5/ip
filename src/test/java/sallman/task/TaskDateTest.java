package sallman.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import sallman.SallmanException;

/**
 * Tests that {@link TaskDate} accepts the input format and only that format,
 * and shows dates back in the display format.
 */
public class TaskDateTest {

    @Test
    public void parse_inputFormat_dateReturned() throws Exception {
        assertEquals(LocalDate.of(2019, 10, 15), TaskDate.parse("2019-10-15"));
    }

    @Test
    public void parse_leapDayInALeapYear_accepted() throws Exception {
        assertEquals(LocalDate.of(2020, 2, 29), TaskDate.parse("2020-02-29"));
    }

    @Test
    public void parse_impossibleCalendarDate_exceptionThrown() {
        // These have the right shape but cannot exist, so a check on the
        // pattern alone would let them through.
        assertThrows(SallmanException.class, () -> TaskDate.parse("2019-02-30"));
        assertThrows(SallmanException.class, () -> TaskDate.parse("2019-02-29"));
        assertThrows(SallmanException.class, () -> TaskDate.parse("2019-13-01"));
    }

    @Test
    public void parse_dayFirstFormat_exceptionThrown() {
        // A natural thing to type, and wrong, so it must not be guessed at.
        assertThrows(SallmanException.class, () -> TaskDate.parse("15-10-2019"));
    }

    @Test
    public void parse_wordsRatherThanADate_exceptionNamesTheFormat() {
        SallmanException e = assertThrows(SallmanException.class, () -> TaskDate.parse("Sunday"));
        assertEquals("I couldn't read \"Sunday\" as a date.", e.getMessage());
    }

    @Test
    public void parse_emptyText_exceptionThrown() {
        assertThrows(SallmanException.class, () -> TaskDate.parse(""));
    }

    @Test
    public void format_anyDate_displayFormatUsed() {
        // Displayed differently from how it is typed, and the day is padded to
        // two digits so listed dates line up.
        assertEquals("Oct 15 2019", TaskDate.format(LocalDate.of(2019, 10, 15)));
        assertEquals("Aug 06 2019", TaskDate.format(LocalDate.of(2019, 8, 6)));
        assertEquals("Jan 01 2020", TaskDate.format(LocalDate.of(2020, 1, 1)));
    }

    @Test
    public void formatThenParse_sameDate_survivesBothConversions() throws Exception {
        LocalDate original = LocalDate.of(2019, 10, 15);
        assertEquals(original, TaskDate.parse(original.toString()));
    }
}
