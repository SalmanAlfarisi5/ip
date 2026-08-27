package sallman;

import java.time.LocalDate;

import sallman.command.AddCommand;
import sallman.command.Command;
import sallman.command.CommandType;
import sallman.command.DeleteCommand;
import sallman.command.ExitCommand;
import sallman.command.ListCommand;
import sallman.command.MarkCommand;
import sallman.command.OnCommand;
import sallman.task.Deadline;
import sallman.task.Event;
import sallman.task.Task;
import sallman.task.TaskDate;
import sallman.task.Todo;


/**
 * Makes sense of what the user types.
 * <p>
 * Splitting a line into a command and its arguments, reading task numbers and
 * dates, and building tasks all happen here, so the rest of the app deals in
 * commands and tasks rather than in raw text.
 */
public class Parser {

    /**
     * Worked examples shown alongside an error, so the user can see the shape
     * of a correct command instead of only being told what was wrong.
     */
    private static final String TODO_EXAMPLE = "Try: todo read book";
    private static final String DEADLINE_EXAMPLE =
            "Try: deadline return book /by " + TaskDate.EXAMPLE;
    private static final String EVENT_EXAMPLE =
            "Try: event project meeting /from " + TaskDate.EXAMPLE + " /to 2019-10-16";

    /** Not meant to be instantiated: this class only holds static helpers. */
    private Parser() {
    }

    /**
     * Turns a line of input into the command it asks for.
     * <p>
     * Only the wording is checked here. Whether a task number is in range
     * depends on the list, which this class does not see, so commands that
     * take one check it when they run.
     *
     * @param input the whole line the user typed, already trimmed
     * @return the command the user asked for
     * @throws SallmanException if the command is unknown or its arguments
     *                          cannot be read
     */
    public static Command parse(String input) throws SallmanException {
        String[] parts = splitCommand(input);
        String keyword = parts[0];
        String arguments = parts[1];

        // Rejects an unknown keyword first, so every case below is a known one.
        CommandType type = CommandType.fromKeyword(keyword);
        return switch (type) {
        case BYE -> new ExitCommand();
        case LIST -> new ListCommand();
        case ON -> new OnCommand(parseOnDate(arguments));
        case MARK -> new MarkCommand(true, arguments);
        case UNMARK -> new MarkCommand(false, arguments);
        case DELETE -> new DeleteCommand(arguments);
        case TODO -> new AddCommand(parseTodo(arguments));
        case DEADLINE -> new AddCommand(parseDeadline(arguments));
        case EVENT -> new AddCommand(parseEvent(arguments));
        };
    }

    /**
     * Splits a line into its command word and the arguments that follow.
     * <p>
     * Separating them up front means a command given without arguments still
     * reaches its own branch, where the missing part can be named, instead of
     * looking like an unknown command.
     *
     * @param input the whole line the user typed, already trimmed
     * @return the command word at index 0 and its arguments at index 1, the
     *         latter empty when none were given
     */
    private static String[] splitCommand(String input) {
        String[] words = input.split("\\s+", 2);
        String arguments = words.length > 1 ? words[1].trim() : "";
        return new String[] {words[0], arguments};
    }

    /**
     * Reads the date given to an {@code on} command.
     *
     * @param arguments text the user typed after the command
     * @return the date asked about
     * @throws SallmanException if the date is missing or cannot be read
     */
    public static LocalDate parseOnDate(String arguments) throws SallmanException {
        if (arguments.isEmpty()) {
            throw new SallmanException("on needs a date.", "Try: on " + TaskDate.EXAMPLE);
        }
        return TaskDate.parse(arguments);
    }

