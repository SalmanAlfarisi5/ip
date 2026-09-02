package sallman;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import sallman.task.Task;
import sallman.task.TaskDate;


/**
 * Everything the user sees and types.
 * <p>
 * Keeping the wording here means the rest of the app decides what happened,
 * not how to phrase it, and the console is the only thing that has to change
 * if the chatbot is later given a different kind of interface.
 */
public class Ui {

    /** Horizontal rule that separates the chatbot's replies from the user's input. */
    private static final String DIVIDER =
            "____________________________________________________________";

    /**
     * ASCII-art banner spelling "saLLMan", shown once when the chatbot starts.
     * Each backslash in the art is escaped as {@code \}, so the string looks
     * wider in the source than it does on screen.
     */
    private static final String BANNER =
              "              _      _      __  __\n"
            + " ___    __ _ | |    | |    |  \\/  |  __ _  _ __\n"
            + "/ __|  / _` || |    | |    | |\\/| | / _` || '_ \\\n"
            + "\\__ \\ | (_| || |___ | |___ | |  | || (_| || | | |\n"
            + "|___/  \\__,_||_____||_____||_|  |_| \\__,_||_| |_|";

    /** Reads the user's commands from the console. */
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Whether replies are printed to the console as well as collected.
     * <p>
     * The console front end prints as it goes; the GUI collects instead and
     * asks for the text when the command is finished.
     */
    private final boolean isPrintingToConsole;

    /** Replies said since the last {@link #drainText()}, without decoration. */
    private final StringBuilder saidText = new StringBuilder();

    /** Creates a user interface that prints to the console. */
    public Ui() {
        this(true);
    }

    /**
     * Creates a user interface, optionally silent.
     *
     * @param isPrintingToConsole false to collect replies without printing them
     */
    public Ui(boolean isPrintingToConsole) {
        this.isPrintingToConsole = isPrintingToConsole;
    }

    /**
     * Returns everything said since this was last called, and forgets it.
     * <p>
     * The text has no dividers or indentation: those belong to the console,
     * whereas a GUI puts each reply in a box of its own.
     *
     * @return the replies since the last call, one line each
     */
    public String drainText() {
        String text = saidText.toString().strip();
        saidText.setLength(0);
        return text;
    }

    /**
     * Prints the given lines as one chatbot reply, wrapped between horizontal
     * rules so replies stand out from what the user typed.
     *
     * @param lines lines of the reply, printed in order
     */
    public void say(String... lines) {
        if (isPrintingToConsole) {
            System.out.println("    " + DIVIDER);
            for (String line : lines) {
                System.out.println("     " + line);
            }
            System.out.println("    " + DIVIDER);
            System.out.println();
        }
        for (String line : lines) {
            saidText.append(line).append(System.lineSeparator());
        }
    }

    /**
     * Shows the banner and greeting.
     *
     * @param name the name the chatbot introduces itself with
     */
    public void showWelcome(String name) {
        if (isPrintingToConsole) {
            System.out.println(BANNER);
        }
        say("Hello! I'm " + name + ", freshly loaded and ready to assist.",
                "What are we working on today?");
    }

    /** Shows the parting message. */
    public void showGoodbye() {
        say("Bye. Hope to see you again soon!");
    }

    /**
     * Reports a problem with what the user asked for.
     *
     * @param e the problem, carrying its explanation and any suggested fix
     */
    public void showError(SallmanException e) {
        say(e.toLines());
    }

    /**
     * Warns about saved lines that could not be read, naming each one so a
     * hand-edited data file can be corrected.
     *
     * @param skippedLines descriptions of the skipped lines, possibly empty
     */
    public void showSkippedLines(List<String> skippedLines) {
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

    /**
     * Confirms that a task was added, and reports the new total.
     *
     * @param task      the task just added
     * @param taskCount number of tasks now in the list
     */
    public void showAdded(Task task, int taskCount) {
        say("Got it. I've added this task:", "  " + task, taskCountSummary(taskCount));
    }

    /**
     * Confirms that a task was removed, and reports the new total.
     *
     * @param task      the task just removed
     * @param taskCount number of tasks left in the list
     */
    public void showRemoved(Task task, int taskCount) {
        say("Noted. I've removed this task:", "  " + task, taskCountSummary(taskCount));
    }

    /**
     * Confirms that a task was marked done.
     *
     * @param task the task, already updated
     */
    public void showMarked(Task task) {
        say("Nice! I've marked this task as done:", "  " + task);
    }

    /**
     * Confirms that a task was marked not done.
     *
     * @param task the task, already updated
     */
    public void showUnmarked(Task task) {
        say("OK, I've marked this task as not done yet:", "  " + task);
    }

    /**
     * Shows the whole task list, numbered from 1.
     *
     * @param tasks the tasks to show
     */
    public void showTaskList(TaskList tasks) {
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        say(lines);
    }

    /**
     * Shows the tasks matching a search, or says there were none.
     *
     * @param matches the matching tasks, possibly empty
     * @param keyword the text that was searched for
     */
    public void showMatchingTasks(List<Task> matches, String keyword) {
        if (matches.isEmpty()) {
            say("No tasks match \"" + keyword + "\".");
            return;
        }
        String[] lines = new String[matches.size() + 1];
        lines[0] = "Here are the matching tasks in your list:";
        for (int i = 0; i < matches.size(); i++) {
            lines[i + 1] = (i + 1) + "." + matches.get(i);
        }
        say(lines);
    }

    /**
     * Shows the tasks falling on one date, or says there are none.
     *
     * @param matches the tasks on that date, possibly empty
     * @param date    the date being asked about
     */
    public void showTasksOn(List<Task> matches, LocalDate date) {
        if (matches.isEmpty()) {
            say("Nothing on " + TaskDate.format(date) + ".");
            return;
        }
        String[] lines = new String[matches.size() + 1];
        lines[0] = "Here is what you have on " + TaskDate.format(date) + ":";
        for (int i = 0; i < matches.size(); i++) {
            lines[i + 1] = (i + 1) + "." + matches.get(i);
        }
        say(lines);
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
     * Returns whether there is another command to read.
     *
     * @return true while input remains, false once it ends
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command, without surrounding spaces.
     *
     * @return what the user typed
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Releases the console input. */
    public void close() {
        scanner.close();
    }
}
