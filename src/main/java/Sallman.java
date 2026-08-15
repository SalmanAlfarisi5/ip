import java.util.Scanner;

/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * At this stage the chatbot stores whatever the user types, lists it back on
 * request, and can mark a task as done, until the user enters {@code bye}.
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
     * Formats one task with its completion status, e.g. {@code [X] read book}.
     *
     * @param task   description of the task
     * @param isDone whether the task has been completed
     * @return the task description prefixed with its status icon
     */
    private static String formatTask(String task, boolean isDone) {
        return "[" + (isDone ? "X" : " ") + "] " + task;
    }

    /**
     * Formats the stored tasks as a numbered list with a heading, ready to be
     * passed to {@link #say(String...)}.
     *
     * @param tasks     array holding the task descriptions
     * @param isDone    completion status of each task, parallel to {@code tasks}
     * @param taskCount number of filled slots at the front of the arrays
     * @return a heading followed by one line per task, numbered from 1
     */
    private static String[] numberedTasks(String[] tasks, boolean[] isDone, int taskCount) {
        String[] lines = new String[taskCount + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < taskCount; i++) {
            lines[i + 1] = (i + 1) + "." + formatTask(tasks[i], isDone[i]);
        }
        return lines;
    }

    public static void main(String[] args) {
        System.out.println(BANNER);
        say("Hello! I'm " + NAME + ", freshly loaded and ready to assist.",
                "What are we working on today?");

        String[] tasks = new String[MAX_TASKS];
        boolean[] isDone = new boolean[MAX_TASKS]; // isDone[i] tracks tasks[i]
        int taskCount = 0; // number of slots filled, so also the index of the next free slot

        try (Scanner in = new Scanner(System.in)) {
            // hasNextLine() also stops the loop if input ends without a "bye".
            while (in.hasNextLine()) {
                String input = in.nextLine().trim();
                if (input.equals("bye")) {
                    say("Bye. Hope to see you again soon!");
                    break;
                } else if (input.equals("list")) {
                    say(numberedTasks(tasks, isDone, taskCount));
                } else if (input.startsWith("mark ")) {
                    // Task numbers shown to the user start at 1, arrays start at 0.
                    int index = Integer.parseInt(input.substring("mark ".length()).trim()) - 1;
                    isDone[index] = true;
                    say("Nice! I've marked this task as done:",
                            "  " + formatTask(tasks[index], true));
                } else if (input.startsWith("unmark ")) {
                    int index = Integer.parseInt(input.substring("unmark ".length()).trim()) - 1;
                    isDone[index] = false;
                    say("OK, I've marked this task as not done yet:",
                            "  " + formatTask(tasks[index], false));
                } else {
                    tasks[taskCount] = input;
                    taskCount++;
                    say("added: " + input);
                }
            }
        }
    }
}
