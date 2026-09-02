# saLLMan

saLLMan is a command-line chatbot that helps you keep track of your tasks:
todos, deadlines and events. Your list is saved to disk automatically and
loaded again the next time you start it.

## Setting up in IntelliJ

Prerequisites: JDK 25, and a recent version of IntelliJ.

1. Open IntelliJ (if you are not in the welcome screen, click `File` >
   `Close Project` to close the existing project first).
1. Open the project into IntelliJ as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained
   [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the
   `SDK default` option.
1. This project is built with Gradle, so IntelliJ should import it as a Gradle
   project and show a Gradle toolbar with an elephant icon. In
   `File` > `Settings`, set **Gradle JVM** to `Project SDK`.<br>
   If the Gradle toolbar does not appear, close the project, delete the
   `.idea` folder, and open it again so IntelliJ re-imports it.
1. Run the `run` task from the Gradle toolbar, or `./gradlew run` in a
   terminal. If the setup is correct, you should see something like this:
   ```
                 _      _      __  __
    ___    __ _ | |    | |    |  \/  |  __ _  _ __
   / __|  / _` || |    | |    | |\/| | / _` || '_ \
   \__ \ | (_| || |___ | |___ | |  | || (_| || | | |
   |___/  \__,_||_____||_____||_|  |_| \__,_||_| |_|
       ____________________________________________________________
        Hello! I'm saLLMan, freshly loaded and ready to assist.
        What are we working on today?
       ____________________________________________________________
   ```

**Warning:** Keep the `src/main/java` folder as the root folder for Java files
(i.e., don't rename those folders or move Java files outside that path), as
this is where Gradle expects to find them.

## Building and running with Gradle

Use `gradlew` on Windows and `./gradlew` on macOS and Linux. The wrapper
downloads the right Gradle version on first use, so Gradle does not need to be
installed separately.

| Command | What it does |
|---|---|
| `./gradlew run` | Runs the chatbot's GUI |
| `./gradlew build` | Compiles, tests, and assembles the project |
| `./gradlew test` | Runs the JUnit tests |
| `./gradlew clean` | Deletes the build directory |
| `./gradlew clean shadowJar` | Builds a runnable JAR at `build/libs/sallman.jar` |

Assertions are enabled for `run`, since Java otherwise ignores them.

The text-based interface is still there alongside the GUI, and is what the
text-UI tests drive:

```
java src/main/java/sallman/Sallman.java
```

## Commands

| Command | Example |
|---|---|
| Add a todo | `todo read book` |
| Add a deadline | `deadline return book /by 2019-10-15` |
| Add an event | `event conference /from 2019-10-14 /to 2019-10-17` |
| List everything | `list` |
| List one day | `on 2019-10-15` |
| Mark done / not done | `mark 2`, `unmark 2` |
| Delete | `delete 2` |
| Exit | `bye` |

Dates are entered as `yyyy-mm-dd` and shown back as `MMM dd yyyy`.

## Where your tasks are saved

Tasks are written to `data/sallman.txt`, relative to the folder the chatbot is
run from. The file is plain text, one task per line, so it can be read and
edited by hand. A line that cannot be understood is reported and skipped
rather than discarding the rest of the list.

To use a different file, pass it as an argument:

```
./gradlew run --args="path/to/tasks.txt"
```

## Testing

JUnit tests belong in `src/test/java` and run with `./gradlew test`. The
build already declares JUnit 5, so adding a test file there is enough for
Gradle to pick it up.

There is also a text-UI test plan in [`test/ui-test-plan.md`](test/ui-test-plan.md),
which feeds commands to the chatbot and checks the console output against the
expected transcript.
