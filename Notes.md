## Context Switches
Takes place when the system changes running process/thread.
Handled by the *kernel*
3 step process.

1. *Save* Turn off interrupts. Push PC, CPU registers to stack.
2. *Switch* Save stack pointer in process record. Get new process record and restore stack pointer from it.
3. *Restore* Pop CPU registers, from stack, and pop PC. Turn on interrupts.

**Each thread has its own stack!**
# When does a context switch occur?

*Depends on the system*
Different strategies:
+ Nonpreemptive scheduling
.. + Running thread runs until it voluntarily releases the CPU.
.... + Explicilty by calling `yield()`
.... + or by blocking, e.g. calling `acquire()`
+ Preemptive scheduling 
.. + The OS can interrupt the running process at any given time. (Typically by using hardware timers and interrupts)
+ Preemption points
.. + A context switch can only occur at specific points(according to language, compiler, and/or run-time system)

**In this course, we assume _preemptive_ _scheduling_** 

## Executing states
Every thread is in one of three states:
+ *Running* (Moved from ready state, handled by scheduling by OS)
+ *Blocked* (Moved from running state when thread executes blocking call, such as `lock()` or `acquire()`)
+ *Ready* (Moved from block state when _another_ thread executes a unblocking call, such as `unlock()` or `release()`)

**The OS keeps track of which threads are in which state (In Java the JVM might also be involved)**
    
## Sequential and parallel behaviour

### Some things are sequential
Such as single actor/user actions, software execution
### MOST things are parallel
In concurrent programming we have multiple threads to handle multiple "events" simultaneously
+ multiple actors
+ interaction with the physical world (e.g. sensors)
**We need to think both sequentially _and_ concurrently!**

## Three ways of checking if a resource is available

1. Busy-wait **(DON'T DO THIS)**
```java
while(n == 0) {
    //keepspinning}
}
```
2. Polling **(DON'T DO THIS)**
```java
while(n == 0){
   Thread.sleep(500);
}
```
3. Using Semaphore **(DO THIS)**
```java
Semaphore avail = new Semaphore(1);
// Consumer thread.
avail.acquire();
// ...
avail.release();
```
**Busy-wait and polling should be avoided** They are inefficient and consumes a lot of CPU resources.
There is also `wait()` `notify()` that can be used instead of Semaphores.

## Monitors

Use monitors to make sure shared resources are executed with mutual exclusion.
Can be achieved by using in-line Semaphores.
Java has built in language support for monitors.
Use the `synchronized` key-word in methods.
This achieves the same as using Semaphores without the clutter.
*Signaling* can be achieved by using `wait()` and `notify()`/`notifyAll()`
Using synchronized code we can safely call other synchronized methods without risking _deadlock_.

## Wait Notify
Always call `wait()` in while-loops.
Call `notify()`/`notifyAll()` when the state of a monitor has been changed. (Not always, but if coded correctly more calls shouldn't be a worry)

### Wait notify state
Wait notify is stateless, compared to Semaphores.
If no thread is waiting a notify has no effect.
If a thread would wait it has to be notified to wake up. Compared to Semaphores where the thread continues unless it is blocked.

## Monitors dos and don'ts

* DON'T mix a thread and monitor in same object/class
* DON'T assume the notified thread to start running immediately. (Dependant on OS scheduling) 
* DON'T assume the condition to be true when a thread gets notified. Another thread could've changed the state in between wake-up and notification.
Always `wait()` in a while-loop, not an if-statement.
* AVOID `synchronized` blocks.
* DO use `notifyAll()` instead of `notify()` as `notify()` to ensure that the relevant thread wakes up.
* DO make all `public` methods `synchronized`.
* DO wrap thread-unsafe classes by monitor.


