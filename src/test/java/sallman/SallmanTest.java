package sallman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests the part of {@link Sallman} a GUI talks to: one reply per input, and
 * enough information about each reply to decide how to show it.
 */
public class SallmanTest {

    @TempDir
    private Path folder;

    private Sallman sallman;

    @BeforeEach
    public void setUp() {
        sallman = new Sallman(folder.resolve("tasks.txt").toString(), false);
    }

    @Test
    public void getResponse_validCommand_notAnError() {
        String reply = sallman.getResponse("todo read book");

        assertTrue(reply.contains("read book"));
        assertFalse(sallman.isLastResponseError());
        assertEquals("AddCommand", sallman.getLastCommandType());
    }

    @Test
    public void getResponse_invalidCommand_reportedAsAnError() {
        sallman.getResponse("blah");

        assertTrue(sallman.isLastResponseError());
        assertEquals("", sallman.getLastCommandType());
    }

    @Test
    public void getResponse_validCommandAfterAnError_errorFlagCleared() {
        // The flag describes the latest reply only, so an earlier error must not
        // make the next, successful reply look like a problem too.
        sallman.getResponse("blah");
        sallman.getResponse("list");

        assertFalse(sallman.isLastResponseError());
    }

    @Test
    public void getResponse_blankInput_emptyReplyAndNoExit() {
        assertEquals("", sallman.getResponse("   "));
        assertFalse(sallman.isExitRequested());
    }

    @Test
    public void getResponse_bye_asksToExit() {
        sallman.getResponse("bye");

        assertTrue(sallman.isExitRequested());
    }
}
