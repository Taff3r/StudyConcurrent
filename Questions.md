### What is a race condition? (Give an example)
Race conditions occur when two or more threads execute simuntainously and the programs functionality is dependant on them executing in a specific manner.
Example: 

We have two threads, T1 and T2, both read and write to the same resource which both read the same resource.
Both threads execute the same operations: 
```java
int commonInt = 0; // common global int
// ... T1 and T2 does this.
int r = commonInt; //read
r = r + 1; // increase
commonInt = r; // write back 
```
For the program to function properly both threads need to execute all three lines without the other reading the before the other has saved the value. Otherwise they would only have one of writes register.

### What does it mean that a class is thread safe?
A class is thread safe if it behaves correctly even when its objects are accesssed through multiple threads concurrently.
**Definition**:
>  A class is thread-safe if it behaves correctly when accessed from multiple threads ... with no additional synchronization or other coordination on the part of the calling code.

### Can a class be made thread-safe by declaring all methods `synchronized`? Motivate your answer. Can you come up with an example?
A class with all synchronized methods is not inherently thread-safe. If all logic is not executed within the monitor the class can still be thread-unsafe.
**Example**:
```java
int value = monitor.getValue();
value = value + 2;
monitor.setValue(value);
```
In the example above the monitor is not thread-safe, since the relevant value could have been changed between the `getValue()` and `setValue()` operations. A better, and thread-safe option would be (as long as method is still `synchronized`):
```java
monitor.increaseValueBy(2);
```

### Intrinsic locks (i.e. the `synchronized` locks) are reentrant. As the name suggests, `ReentrantLock` objects are too.
1. What does "reentrant" mean in this context?
2. Why are reentrant locks useful?
**Answer**
1. The process can claim the lock multiple times without blocking on itself.
2. They can provide synchronization of critical areas in code.

### For each of the following implementations of a `BankAccount` class determine whether or not the implementations are thread-safe.

1. No since the +=/-= are not atomic operations or `synchronized`. If multiple threads access the bank account object, the first thread can read the `balance` through deposit without adding, another thread can `withdraw()` reading the same value and decrementing, and then yet another thread does another `withdraw()` and decrements the value. The first thread then increments the value and returns. Since there is no synchronization only the threads that writes the last will count.
2. Yes since they methods are synchronized and only one thread can access the monitor at once. As long as the `getBalance()`-method is only used for viewing the latest balance, and not used in operations to withdraw or deposit.
3. Yes since when a variable is declated `volatile` it is instantly updated in all threads when it is changed, there is still `getBalance()` for the same reason as above.
4. Yes since atomic operations ready and write in one operation.

### Consider the following four techniques for thread safety:

1. Thread confinement
2. Instance confinement
3. Stack confinement
4. Immutability
Describe each of these in one or two sentances. For each technique, explain how it guarantees thread safety.

1. **Thread confinement** Using a single thread to perform all operations for a relevant resoruce, e.g. a GUI. Its thread safe, since there is only one thread that executes the operations.
2. **Instance confinement**  Encapsulating data within an object and ensuring that the data is only accessed by the objects methods, making it easier to ensure that the data is always accessed by is always accessed with the apporpriate lock held.
3. **Stack confinement** an object can only be reached through local variables, and only exist on the executing threads stack, no other threads can access it.
4. **Immutability** making the data immutable. Since it is immutable no thread can change its values.

#### See question above:
1. Which of the terms corresponds most closely to a monitor?
2. Which of the terms is used with the Swing EDT?

**Answer**
1. **Instance confinement**
2. **Thread confinement** 

### The BlockingQueue interface is implemented by ArrayBlockingQueue and LinkedBlockingQueue. The former has a bounded size, whereas the latter does not. Consider the call:
```java
q.put(m);
```
1. Can the `put()` call block if `q` is an `ArrayBlockingQueue`?
2. Can the `put()` call block if `q` is an `LinkedBlockingQueue`?

**Answer**
1. Yes. Blocks if queue is full.
2. No. Adds the element directly to the queue. 

**Advatantages of blocking** `put()`: Can be helpful if you want to throttle the amount of requests. With our own implementation, if the queue is full, we can also signal to the consumer that the queue is full and that they should wait and try later.
**Disadvatantages of blocking** `put()`: Consumer can get way ahead of producers, eventually leading to a `OutOfMemoryError`.

### What is `InterruptedException`? In which situations is it thrown?
**Answer**
`InterruptedException` is an exception which is thrown when a thread gets interrupted, either before or during the activity.
`InterruptedException` can be thrown when a thread is, waiting, sleeping, other blocking operations, or otherwise occupied.

