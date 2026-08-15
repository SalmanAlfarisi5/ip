import java.util.Scanner;

/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * At this stage the chatbot tracks todos, deadlines and events, lists them
 * back on request, and can mark them done, until the user enters {@code bye}.
 * An unknown command and a todo with no description are reported to the user;
 * other invalid input is not handled yet.
 */
public class Sallman {

    /** Name the chatbot introduces itself with. */
    private static final String NAME = "saLLMan";

    /** Maximum number of tasks the chatbot can hold, as allowed by the requirements. */
    private static final int MAX_TASKS = 100;

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
                if (input.equals("bye")) {
                    say("Bye. Hope to see you again soon!");
                    break;
                } else if (input.equals("list")) {
                    say(numberedTasks(tasks, taskCount));
                } else if (input.startsWith("mark ")) {
                    // Task numbers shown to the user start at 1, arrays start at 0.
                    int index = Integer.parseInt(input.substring("mark ".length()).trim()) - 1;
                    tasks[index].markAsDone();
                    say("Nice! I've marked this task as done:",
                            "  " + tasks[index]);
                } else if (input.startsWith("unmark ")) {
                    int index = Integer.parseInt(input.substring("unmark ".length()).trim()) - 1;
                    tasks[index].markAsNotDone();
                    say("OK, I've marked this task as not done yet:",
                            "  " + tasks[index]);
                } else if (input.equals("todo") || input.startsWith("todo ")) {
                    // Matching a bare "todo" too, so that a missing description
                    // is reported as such instead of looking like a typo.
                    String description = input.substring("todo".length()).trim();
                    if (description.isEmpty()) {
                        say("Oops! A todo needs a description.");
                        continue;
                    }
                    tasks[taskCount] = new Todo(description);
                    taskCount++;
                    announceAdded(tasks[taskCount - 1], taskCount);
                } else if (input.startsWith("deadline ")) {
                    // "return book /by Sunday" splits into description and due date.
                    String[] parts = input.substring("deadline ".length()).split(" /by ", 2);
                    tasks[taskCount] = new Deadline(parts[0].trim(), parts[1].trim());
                    taskCount++;
                    announceAdded(tasks[taskCount - 1], taskCount);
                } else if (input.startsWith("event ")) {
                    // "meeting /from Mon 2pm /to 4pm" splits into description, start, end.
                    String[] fromParts = input.substring("event ".length()).split(" /from ", 2);
                    String[] toParts = fromParts[1].split(" /to ", 2);
                    tasks[taskCount] = new Event(fromParts[0].trim(), toParts[0].trim(),
                            toParts[1].trim());
                    taskCount++;
                    announceAdded(tasks[taskCount - 1], taskCount);
                } else {
                    say("Oops! I don't know what that means.");
                }
            }
        }
    }
}
