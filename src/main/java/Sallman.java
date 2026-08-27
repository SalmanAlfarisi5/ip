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
        String[] parts = Parser.splitCommand(input);
        String keyword = parts[0];
        String arguments = parts[1];

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
            LocalDate date = Parser.parseOnDate(arguments);
            ui.showTasksOn(tasks.tasksOn(date), date);
        }
        case MARK, UNMARK -> {
            int index = Parser.parseTaskNumber(keyword, arguments, tasks.size());
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
            int index = Parser.parseTaskNumber(keyword, arguments, tasks.size());
            Task removed = tasks.remove(index);
            ui.showRemoved(removed, tasks.size());
            isListChanged = true;
        }
        case TODO -> {
            addTask(Parser.parseTodo(arguments));
            isListChanged = true;
        }
        case DEADLINE -> {
            addTask(Parser.parseDeadline(arguments));
            isListChanged = true;
        }
        case EVENT -> {
            addTask(Parser.parseEvent(arguments));
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
     * Starts the chatbot.
     *
     * @param args optionally the data file path, for tests that must not touch
     *             the real task list
     */
    public static void main(String[] args) {
        new Sallman(args.length > 0 ? args[0] : DEFAULT_DATA_PATH).run();
    }
}