### Suppose one thread interrups another as follows:
```java
t1.interrupt();
System.out.println("t1 has been interrupted");
```
The thread `t1` has been implemented to terminate when interrupted. However, when we run the program the printout often appears before `t1` acctually terminates.
1. Why is `interrupt()` not immediate? What must happen before `t1` acctually terminates?
2. Add one line to ensure that the printout isn't executed before `t1` is acctually terminated.

*. There is a cleanup phase that has to be executed first. Then a context switch.
*. ????? pls help
```java
t1.interrupt();
if(t1.isInterrupted())
    System.out.println("t1 has been interrupted");
```

### Suppose `t2` runs the following code:
```java
Queue q = new Queue();

public static void t2run(){
    try{
        while(true){
            String m = q.fetch();
            System.out.println("received " + m);
        }
    } catch (InterruptedException e){
        System.out.println("terminated");
    }
}
Thread t2 = new Thread(() -> t2run());
```
The monitor `q` is an instance of class `Queue`: 
```java
public class Queue {
    private List<String> list = new LinkedList<String>();
    
    public synchronized String fetch(){
        try{
            wait();
        } catch (InterruptedException e){
            // not my problem
        }
        return list.remove(0);
    }

    public synchronized void put(String m){
        list.add(m);
        notifyAll();
    }
}
```
Another thread now attempts to interrupt `t2`, just like in the previous example. However, `t2` doesn't respond to the interruption: the printout "terminated" never appears.

1. Why doesn't the `InterruptedException` reach `t2run()`?
2. Modify `Queue` to rectify the problem, by removing a few lines, and changing another.
.. * When the following command is executed: `t2.interrupt();` thread `t2` should now execute its catch clause and print "terminated".
**Answer**
1. Because the `InterruptedException` is ignored and not passed further up the stack.
2. The fix:

```java
public class Queue {
    private List<String> list = new LinkedList<String>();

    public synchronized String fetch() throws InterruptedException { // Changed here
        while (list.isEmpty()){
            wait();
        }
        return list.remove(0);
    }

    public synchronized void put(String m){
        list.add(m);
        notifyAll();
    }
}
```
### A server application includes 1000 tasks, all implementing the `Runnable` interface. Each task requires significant computation (minutes of processor time), and is launched when the method `launch()` below is executed.
```java
public class Server{
    /** Called whenever a new task arrives */
    public void launch(Runnable task){
        // To be implemented
    }
}
```
For the implementation of `launch()`, consider two alternatives:
1. We could start a new thread for each task, like this:
```java
public class Server{
    public void launch(Runnable task){
        Thread t = new Thread(task);
        t.start();
    }
}
```
OR we could submit the task to a thread pool with a limited number of threads, like this:
```java
public class Server{
    private ExectorServiver pool = Executors.newFixedThreadPool(4);
    public void launch(Runnable task){
        pool.submit(task);
    }
}
```
We assume that the number of threads above (4) to match the computer´s capacity for simultaneous threads (the number of cores or threads in the hardware).

In both solutions, the server turns out to be fully utilized - the computer is completely buy with task execution. Never the less, there is a performance difference between the two alternatives:
when given the same (large) number of tasks, one of them completes before the other.
Which solution is the more efficient on here, when we consider _throughput_ (number of tasks completed per second)? Motivate your answer.

**Answer**

Using the thread pool is more efficient, since there is less overhead from context switching. If server has exactly 4 cores, each thread can run independently on each core, only waiting when it needs to, but that depends on the program.


### In the previous assignment, tasks where long-running. Suppose we **also** introduce occasional short-running tasks (say less than 10ms). The server still runs a great number of long-running tasks, and occastionally also a short running one.
Again consider the two alternatives. Which solution would give the shortest average _response times_ for such occasional, short-running tasks? (Here we define the response time as the time from the call to `launch()` to the completion of the task).

**Answer**
The multiple thread solution, since a thread pool would have to queue them up, and if there are several (a lot) of tasks in the queue they will have to wait approximatly (several) * execution time of minutes before they get a reponse.

### Thread confinement is commonly applied in user-interface frameworks such as Swing. How?
**Answer**
By using a dedicated thread for updating the user interface, confining all work that is related to the GUI to one thread with the need for synchronization.

### What does the method `SwingUtilites.invokeLater` do? What is it used for?
**Answer**
It adds a Runnable to a queue of GUI updates that will get executed.

### What is the output of the following lines?
```java
SwingUtilites.invokeLater(() -> {
    if(SwingUtilites.isEventDispatchThread()){
        System.out.println("X");
    }else{
        System.out.println("Y");
    }
});
```
**Answer**
X

