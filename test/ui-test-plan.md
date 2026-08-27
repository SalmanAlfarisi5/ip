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

The app saves its task list, so the runner passes a scratch data file
(`_temp/ui-test-data.txt`) and deletes it before each case. Cases therefore
start with an empty list unless they declare a `Setup input:` session, which
runs first against the same file to seed it.

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

**Aim:** Verify an unknown command is refused, quoted back so the user can see
what was not understood, and crucially that it is not silently added to the
list. The trailing `list` is what proves the internal state stayed clean.

**Input:**

```text
blah
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Sorry, I don't know what "blah" means.
     I understand: todo, deadline, event, list, mark, unmark, delete, bye.
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
     A todo needs a description.
     Try: todo read book
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
     A todo needs a description.
     Try: todo read book
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
than crashing, and that each is distinguished: out of range above and below,
non-numeric, and missing. The missing-number message must name the command the
user actually typed. The closing `list` confirms the real task survived all of
it untouched.

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
     There is no task 99 in your list.
     You only have task 1.
    ____________________________________________________________

    ____________________________________________________________
     There is no task 0 in your list.
     You only have task 1.
    ____________________________________________________________

    ____________________________________________________________
     "abc" is not a number.
     Try: mark 2
    ____________________________________________________________

    ____________________________________________________________
     mark needs a task number.
     Try: mark 2
    ____________________________________________________________

    ____________________________________________________________
     unmark needs a task number.
     Try: unmark 2
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC8b: The valid range is reported when several tasks exist

**Aim:** Verify the out-of-range message switches from the singular
"You only have task 1." to a range once there is more than one task.

**Input:**

```text
todo read book
todo buy bread
mark 99
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
       [T][ ] buy bread
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     There is no task 99 in your list.
     Pick a number from 1 to 2.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC9: Marking on an empty list is refused

**Aim:** Verify `mark 1` on an empty list is refused. Task 1 is a valid number
in general, so this checks the range is compared against the current task
count rather than the array size. The empty list gets its own message, since
telling the user to "pick a number from 1 to 0" would be nonsense.

**Input:**

```text
mark 1
bye
```

**Expected output:**

```text
    ____________________________________________________________
     There is no task 1: your list is empty.
     Try: todo read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC10: Incomplete deadlines are refused

**Aim:** Verify a deadline missing its `/by`, its description, or its date is
refused with a *different* message in each case, so the user is told which
part is missing rather than being handed a generic complaint. A valid deadline
afterwards confirms the rejections left the list usable.

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
     I couldn't find a /by in that deadline.
     Try: deadline return book /by Sunday
    ____________________________________________________________

    ____________________________________________________________
     That deadline has no description before the /by.
     Try: deadline return book /by Sunday
    ____________________________________________________________

    ____________________________________________________________
     That deadline has no due date after the /by.
     Try: deadline return book /by Sunday
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

**Aim:** Verify each of the five ways an event can be incomplete gets its own
message: no `/from`, no `/to`, no description, no start time, no end time. A
valid event afterwards confirms the rejections left the list usable.

**Input:**

```text
event meeting
event meeting /from Mon
event /from Mon 2pm /to 4pm
event meeting /from /to 4pm
event meeting /from Mon 2pm /to
event project meeting /from Mon 2pm /to 4pm
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     I couldn't find a /from in that event.
     Try: event project meeting /from Mon 2pm /to 4pm
    ____________________________________________________________

    ____________________________________________________________
     I couldn't find a /to in that event.
     Try: event project meeting /from Mon 2pm /to 4pm
    ____________________________________________________________

    ____________________________________________________________
     That event has no description before the /from.
     Try: event project meeting /from Mon 2pm /to 4pm
    ____________________________________________________________

    ____________________________________________________________
     That event has no start time after the /from.
     Try: event project meeting /from Mon 2pm /to 4pm
    ____________________________________________________________

    ____________________________________________________________
     That event has no end time after the /to.
     Try: event project meeting /from Mon 2pm /to 4pm
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

