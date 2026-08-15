# saLLMan UI test plan

Text-based tests for the chatbot's console interface. Each test case feeds its
`Input:` lines to the program on stdin and compares the whole console output
against `Expected output:` (with the shared session preamble prepended).

Run all cases from the repository root:

```bash
python .claude/skills/test-ui/scripts/run-ui-tests.py
```

Run a single case:

```bash
python .claude/skills/test-ui/scripts/run-ui-tests.py --filter "TC3"
```

Testing stops at the first failure and reports the expected and actual output.

## Session preamble

Every session opens with the banner and greeting, so this block is prepended to
every expected output rather than repeated in each test case.

```text
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

## Test cases

### TC1: Greeting and exit on an empty list

**Aim:** Verify the chatbot starts, reports an empty list without crashing, and
exits on `bye`.

**Input:**

```text
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC2: Add each task type and list them

**Aim:** Verify todo, deadline and event are each stored with the correct type
icon and date fields, and that the running total is reported.

**Input:**

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC3: Mark and unmark round trip

**Aim:** Verify `mark` sets the status icon to `X`, `unmark` clears it, and that
`list` reflects the change both times. Interleaving `list` after each command
checks the stored state, not just the confirmation message.

**Input:**

```text
todo read book
mark 1
list
unmark 1
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] read book
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC4: Free-text dates are accepted verbatim

**Aim:** Verify dates are still treated as plain strings, so unparseable text is
stored and echoed back unchanged.

**Input:**

```text
deadline do homework /by no idea :-p
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] do homework (by: no idea :-p)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] do homework (by: no idea :-p)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC5: Unrecognised input is rejected, not stored

**Aim:** Verify an unknown command is refused, and crucially that it is not
silently added to the list. The trailing `list` is what proves the internal
state stayed clean.

**Input:**

```text
blah
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Oops! I don't know what that means.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC6: A todo with no description is refused

**Aim:** Verify a bare `todo` is reported as a missing description rather than
as an unknown command, and that nothing is added. The following valid `todo`
checks that the rejected input left the list usable.

**Input:**

```text
todo
todo read book
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Oops! A todo needs a description.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC7: A todo whose description is only spaces is refused

**Aim:** Verify the description is checked after trimming, so whitespace alone
does not create a task with a blank description.

**Input:**

```text
todo    
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Oops! A todo needs a description.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Known gaps

These crash the program today and are expected to be covered when Level-5
adds error handling:

- `mark 99` / `mark 0` — index outside the list.
- `mark abc` — non-numeric task number.
- `deadline submit report` — missing the `/by` part.
- `event meeting /from Mon` — missing the `/to` part.
- Adding a 101st task — exceeds the fixed-size array.
