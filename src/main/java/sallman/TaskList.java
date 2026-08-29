package sallman;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import sallman.task.Task;


/**
 * The tasks the user is tracking, with the operations that act on the list as
 * a whole.
 * <p>
 * Task numbers shown to the user start at 1, but every index taken or returned
 * here starts at 0. Converting between the two is the caller's job, so that
 * this class deals only in list positions.
 */
public class TaskList {

    /** The tasks, in the order the user added them. */
    private final List<Task> tasks;

    /** Creates an empty list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a list holding the given tasks, e.g. those just loaded from disk.
     *
     * @param tasks the tasks to start with
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes the task at the given position.
     *
     * @param index position of the task, counting from 0
     * @return the task that was removed
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the given position.
     *
     * @param index position of the task, counting from 0
     * @return the task there
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns how many tasks are in the list.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the tasks falling on the given date.
     * <p>
     * Each task decides for itself whether it falls on that date, so this does
     * not need to know which kinds of task carry dates.
     *
     * @param date the date being asked about
     * @return the matching tasks, in list order, possibly empty
     */
    public List<Task> tasksOn(LocalDate date) {
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.isOn(date)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Returns the tasks whose description contains the given keyword.
     * <p>
     * Each task decides for itself whether it matches, so this does not need
     * to know how a description is stored.
     *
     * @param keyword the text being searched for
     * @return the matching tasks, in list order, possibly empty
     */
    public List<Task> find(String keyword) {
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.hasKeyword(keyword)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Returns the tasks as a plain list, for saving.
     *
     * @return a copy of the tasks, so callers cannot change the list through it
     */
    public List<Task> asList() {
        return new ArrayList<>(tasks);
    }
}
