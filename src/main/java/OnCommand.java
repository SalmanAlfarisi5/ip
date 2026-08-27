import java.time.LocalDate;

/**
 * Shows the tasks falling on one date.
 */
public class OnCommand extends Command {

    /** The date being asked about. */
    private final LocalDate date;

    /**
     * Creates a command that lists what falls on the given date.
     *
     * @param date the date being asked about
     */
    public OnCommand(LocalDate date) {
        this.date = date;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOn(tasks.tasksOn(date), date);
    }
}
