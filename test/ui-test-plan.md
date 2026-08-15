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

### TC8: Bad task numbers are refused

**Aim:** Verify every way of getting a task number wrong is reported rather
than crashing: out of range above and below, non-numeric, and missing. The
closing `list` confirms the real task survived all of it untouched.

**Input:**

```text
todo read book
mark 99
mark 0
mark abc
mark
unmark
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
     Oops! There is no task with that number.
    ____________________________________________________________

    ____________________________________________________________
     Oops! There is no task with that number.
    ____________________________________________________________

    ____________________________________________________________
     Oops! That is not a task number.
    ____________________________________________________________

    ____________________________________________________________
     Oops! Give me a task number.
    ____________________________________________________________

    ____________________________________________________________
     Oops! Give me a task number.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC9: Marking on an empty list is refused

**Aim:** Verify `mark 1` on an empty list is refused. Task 1 is a valid number
in general, so this checks the range is compared against the current task
count rather than the array size.

**Input:**

```text
mark 1
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Oops! There is no task with that number.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC10: Incomplete deadlines are refused

**Aim:** Verify a deadline missing its `/by`, its description, or its date is
refused in each case, and that a valid deadline still works afterwards.

**Input:**

```text
deadline submit report
deadline /by Sunday
deadline report /by
deadline return book /by Sunday
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Oops! A deadline needs a description and a /by part.
    ____________________________________________________________

    ____________________________________________________________
     Oops! A deadline needs a description and a /by part.
    ____________________________________________________________

    ____________________________________________________________
     Oops! A deadline needs a description and a /by part.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: Sunday)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[D][ ] return book (by: Sunday)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC11: Incomplete events are refused

**Aim:** Verify an event missing its `/to`, missing both parts, or missing its
description is refused, and that a valid event still works afterwards.

**Input:**

```text
event meeting /from Mon
event meeting
event /from Mon 2pm /to 4pm
event project meeting /from Mon 2pm /to 4pm
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Oops! An event needs a description, a /from part and a /to part.
    ____________________________________________________________

    ____________________________________________________________
     Oops! An event needs a description, a /from part and a /to part.
    ____________________________________________________________

    ____________________________________________________________
     Oops! An event needs a description, a /from part and a /to part.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 1 task in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC12: A blank line is ignored

**Aim:** Verify pressing Enter on its own produces no reply at all, rather than
an unknown-command complaint.

**Input:**

```text

todo read book
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
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Not covered by this plan

- **A full task list.** Adding a 101st task is refused with
  `Oops! Your list is full.`, verified by hand with a generated 101-command
  session. It is left out of the plan because a 100-line test case would
  dominate the file. The A-Collections extension in Level-6 removes the limit
  and with it this case.