### TC13: Deleting from the middle renumbers the rest

**Aim:** Verify `delete` removes the right task, reports the new total, and
that the tasks after it move up. Marking task 3 afterwards is the real check:
if the deleted slot were left behind, task 3 would still be the old task 4 and
the wrong item would be marked.

**Input:**

```text
todo read book
todo return book
todo buy bread
todo join club
delete 2
list
mark 3
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
       [T][ ] return book
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] buy bread
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] join club
     Now you have 4 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [T][ ] return book
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] buy bread
     3.[T][ ] join club
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] join club
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[T][ ] buy bread
     3.[T][X] join club
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC14: Deleting the last task, then an out-of-range delete

**Aim:** Verify deleting the final task leaves an empty list rather than
running off the end, and that the now-stale task number is refused afterwards.

**Input:**

```text
todo read book
delete 1
delete 1
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
     Noted. I've removed this task:
       [T][ ] read book
     Now you have 0 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     There is no task 1: your list is empty.
     Try: todo read book
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC15: A bad delete number is refused

**Aim:** Verify `delete` reuses the same task-number validation as `mark`, and
that its error hints name `delete` rather than another command.

**Input:**

```text
todo read book
delete 99
delete abc
delete
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
     There is no task 99 in your list.
     You only have task 1.
    ____________________________________________________________

    ____________________________________________________________
     "abc" is not a number.
     Try: delete 2
    ____________________________________________________________

    ____________________________________________________________
     delete needs a task number.
     Try: delete 2
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

## Not covered by this plan

- **A very large task list.** The list is held in an `ArrayList`, so there is
  no fixed limit to bump into. Adding 150 tasks was checked by hand; it is left
  out of the plan because a 150-line test case would dominate the file. The
  earlier 100-task limit, and its "your list is full" error, no longer exist.

### TC17: Tasks survive a restart

**Aim:** Verify the list is written to disk when it changes and read back on
startup. The setup session below runs first against the same data file; this
case then starts a *fresh process* and lists what was saved. Marking is
included so the done status is checked too, not just the descriptions.

**Setup input:**

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
mark 1
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC18: A deletion is persisted too

**Aim:** Verify saving happens on every change, not only on add. If save were
wired only into the add commands, the deleted task would reappear here.

**Setup input:**

```text
todo read book
todo return book
delete 1
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] return book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC19: A corrupted save file is repaired, not fatal

**Aim:** Verify a damaged data file neither crashes the chatbot nor discards
the tasks that are still readable. Each bad line is named with its line number
and the specific fault, so a hand-edited file can be corrected. The six bad
lines cover every rejection the loader makes: too few fields, unknown type,
invalid done flag, empty description, deadline with no date, event missing an
end. Line 8 is blank and must be skipped silently rather than reported.

**Setup data file:**

```text
T | 1 | read book
GARBAGE LINE
D | 0 | return book | June 6th
X | 0 | alien task
T | 7 | bad flag
T | 0 | 
D | 0 | no due date

E | 0 | meeting | Mon
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     I skipped 6 unreadable lines in your saved data:
     line 2: expected at least 3 fields, found 1
     line 4: unknown task type "X"
     line 5: the done flag should be 0 or 1, found "7"
     line 6: expected at least 3 fields, found 2
     line 7: a deadline needs a due date
     line 9: an event needs both a start and an end
     Everything else loaded fine. The bad lines will be dropped
     the next time your list changes.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][X] read book
     2.[D][ ] return book (by: June 6th)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```

### TC20: A clean save file produces no warning

**Aim:** Verify the skipped-line warning appears only when something was
actually wrong. A loader that reported on every startup would train the user
to ignore it.

**Setup data file:**

```text
T | 0 | read book
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope to see you again soon!
    ____________________________________________________________
```
