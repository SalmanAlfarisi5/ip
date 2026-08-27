import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * At this stage the chatbot tracks todos, deadlines and events, lists them
 * back on request, and can mark them done or delete them, until the user
 * enters {@code bye}. Tasks carry real dates, so the list can also be
 * filtered to whatever falls on a given day. The list is loaded from disk
 * at startup and saved again whenever it changes.
 * Invalid input is reported to the user rather than allowed to crash the
 * program.
 */
public class Sallman {

    /** Name the chatbot introduces itself with. */
    private static final String NAME = "saLLMan";

    /**
     * Where the task list is saved when no other path is given on the command
     * line. Relative to the folder the app is run from, so it works on any
     * computer.
     */
    private static final String DEFAULT_DATA_PATH = "data/sallman.txt";

    /**
     * Worked examples shown alongside an error, so the user can see the shape
     * of a correct command instead of only being told what was wrong.
     */
    private static final String TODO_EXAMPLE = "Try: todo read book";
    private static final String DEADLINE_EXAMPLE =
            "Try: deadline return book /by " + TaskDate.EXAMPLE;
    private static final String EVENT_EXAMPLE =
            "Try: event project meeting /from " + TaskDate.EXAMPLE + " /to 2019-10-16";

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
        say("Got it. I've added this task:", "  " + task, taskCountSummary(taskCount));
    }

    /**
     * Reports how many tasks are in the list, shown after adding or removing one.
     *
     * @param taskCount number of tasks now in the list
     * @return a sentence naming the total, with "task" pluralised to match
     */
    private static String taskCountSummary(int taskCount) {
        return "Now you have " + taskCount + (taskCount == 1 ? " task" : " tasks")
                + " in the list.";
    }

    /**
     * Adds a task to the list and confirms it to the user.
     *
     * @param tasks the list to add to
     * @param task  the task to add
     */
    private static void addTask(TaskList tasks, Task task) {
        tasks.add(task);
        announceAdded(task, tasks.size());
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
     * Formats the tasks falling on one date as a numbered list with a heading.
     *
     * @param tasks the tasks to search
     * @param date  the date being asked about
     * @return a heading followed by the matching tasks, or a single line
     *         saying nothing falls on that date
     */
    private static String[] tasksOnDate(TaskList tasks, LocalDate date) {
        List<Task> matches = tasks.tasksOn(date);
        if (matches.isEmpty()) {
            return new String[] {"Nothing on " + TaskDate.format(date) + "."};
        }
        String[] lines = new String[matches.size() + 1];
        lines[0] = "Here is what you have on " + TaskDate.format(date) + ":";
        for (int i = 0; i < matches.size(); i++) {
            lines[i + 1] = (i + 1) + "." + matches.get(i);
        }
        return lines;
    }

    /**
     * Formats the stored tasks as a numbered list with a heading, ready to be
     * passed to {@link #say(String...)}.
     *
     * @param tasks the tasks to list
     * @return a heading followed by one line per task, numbered from 1
     */
    private static String[] numberedTasks(TaskList tasks) {
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
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
     * e.g. {@code return book /by 2019-10-15}.
     *
     * @param arguments text the user typed after the command
     * @return the new task
     * @throws SallmanException if the {@code /by}, the description, or the due
     *                          date is missing, or the date cannot be read
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

    /**
     * Warns the user about any saved lines that could not be read, naming each
     * one so a hand-edited data file can be corrected.
     *
     * @param skippedLines descriptions of the skipped lines, possibly empty
     */
    private static void reportSkippedLines(List<String> skippedLines) {
        if (skippedLines.isEmpty()) {
            return;
        }
        List<String> reply = new ArrayList<>();
        reply.add("I skipped " + skippedLines.size()
                + (skippedLines.size() == 1 ? " unreadable line" : " unreadable lines")
                + " in your saved data:");
        reply.addAll(skippedLines);
        reply.add("Everything else loaded fine. The bad lines will be dropped");
        reply.add("the next time your list changes.");
        say(reply.toArray(new String[0]));
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        say("Hello! I'm " + NAME + ", freshly loaded and ready to assist.",
                "What are we working on today?");

        // Accepting the path as an argument lets the tests use a scratch file
        // instead of the real task list.
        Storage storage = new Storage(args.length > 0 ? args[0] : DEFAULT_DATA_PATH);

        // An ArrayList grows as needed, so there is no task limit to enforce
        // and no separate count to keep in step with the contents.
        TaskList tasks;
        try {
            tasks = new TaskList(storage.load());
        } catch (SallmanException e) {
            say(e.toLines());
            tasks = new TaskList();
        }
        reportSkippedLines(storage.getSkippedLines());

        // A "break" inside a switch leaves the switch, not the loop, so the
        // bye command records its intent here instead.
        boolean isExiting = false;

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
                String keyword = words[0];
                String arguments = words.length > 1 ? words[1].trim() : "";

                // Each branch below either carries out the command or throws,
                // so the happy path reads without the error cases in the way.
                try {
                    // Rejects an unknown keyword before the switch, so every
                    // case below is one of the known commands.
                    Command command = Command.fromKeyword(keyword);
                    boolean isListChanged = false;

                    switch (command) {
                    case BYE -> {
                        say("Bye. Hope to see you again soon!");
                        isExiting = true;
                    }
                    case LIST -> say(numberedTasks(tasks));
                    case ON -> {
                        if (arguments.isEmpty()) {
                            throw new SallmanException("on needs a date.",
                                    "Try: on " + TaskDate.EXAMPLE);
                        }
                        say(tasksOnDate(tasks, TaskDate.parse(arguments)));
                    }
                    case MARK, UNMARK -> {
                        int index = parseTaskNumber(keyword, arguments, tasks.size());
                        Task task = tasks.get(index);
                        if (command == Command.MARK) {
                            task.markAsDone();
                            say("Nice! I've marked this task as done:", "  " + task);
                        } else {
                            task.markAsNotDone();
                            say("OK, I've marked this task as not done yet:", "  " + task);
                        }
                        isListChanged = true;
                    }
                    case DELETE -> {
                        int index = parseTaskNumber(keyword, arguments, tasks.size());
                        // remove() closes the gap itself and returns what it took out.
                        Task removed = tasks.remove(index);
                        say("Noted. I've removed this task:", "  " + removed,
                                taskCountSummary(tasks.size()));
                        isListChanged = true;
                    }
                    case TODO -> {
                        addTask(tasks, parseTodo(arguments));
                        isListChanged = true;
                    }
                    case DEADLINE -> {
                        addTask(tasks, parseDeadline(arguments));
                        isListChanged = true;
                    }
                    case EVENT -> {
                        addTask(tasks, parseEvent(arguments));
                        isListChanged = true;
                    }
                    }

                    // Saving once here, rather than in each case above, means a
                    // new command that changes the list only has to set the flag.
                    if (isListChanged) {
                        storage.save(tasks.asList());
                    }
                } catch (SallmanException e) {
                    say(e.toLines());
                }

                if (isExiting) {
                    break;
                }
            }
        }
    }
}
