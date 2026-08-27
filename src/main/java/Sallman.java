import java.time.LocalDate;

/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * Holds the three parts the app is built from and runs the command loop over
 * them: a {@link Ui} for talking to the user, a {@link Storage} for the saved
 * task list, and a {@link TaskList} for the tasks themselves.
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

    /** Talks to the user. */
    private final Ui ui;

    /** Reads and writes the saved task list. */
    private final Storage storage;

    /** The tasks being tracked. */
    private final TaskList tasks;

    /**
     * Creates a chatbot backed by the given data file, loading whatever is
     * already saved there.
     * <p>
     * A file that cannot be read leaves the chatbot usable with an empty list,
     * rather than stopping it from starting at all.
     *
     * @param filePath where the task list is saved
     */
    public Sallman(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
        TaskList loaded;
        try {
            loaded = new TaskList(storage.load());
        } catch (SallmanException e) {
            ui.showError(e);
            loaded = new TaskList();
        }
        this.tasks = loaded;
    }

    /** Greets the user, then handles commands until they ask to stop. */
    public void run() {
        ui.showWelcome(NAME);
        ui.showSkippedLines(storage.getSkippedLines());

        boolean isExiting = false;
        while (!isExiting && ui.hasNextCommand()) {
            String input = ui.readCommand();
            if (input.isEmpty()) {
                continue; // a blank line is not worth complaining about
            }
            try {
                isExiting = execute(input);
            } catch (SallmanException e) {
                ui.showError(e);
            }
        }
        ui.close();
    }

    /**
     * Carries out one command.
     *
     * @param input the whole line the user typed, already trimmed
     * @return true if the chatbot should stop after this command
     * @throws SallmanException if the command cannot be carried out
     */
    private boolean execute(String input) throws SallmanException {
        // Everything up to the first space is the command, the rest are its
        // arguments. Separating them up front means a command given without
        // arguments still reaches its own branch, where the missing part can
        // be named.
        String[] words = input.split("\s+", 2);
        String keyword = words[0];
        String arguments = words.length > 1 ? words[1].trim() : "";

        // Rejects an unknown keyword before the switch, so every case below is
        // one of the known commands.
        Command command = Command.fromKeyword(keyword);
        boolean isListChanged = false;

        switch (command) {
        case BYE -> {
            ui.showGoodbye();
            return true;
        }
        case LIST -> ui.showTaskList(tasks);
        case ON -> {
            if (arguments.isEmpty()) {
                throw new SallmanException("on needs a date.", "Try: on " + TaskDate.EXAMPLE);
            }
            LocalDate date = TaskDate.parse(arguments);
            ui.showTasksOn(tasks.tasksOn(date), date);
        }
        case MARK, UNMARK -> {
            int index = parseTaskNumber(keyword, arguments, tasks.size());
            Task task = tasks.get(index);
            if (command == Command.MARK) {
                task.markAsDone();
                ui.showMarked(task);
            } else {
                task.markAsNotDone();
                ui.showUnmarked(task);
            }
            isListChanged = true;
        }
        case DELETE -> {
            int index = parseTaskNumber(keyword, arguments, tasks.size());
            Task removed = tasks.remove(index);
            ui.showRemoved(removed, tasks.size());
            isListChanged = true;
        }
        case TODO -> {
            addTask(parseTodo(arguments));
            isListChanged = true;
        }
        case DEADLINE -> {
            addTask(parseDeadline(arguments));
            isListChanged = true;
        }
        case EVENT -> {
            addTask(parseEvent(arguments));
            isListChanged = true;
        }
        }

        // Saving once here, rather than in each case above, means a new command
        // that changes the list only has to set the flag.
        if (isListChanged) {
            storage.save(tasks.asList());
        }
        return false;
    }

    /**
     * Adds a task to the list and confirms it to the user.
     *
     * @param task the task to add
     */
    private void addTask(Task task) {
        tasks.add(task);
        ui.showAdded(task, tasks.size());
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
     * Starts the chatbot.
     *
     * @param args optionally the data file path, for tests that must not touch
     *             the real task list
     */
    public static void main(String[] args) {
        new Sallman(args.length > 0 ? args[0] : DEFAULT_DATA_PATH).run();
    }
}