    /**
     * Reads the task number given to a {@code mark} or {@code unmark} command.
     *
     * @param command   the command the number was given to, used in messages
     * @param arguments text the user typed after the command
     * @param taskCount number of tasks currently in the list
     * @return the matching index into the task array, counting from 0
     * @throws SallmanException if the number is missing, not a number, or
     *                          outside the list
     */
    public static int parseTaskNumber(String command, String arguments, int taskCount)
            throws SallmanException {
        if (arguments.isEmpty()) {
            throw new SallmanException(command + " needs a task number.",
                    "Try: " + command + " 2");
        }
        int index;
        try {
            // Task numbers shown to the user start at 1, arrays start at 0.
            index = Integer.parseInt(arguments) - 1;
        } catch (NumberFormatException e) {
            // Translate Java's exception into one phrased for the user.
            throw new SallmanException("\"" + arguments + "\" is not a number.",
                    "Try: " + command + " 2");
        }
        if (taskCount == 0) {
            throw new SallmanException("There is no task " + arguments
                    + ": your list is empty.", TODO_EXAMPLE);
        }
        if (index < 0 || index >= taskCount) {
            throw new SallmanException("There is no task " + arguments + " in your list.",
                    taskCount == 1
                            ? "You only have task 1."
                            : "Pick a number from 1 to " + taskCount + ".");
        }
        return index;
    }

    /**
     * Builds a todo from the text following the {@code todo} command.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if the description is missing
     */
    public static Task parseTodo(String arguments) throws SallmanException {
        if (arguments.isEmpty()) {
            throw new SallmanException("A todo needs a description.", TODO_EXAMPLE);
        }
        return new Todo(arguments);
    }

    /**
     * Builds a deadline from the text following the {@code deadline} command,
     * e.g. {@code return book /by 2019-10-15}.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if the {@code /by}, the description, or the due
     *                          date is missing, or the date cannot be read
     */
    public static Task parseDeadline(String arguments) throws SallmanException {
        String[] parts = arguments.split("/by", 2);
        if (parts.length < 2) {
            throw new SallmanException("I couldn't find a /by in that deadline.",
                    DEADLINE_EXAMPLE);
        }
        String description = parts[0].trim();
        String by = parts[1].trim();
        if (description.isEmpty()) {
            throw new SallmanException("That deadline has no description before the /by.",
                    DEADLINE_EXAMPLE);
        }
        if (by.isEmpty()) {
            throw new SallmanException("That deadline has no due date after the /by.",
                    DEADLINE_EXAMPLE);
        }
        return new Deadline(description, TaskDate.parse(by));
    }

    /**
     * Builds an event from the text following the {@code event} command,
     * e.g. {@code project meeting /from 2019-10-15 /to 2019-10-16}.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if any of the description, {@code /from} or
     *                          {@code /to} parts is missing, a date cannot be
     *                          read, or the end falls before the start
     */
    public static Task parseEvent(String arguments) throws SallmanException {
        String[] fromParts = arguments.split("/from", 2);
        if (fromParts.length < 2) {
            throw new SallmanException("I couldn't find a /from in that event.",
                    EVENT_EXAMPLE);
        }
        String[] toParts = fromParts[1].split("/to", 2);
        if (toParts.length < 2) {
            throw new SallmanException("I couldn't find a /to in that event.", EVENT_EXAMPLE);
        }
        String description = fromParts[0].trim();
        String from = toParts[0].trim();
        String to = toParts[1].trim();
        if (description.isEmpty()) {
            throw new SallmanException("That event has no description before the /from.",
                    EVENT_EXAMPLE);
        }
        if (from.isEmpty()) {
            throw new SallmanException("That event has no start time after the /from.",
                    EVENT_EXAMPLE);
        }
        if (to.isEmpty()) {
            throw new SallmanException("That event has no end time after the /to.",
                    EVENT_EXAMPLE);
        }
        LocalDate start = TaskDate.parse(from);
        LocalDate end = TaskDate.parse(to);
        if (end.isBefore(start)) {
            // Such an event covers no days at all, so it could never be found
            // by the on command and is almost certainly a typo.
            throw new SallmanException("That event ends before it starts.",
                    "Check the order of the /from and /to dates.");
        }
        return new Event(description, start, end);
    }
}
