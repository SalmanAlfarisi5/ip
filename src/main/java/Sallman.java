/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * Holds the three parts the app is built from and runs the command loop over
 * them: a {@link Ui} for talking to the user, a {@link Storage} for the saved
 * task list, and a {@link TaskList} for the tasks themselves.
 * <p>
 * The loop itself knows nothing about individual commands: it asks
 * {@link Parser} for a {@link Command} and lets that command carry itself out.
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

        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            String fullCommand = ui.readCommand();
            if (fullCommand.isEmpty()) {
                continue; // a blank line is not worth complaining about
            }
            try {
                Command command = Parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (SallmanException e) {
                ui.showError(e);
            }
        }
        ui.close();
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
