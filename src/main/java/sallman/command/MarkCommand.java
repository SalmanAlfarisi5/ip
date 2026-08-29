package sallman.command;

import sallman.Parser;
import sallman.SallmanException;
import sallman.Storage;
import sallman.TaskList;
import sallman.Ui;
import sallman.task.Task;

/**
 * Changes whether one task is done.
 * <p>
 * Marking and unmarking differ only in the value they set and the wording of
 * the reply, so one command covers both.
 */
public class MarkCommand extends Command {

    /** Whether this marks the task done, rather than not done. */
    private final boolean isMarkingDone;

    /** The task number the user gave, checked when the command runs. */
    private final String arguments;

    /**
     * Creates a command that sets the done status of the task named.
     *
     * @param isMarkingDone true to mark done, false to mark not done
     * @param arguments     text the user typed after the command
     */
    public MarkCommand(boolean isMarkingDone, String arguments) {
        this.isMarkingDone = isMarkingDone;
        this.arguments = arguments;
    }

    /**
     * Sets the done status of the numbered task, reports it, and saves the list.
     *
     * @param tasks   the list holding the task
     * @param ui      used to confirm the change
     * @param storage used to save the changed list
     * @throws SallmanException if the number is missing, unreadable or outside
     *                          the list, or if the list cannot be saved
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws SallmanException {
        // The keyword is quoted back in the error messages, so it has to match
        // whichever of the two commands the user actually typed.
        String keyword = isMarkingDone
                ? CommandType.MARK.getKeyword()
                : CommandType.UNMARK.getKeyword();
        int index = Parser.parseTaskNumber(keyword, arguments, tasks.size());
        Task task = tasks.get(index);
        if (isMarkingDone) {
            task.markAsDone();
            ui.showMarked(task);
        } else {
            task.markAsNotDone();
            ui.showUnmarked(task);
        }
        storage.save(tasks.asList());
    }
}
