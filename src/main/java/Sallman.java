/**
 * Entry point of the saLLMan chatbot.
 * <p>
 * At this stage the chatbot only greets the user and exits; it does not yet
 * read any input.
 */
public class Sallman {

    /** Name the chatbot introduces itself with. */
    private static final String NAME = "saLLMan";

    /** Horizontal rule that separates the chatbot's replies from the user's input. */
    private static final String DIVIDER =
            "____________________________________________________________";

    /**
     * ASCII-art banner spelling "saLLMan", shown once when the chatbot starts.
     * Each backslash in the art is escaped as {@code \\}, so the string looks
     * wider in the source than it does on screen.
     */
    private static final String BANNER =
              "              _      _      __  __\n"
            + " ___    __ _ | |    | |    |  \\/  |  __ _  _ __\n"
            + "/ __|  / _` || |    | |    | |\\/| | / _` || '_ \\\n"
            + "\\__ \\ | (_| || |___ | |___ | |  | || (_| || | | |\n"
            + "|___/  \\__,_||_____||_____||_|  |_| \\__,_||_| |_|";

    public static void main(String[] args) {
        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println("Hello! I'm " + NAME + ".");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
