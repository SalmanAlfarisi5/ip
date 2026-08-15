import java.util.Scanner;

/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * At this stage the chatbot tracks todos, deadlines and events, lists them
 * back on request, and can mark them done, until the user enters {@code bye}.
 * Invalid input is reported to the user rather than allowed to crash the
 * program.
 */
public class Sallman {

    /** Name the chatbot introduces itself with. */
    private static final String NAME = "saLLMan";

    /** Maximum number of tasks the chatbot can hold, as allowed by the requirements. */
    private static final int MAX_TASKS = 100;

    /**
     * Worked examples shown alongside an error, so the user can see the shape
     * of a correct command instead of only being told what was wrong.
     */
    private static final String TODO_EXAMPLE = "Try: todo read book";
    private static final String DEADLINE_EXAMPLE = "Try: deadline return book /by Sunday";
    private static final String EVENT_EXAMPLE =
            "Try: event project meeting /from Mon 2pm /to 4pm";

    /** Horizontal rule that separates the chatbot's replies from the user's input. */
    private static final String DIVIDER =
            "____________________________________________________________";

    /**
     * ASCII-art banner spelling "saLLMan", shown once when the chatbot starts.
     * Each backslash in the art is escaped as {@code \\}, so the string looks
     * wider in the source than it does on screen.
     */
    private static final String BANNER =
              "              _      _      __  __\n"
            + " ___    __ _ | |    | |    |  \\/  |  __ _  _ __\n"
            + "/ __|  / _` || |    | |    | |\\/| | / _` || '_ \\\n"
            + "\\__ \\ | (_| || |___ | |___ | |  | || (_| || | | |\n"
            + "|___/  \\__,_||_____||_____||_|  |_| \\__,_||_| |_|";

    /**
     * Prints the given lines as one chatbot reply, wrapped between horizontal
     * rules so replies stand out from what the user typed.
     *
     * @param lines lines of the reply, printed in order
     */
    private static void say(String... lines) {
        System.out.println("    " + DIVIDER);
        for (String line : lines) {
            System.out.println("     " + line);
        }
        System.out.println("    " + DIVIDER);
        System.out.println();
    }

    /**
     * Confirms to the user that a task was added, and reports the new total.
     *
     * @param task      the task just added
     * @param taskCount number of tasks now in the list
     */
    private static void announceAdded(Task task, int taskCount) {
        say("Got it. I've added this task:",
                "  " + task,
                "Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks")
                        + " in the list.");
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
    private static int parseTaskNumber(String command, String arguments, int taskCount)
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
     * Formats the stored tasks as a numbered list with a heading, ready to be
     * passed to {@link #say(String...)}.
     *
     * @param tasks     array holding the tasks
     * @param taskCount number of filled slots at the front of {@code tasks}
     * @return a heading followed by one line per task, numbered from 1
     */
    private static String[] numberedTasks(Task[] tasks, int taskCount) {
        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            lines[i + 1] = (i + 1) + "." + tasks[i];
        }
        return lines;
    }

    /**
     * Builds a todo from the text following the {@code todo} command.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if the description is missing
     */
    private static Task parseTodo(String arguments) throws SallmanException {
        if (arguments.isEmpty()) {
            throw new SallmanException("A todo needs a description.", TODO_EXAMPLE);
        }
        return new Todo(arguments);
    }

    /**
     * Builds a deadline from the text following the {@code deadline} command,
     * e.g. {@code return book /by Sunday}.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if the {@code /by}, the description, or the due
     *                          date is missing
     */
    private static Task parseDeadline(String arguments) throws SallmanException {
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
        return new Deadline(description, by);
    }

    /**
     * Builds an event from the text following the {@code event} command,
     * e.g. {@code project meeting /from Mon 2pm /to 4pm}.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if any of the description, {@code /from} or
     *                          {@code /to} parts is missing
     */
    private static Task parseEvent(String arguments) throws SallmanException {
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
        return new Event(description, from, to);
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        say("Hello! I'm " + NAME + ", freshly loaded and ready to assist.",
                "What are we working on today?");

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0; // number of slots filled, so also the index of the next free slot

        try (Scanner in = new Scanner(System.in)) {
            // hasNextLine() also stops the loop if input ends without a "bye".
            while (in.hasNextLine()) {
                String input = in.nextLine().trim();
                if (input.isEmpty()) {
                    continue; // a blank line is not worth complaining about
                }

                // Everything up to the first space is the command, the rest are
                // its arguments. Separating them up front means a command given
                // without arguments still reaches its own branch, where the
                // missing part can be named.
                String[] words = input.split("\\s+", 2);
                String command = words[0];
                String arguments = words.length > 1 ? words[1].trim() : "";

                // Each branch below either carries out the command or throws,
                // so the happy path reads without the error cases in the way.
                try {
                    boolean isAddCommand = command.equals("todo")
                            || command.equals("deadline")
                            || command.equals("event");
                    if (isAddCommand && taskCount == MAX_TASKS) {
                        throw new SallmanException("Your list is full at " + MAX_TASKS
                                + " tasks.",
                                "I can't take any more until some come off the list.");
                    }

                    if (command.equals("bye")) {
                        say("Bye. Hope to see you again soon!");
                        break;
                    } else if (command.equals("list")) {
                        say(numberedTasks(tasks, taskCount));
                    } else if (command.equals("mark") || command.equals("unmark")) {
                        int index = parseTaskNumber(command, arguments, taskCount);
                        if (command.equals("mark")) {
                            tasks[index].markAsDone();
                            say("Nice! I've marked this task as done:", "  " + tasks[index]);
                        } else {
                            tasks[index].markAsNotDone();
                            say("OK, I've marked this task as not done yet:",
                                    "  " + tasks[index]);
                        }
                    } else if (isAddCommand) {
                        Task task;
                        if (command.equals("todo")) {
                            task = parseTodo(arguments);
                        } else if (command.equals("deadline")) {
                            task = parseDeadline(arguments);
                        } else {
                            task = parseEvent(arguments);
                        }
                        tasks[taskCount] = task;
                        taskCount++;
                        announceAdded(task, taskCount);
                    } else {
                        throw new SallmanException(
                                "Sorry, I don't know what \"" + command + "\" means.",
                                "I understand: todo, deadline, event, list, mark, "
                                        + "unmark, bye.");
                    }
                } catch (SallmanException e) {
                    say(e.toLines());
                }
            }
        }
    }
}
