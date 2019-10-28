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

1. Because the threads reenter at the same position (line of code) when they are unblocked.
2. They can provide synchronization of critical areas in code.

### For each of the following implementations of a `BankAccount` class determine whether or not the implementations are thread-safe.

1. No since the +=/-= are not atomic operations. If multiple threads access the bank account object, the first thread can read the `balance` through deposit without adding, another thread can `withdraw()` reading the same value and decrementing, and then yet another thread does another `withdraw()` and decrements the value. The first thread then increments the value and returns. Since there is no synchronization only the threads that writes the last will count.
2. (MatyYes since there is synchronization and the `balance` attribute is declared private. 
3. Yes since when a variable is declated `volatile` it is instantly updated in all threads when it is changed.
4. Yes since atomic operations ready and write in one synchronized method.

### Consider the following four techniques for thread safety:

1. Thread confinement
2. Instance confinement
3. Stack confinement
4. Immutability
Describe each of these in one or two sentances. For each technique, explain how it guarantees thread safety.

1. **Thread confinement** Using a single thread to perform all operations for a relevant resoruce, e.g. a GUI. Its thread safe, since there is only one thread that executes the operations.
2. **Instance confinement** instance confinement means keeping all state to yourself. Since the state is private