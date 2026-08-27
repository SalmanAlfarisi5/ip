# saLLMan

saLLMan is a command-line chatbot that helps you keep track of your tasks. Given below are instructions on how to set it up.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/sallman/Sallman.java` file, right-click it, and choose `Run Sallman.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
   ```
   ____________________________________________________________
                 _      _      __  __
    ___    __ _ | |    | |    |  \/  |  __ _  _ __
   / __|  / _` || |    | |    | |\/| | / _` || '_ \
   \__ \ | (_| || |___ | |___ | |  | || (_| || | | |
   |___/  \__,_||_____||_____||_|  |_| \__,_||_| |_|
   Hello! I'm saLLMan.
   What can I do for you?
   ____________________________________________________________
   Bye. Hope to see you again soon!
   ____________________________________________________________
   ```

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.
