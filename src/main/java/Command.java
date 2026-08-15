/**
 * The commands the chatbot understands, each paired with the keyword the user
 * types for it.
 * <p>
 * Having a fixed set of named constants lets the compiler check that a command
 * is one of the known ones, which a bare {@code String} could not do. It also
 * keeps the list of keywords in one place: {@link #keywords()} builds the help
 * text from these constants, so a new command cannot be added without the help
 * text following it.
 */
public enum Command {
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    BYE("bye");

    /** What the user types to invoke this command. */
    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the keyword the user types for this command, for use in messages
     * that should name the command the way the user wrote it.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Finds the command matching a keyword typed by the user.
     *
     * @param keyword first word of the user's input
     * @return the matching command
     * @throws SallmanException if no command uses that keyword
     */
    public static Command fromKeyword(String keyword) throws SallmanException {
        for (Command command : values()) {
            if (command.keyword.equals(keyword)) {
                return command;
            }
        }
        throw new SallmanException("Sorry, I don't know what \"" + keyword + "\" means.",
                "I understand: " + keywords() + ".");
    }

    /**
     * Lists every keyword in declaration order, for use in help and error text.
     *
     * @return the keywords separated by commas, e.g. {@code todo, deadline, ...}
     */
    public static String keywords() {
        StringBuilder list = new StringBuilder();
        for (Command command : values()) {
            if (list.length() > 0) {
                list.append(", ");
            }
            list.append(command.keyword);
        }
        return list.toString();
    }
}
