import java.util.LinkedList;
import java.util.List;


public class ThreadPool {
    private Thread[] workers;
    private TaskQueue queue = new TaskQueue();

    public ThreadPool(int n) {
        this.workers = new Thread[n];
        for (int i = 0; i < n; i++) {
            workers[i] = new WorkerThread(queue);
            workers[i].start();
        }
    }

    public void submit(Runnable r) {
        queue.submit(r);
    }


    private class WorkerThread extends Thread {
        private TaskQueue queue;

        public WorkerThread(TaskQueue queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Runnable job = queue.awaitWork();
                    job.run();
                }
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
    }
        private class TaskQueue {
            private List<Runnable> tasks = new LinkedList<Runnable>();

            public synchronized void submit(Runnable r) {
                tasks.add(r);
                notifyAll();
            }

            public synchronized Runnable awaitWork() throws InterruptedException {
                while (tasks.isEmpty()) {
                    wait();
                }
                return tasks.remove(0);
            }
        }
}