### Lock-free (or non-blocking) data structurs attain thread safety without using blocking or locks.
1. State one advantage of lock-free data structures.
2. How do lock-free data structures work? What synchronization mechanism(s) do they rely on?
Explain the idea in a few (two-three) sentences.

**Answer**
1. Guaranteed system-wide progress. Better for performance.
2. Non-blocking data structures uses common memory and atomic read-modify-write primitives that the hardware must provide. Critical sections are almost always implemented using standard interfaces over these primitives. 

### 19 TODO

### 20 TODO

### This task is about thread scheduling, and how it deffers between RTOS and desktop operatiing system such as Windows. Consider the threads `hi` and `lo` below.

```java
Semaphore sem = new Semaphore(0);

Thread hi = new Thread(() -> {
    sem.acquire();
    System.out.println("hi");
    // ...
});

Thread lo = new Thread(() ->{
    sem.release();
    System.out.println("lo");
    // ...
});

hi.setPriority(Thread.MAX_PRIORITY);
lo.setPriority(Thread.MIN_PRIORITY);
hi.start();
Thread.sleep(1000); // ensure thread 'hi' actually reaches line 5 before we continue.
lo.start();
```

1. If these threads run on a regular OS, in which order will the printouts appear? Is the order guaranteed to be the same every time?
2. If these threads were to run on an RTOS, in which order will the printouts appear? Is the order guaranteed to be the same every time?
**NOTE** ASSUME A SINGLE-CORE PROCESSOR!

**Answer**
1. lo then hi, but not guaranteed. Since there can occur a context switch after `sem.release()`.
2. hi then lo, since there is automatic context switch after the semaphore is released. 


### Suppose we have a real-time system with four threads running in an RTOS, using strict priorites, scheduled according to the RMS (Rate Monotonic Scheduling) strategy. For each threadi, we knowthe worst-case execution time Ci and the execution period Ti. We assume that the thread’s deadline equals  he period  –  that is, the thread must complete  its work before the next time it needs to execute.
We calculate U as:
U = Sum(1->4){Ci / Ti}
For which values of U can we be **sure** that the system is scheduable? - that is, that all threads will meet their deadlines. (**Assume a single core processor**)A

**Answer**
Using RMS analysis we can **guarantee** schedulabilitry if the sum stays below the upper bound U.
Sum < n(2^(1/n) - 1) = Un, where n is the number of threads.
So in this case: 4(2^(1/4) - 1) = 4*root(2)^root(2) - 4 = ~0.757
So all values of the sum that is < 0.757 is **guaranteed** to be scheduable.

### What is priority inversion? Explain in one or a few sentences.
**Answer**
Suppose we have three threads with (H)igh, (M)edium, and L(ow) priorities:
1. L executes and enters a critical region
2. M preempts and start executing
3. H preempts and tries to enter a monitor. H is blocked.
4. M continues executing for an arbitrary long period of time, blocking both L **AND** H!

### What is a distributed system? Explain in one or a few sentences.
A distributed system is a network that consists of autonomous computers that are connnected using a distribution middleware. They help in sharing different resources and capabilities to provide users with a single and integrated coherent network.

### When we design software, we often don’t need to consider hardware failures. For a distributed system, however, we must. Why?
**Answer**
TODO

### Why is message-passing useful in distributed systems?A
**Answer**
With distributed objects the sender and receiver may be on different computers, running different operating systems, using different programming languages, etc. In this case the bus layer takes care of details about converting data from one system to another, sending and receiving data across the network, etc.

### Virtually all computers use cache memories. On a multi-core computer, different threads may observe different values for the same variable. How can this happen? (Remember that on a multi-core computer, different threads may run on different cores.)
**Answer**
Each core has their own cache, which they access first sice it is faster. Example:
* Suppose processor `P1` has read the value of variable `x`.
* A _copy_ of `x` will be in the cache of `P1`.
* Assume next `P2` writes a new value to `x`.
* That new value, `x1`, will be in the cache of `P2`.
* What happens if `P1` wants to read `x` again?
* _Without_ caches `P1` would propably see the new value (If it has reached memory).

### Consider a situation where we use transactional memory instead of locks.
1. Deadlock is no longer possible. Why?
2. Instead, there is a greater potential for livelock. How?

**Answer**
1. There are no locks.
2. If there is a conflict in a transaction and both threads startover there can occur another conflict and it starts over again, repeat indefinetely...
.. *. Ex: Imaging walking down the street and walking into someone, you turn right, while the person meeting you turns left, you then turn left, and he turns right, but since the threads don't communicate this goes on forever.