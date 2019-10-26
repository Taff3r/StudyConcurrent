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

## Thread safety
Definition of "thread-safe":
>  A class is thread-safe if it behaves correctly when accessed from multiple threads ... with no additional synchronization or other coordination on the part of the calling code.

**A monitor is NOT inherently thread-safe!**
Example:
```java
int t = monitor.getTime();
t = increaseByOneSecond(t);// some  integer  arithmetics
monitor.setTime(t);
```
Even if the monitor methods are mutually exclusive this will not give the desired result.
*Why?*
The monitor is **NOT** thread-safe.
If another thread sets the time in between the two monitor calls the variable `t` is outdated and the new time is lost when `monitor.setTime(t)` is called.

We need to use a single **atomic** operation to make sure that the time is kept correctly.
Such a method would instead, for example, be: 
```java
monitor.increaseTimeByOne();
```
OR
```java
monitor.increaseTime(1);
```
### General idea
Avoid **race-conditions** by using making monitor methods longer and `wait()`-ing for state changes.

## Immutable objects
Immutable objects have **all** attributes declared final, so that the object cannot be changed during run-time.
**Immutable objects are ALWAYS thread-safe**

## The Java memory model
Assume two threads and no synchronization.
```java
int x = 0;
/// Some time later Thread 1 does:
x = 7;
/// And later Thread 2 does:
System.out.println(x);
```
What will be printed?
**Answer: Can be both 0 or 7!**
A value written from one thread _without synchronization_ **not automatically** visible to other threads.

### volatile
In Java synchronized does two things:
* Ensure mutual exclusion/signaling for threads.
* Ensure changes to variables are visible to other threads.

In some cases we may wish to have the second feature without the first.
We can achieve this by declaring an attribute `volatile`.

```java
volatile int x = 1;
```

When a volatile shared variable is written its value is automatically visible to other threads.
With this change the previous example will always print 7.
However race conditions are still possible, **volatile is not replacement for synchronization**

### Atomic values
There are some classes in the `java.util.concurrent` package that contains small classes that monitor a single value. E.g. `AtomicInteger`

`AtomicInteger` for example contains the following methods:
```java
/** Atomically increments the value by one and returns the current value */
public int incrementByOneAndGet();

/** Atomically sets the value to the given updated value if current value == expected value. */
public boolean compareAndSet(int expect, int update);
```

### Thread-safety key points
* A thread-safe class meets its specification, even if accessed concurrently accessed by multiple threads.
* Just making methods `synchronized` is **not** enough.
* To attain thread-safety we often move logic from the threads to the monitor.
* An immutable object is **always** thread-safe.
* If a thread accesses attributes with neither synchronization or `volatile` strange things will happen, sooner or later.
* Prefer synchronization over `volatile` when ever possible.A
* Java's `java.util.concurrent` offers classes that support atomic modifications to single values.

## Thread pools and UI.
We sometimes want to define a **task** with some amount of work to be run in a seperate thread.
We often don't care which thread runs the task, just that it gets done.
```java
class SomeTask implements Runnable {
    public void run(){
        // long running work goes-here 
    } 
}
```
Ideally tasks are (mostly) **independent activities**.
For tasks we generally care about two things:
* **Throughput** The average number of tasks completed/second
* **Response time** The average time to complete a task.

We could run tasks in a dedicated Thread, how ever that results in some problems.
Each thread results in more context switches, scheduling, overhead, etc.
This leads to less and less processor resources being spent on useful work!

### Idea: seperate tasks from threads.
* **Tasks** defined by the application.
* **Threads** managed centrally.

A **Thread pool** is one way to achieve this.
* A pool includes a limited number of **worker threads**
* The **application** submits **tasks** to the **pool**
* Each worker thread picks a task, runs it, picks another task, runs it, and so on...

Think of thread pool as team or workers, sharing a single TODO-list.

### Thread pool classes
We can make a simple thread pool by using one of three classes:
* TaskQueue
* WorkerThread
* ThreadPool

```java
// Creates a ThreadPool with three threads.
ThreadPool pool = Executors.newFixedThreadPool(3);
```
A pool usually implements the `ExecutorService`










                                                                                                                               

