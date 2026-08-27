/**
 * Removes one task from the list.
 */
public class DeleteCommand extends Command {

    /** The task number the user gave, checked when the command runs. */
    private final String arguments;

    /**
     * Creates a command that deletes the task the given argument names.
     *
     * @param arguments text the user typed after the command
     */
    public DeleteCommand(String arguments) {
        this.arguments = arguments;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws SallmanException {
        // Checked here rather than while parsing, because whether a number is
        // in range depends on the list, which the parser does not see.
        int index = Parser.parseTaskNumber(CommandType.DELETE.getKeyword(), arguments,
                tasks.size());
        Task removed = tasks.remove(index);
        ui.showRemoved(removed, tasks.size());
        storage.save(tasks.asList());
    }
}
