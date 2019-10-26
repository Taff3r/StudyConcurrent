#Context Switches
Takes place when the system changes running process/thread.
Handled by the *kernel*
3 step process.

1. *Save* Turn off interrupts. Push PC, CPU registers to stack.
2. *Switch* Save stack pointer in process record. Get new process record and restore stack pointer from it.
3. *Restore* Pop CPU registers, from stack, and pop PC. Turn on interrupts.

**Each thread has its own stack!**