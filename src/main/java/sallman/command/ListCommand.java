package sallman.command;

import sallman.Storage;
import sallman.TaskList;
import sallman.Ui;

/**
 * Shows every task in the list.
 */
public class ListCommand extends Command {

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskList(tasks);
    }
}
