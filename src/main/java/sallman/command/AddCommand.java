package sallman.command;

import sallman.SallmanException;
import sallman.Storage;
import sallman.TaskList;
import sallman.Ui;
import sallman.task.Task;

/**
 * Adds one task to the list.
 * <p>
 * Todos, deadlines and events differ only in how they are built, which the
 * parser has already done by this point, so one command covers all three.
 */
public class AddCommand extends Command {

    /** The task to add, already built and validated. */
    private final Task task;

    /**
     * Creates a command that adds the given task.
     *
     * @param task the task to add
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws SallmanException {
        tasks.add(task);
        ui.showAdded(task, tasks.size());
        storage.save(tasks.asList());
    }
}
