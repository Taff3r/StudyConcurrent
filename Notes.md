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
// Creates a ExecutorService pool (thread pool) with three threads.
ExecutorService pool = Executors.newFixedThreadPool(3);
```
A pool usually implements the `ExecutorService` interface.

### Callables and Futures

Runnables does not produce any results. (return type is `void`)
However a Callable does.
```java
public interface Callable<V>{
    public V call();
}
```
A thread pool returns a Future, which can be used to keep track of the task's progress.

```java
public interface Future<V>{
    /** Waits if necessary for the computation to complete, and then retrieves the result. */
    V get();
                                                   
    /** Attempts to cancel execution of this tasks */
    boolean cancel(boolean mayInterruptIfRunning);

    /** Returns true if this task is completed. */
    boolean isDone();

    // ...
}
```
**Runnable**s have no result, but can still be used as Futures.

```java
Runnable task = () -> { // Do stuff..};
Future<?> future = pool.submit(task);

future.get() // Will block and return no result. Much like calling join() on a Thread.

// We can cancel (stop) a submitted task with cancel();
future.cancel(true);
```

### Swing 
Standard Java GUI framework.
Like most other GUI frameworks, Swing requires special care when used with threads.

### Threading in GUI.
Most GUI frameworks are single-threaded.
Because of performance and deadlock issues.
In Swing this is the **Event Dispatch Thread (EDT)**

The EDT can be thought of as a "single-threaded thread pool"
So to do something with Swing submit it as a task to the EDT.

There is two static methods to submit a runnable to the EDT.
```java
SwingUtilites.invokeLater(() -> {...});
// And
SwingUtilites.invokeAndWait(() -> {...});
```
These are similar but `invokeAndWait();` also blocks.
The Runnables in used should be short. **Don't** block, `sleep();`, or perform lengthy computations.
If there is a need to block/perform lengthy computations, use a separate thread or thread pool.

### Swing threading dos and dont's
* DO wrap all calls to Swing in the static methods.
* DON'T block, sleep(), etc. in Swing (listener) callbacks.

### Thread confinement
Thread confinement can be used to achieve **thread-safety**.
Using a single thread to do the work on behalf of the application makes sure that only thread does all the relevant work.

## Message passing
In Java we use `BlockingQueues` which can hold messages of any type.
* A thread can put messages in several queues.
* A thread _typically_ only reads from one queue. (Otherwise things get tricky, since fetching a message from a queue is a blocking operation)
* **No shared data** (beyond sent/received messages)
* Messaging should be asynchronous the `send()` operation should return immediately. (Not block)

* A message queue, as well as a Semaphore can be implemented as a monitor.
* An empty queue is equivalent to a Semaphore
.. * The value of the counter of the semaphore corresponds to the number of messages in the mailbox.
.. * Send message == `release();`
.. * Receive message == `acquire();`
* Messages can be arbitrary objects, useful for passing information between threads.

All three constructs are equally powerful, but practical in different situations.

We often want to associate a message queue with a thread.
Each such thread has its own **queue of received messages**
Each thread sends to **other** queues (many) and receives from **one**.

### Message passing dos and don'ts

* DON'T share any data (beyond messages) between threads.
* DON'T retain a message after sending it to another thread.
* AVOID mixing message-passaging with monitors/semaphores.
* DO use immutable messages.
* DO receive messages **EXACTLY ONCE** in your threads.
* DO include all information the receiver needs in the message, create a Message class if needed.

## Thread interruption
Java has a general feature for interrupting threads, `interrupt();`.

When the `interrupt();` is called.
* The interrupted status of the thread is set to `true`.
* As soon as the thread performs a blocking operation, an `InterruptedException` is thrown.
* The corresponding catch in the thread will be executed.

Calling `interrupt();` causes the thread to stop **as soon as possible (NOT IMMEDIATELY)**.
It then performs any final clean-up work, such as close files and network connections.
Then it exits (return from `run()`).

### Handling InterruptException
* In a monitor method, we usually **don't know** - use throws.
* In a `run()` method, we usually **do** - use catch.

## Deadlock
Mutual exclusion means that a thread can be **blocked**. If the blocking never ends, we have **deadlock** situation.

### No resource preemption 
Once a thread holds a resource only the thread itself can release it.
The system, or another thread, cannot take away the resource.
We say that there is **no resource preemption**.

### Circular wait
* If several threads **can be** waiting for each other, we have a deadlock **risk**.
* When several threads are **waiting** for each other, we have a **deadlock**.

### Four conditions for deadlock to occur
1. Mutual exclusion: only one thread can access a resource at a time. **Always to true**
2. Hold and wait: a thread can reserve a resource and the wait for another. **Often useful**
3. No resource preemption: a thread cannot be forced to release held resources. **Always true**
4. Circular wait: thread-resource dependencies are circular. **Does not have to be true, can be avoided!**

### Resource allocation graphs
1. Draw resource (e.g. monitors, mutexes)
2. Draw hold-wait situations (Arrow from each hold resource to a thread marker, arrows from thread marker to resource waited for)
3. Cycles indicate risk for deadlock. The number of 'hold-wait' links in the circular chain shows how many and which threads are required for deadlock.

### Starvation
If a thread attempts to allocate a resource it must be able to get it eventually.

### Livelock
Occurs when several threads attempt to allocate the same resource but none actually get it due to the execution pattern.
Behaves like deadlock, but the threads actually runs. (But don't do any meaningful work)

## Real time systems

A **single-core** computer can only execute one thread at a time.
A **multi-core** computer can execute multiple threads at the same time, making concurrent applications faster.
To fully utilize a multi-core computer we need threads, or something equivalent.

**General meaning of Real-time systems**:
* reactive systems, executing in response to external events.
**Specific meaning** (in some cases):
* reactive systems subjects to **deadlines**:
.. * a late result is at best meaningless, at worst **catastrophic**; often referred to as **firm** or **hard real-time systems**.

### Concepts
1. **Release time**, the desired start of period/job.
2. **Start time**, after context switch, invocation of control computation.
3. **Response time**, contol and execution is completed, including update of control states and any preperation for next sample/job.

### Scheduling
We could come up with a schedule, assuming we know:
* Worst-cade repsonse time (R)
* Deadlne (D)
* Period (T)

#### Isn't this what OS is for?
Desktop OS:s 
* Focus on **average-case** performance (**interactive** systems)
* Avoid starvation (all threads should eventually get to run)
* Boost interactive applications
* Ensure one bad high-prio thread cannot bring down the system

In such operating systems, assigning **higher priority** to a thread leads to it receiving a **larger share of processor time**.
(The course book recommends against trying to solve concurreny problems using priorities (218-219)

For a (hard) real-time system, we need something else.
**Real-time operating systems (RTOS)** are designed for predicatable real-time systems, and prioritiex low latency over high throughput.
A common strategy is to use **strict priorities**: the highest priority, ready threads is **always** selected to run.
(RTOS should not be run on PCs, as they are not good for interactive systems, RTOSes are good for embedded systems with real-time requirements)

### Regular priorities (desktop OS) vs. strict priorites (RTOS)
Assume that we have two threads, and T2 has the highest priority.
```java
Semaphore sem = new Semaphore(0);
```

Assume that T2 reaches `sem.acquire()` before T1 has started executing.
Then T1's call to to `sem.release()` makes T2 ready to continue.
What happens here? Does T1 or T2 continue first?

In a regular OS, T2 will start running at some later point, and T1 and T2 could execute in any order. (Even on a single core system).
In an RTOS `sem.release()` will cause an automatic context switch, and T2 will run immediately, since it has higher priority (On a single core machine).

### Fixed priority scheduling
* Most OS's schedulers, as well as Java threads are based on priorities. (Other choices are possible, e.g. deadling scheduling).
* For real-time systems, we use strict priorites. (Typically handeled by an RTOS)
* We assume **fixed-priority scheduling**, the threads priority don't change during run-time.
* We assume that we know the WCET (worst-case execution time) for each task.

### Scheduling analysis
**Problems:**
* How can we know wheter or not our threads will meet their deadlines?
* How do we assign priorites to our threads? 

### Rate monotonic scheduling
**Idea**: Set priority according to period.
Short period == high priority!

**Notation**
For each thread we know:
* T = Period
* C = Execution Time (WCET)
* U = C/T = CPU Utilization

### Earliest deadline first
**Idea**: always assign the CPU the to thread cloesest to its deadline.
Advantages: 100% CPU utilization is possible.
**BUT** requires special OS support, **and** behaves badly at overload. (Leading to all deadlines missed)

### Complications
#### Blocking
A high-priority thread must not wait for lower-priority threads (more than a short time)
**Problem**: Can we guarantee timing for high-priority threads without knowing all low- or medium-priority threads?
What about blocking on shared resources?

#### Priority inversion
Suppose we have three threads with (H)igh, (M)edium, and L(ow) priorities:
1. L executes and enters a critical region
2. M preempts and start executing
3. H preempts and tre to enter a monitor. H is blocked.
4. M continues executing for an arbitrary long period of time, blocking both L **AND** H!

#### WCET
Will the following loop ever terminate?
```java
int n = 1234;
while(n != 1){
    if(n % 2 == 0){
        n = n / 2;
    }else{
        n = n * 3 + 1;
    }
}
```
What is the WCET of this algorithm?

### Limitations of fixed-priority scheduling

* The present theoretical frameworks (RMS, EDF, ...) assume a **single core processor**
* Multicore real-time scheduling is an area of active research (and far more complex)
* Quickly gets even more complicated when we take blocking and priority inversion into account.
* WCET is notoriously hard to obtain.
