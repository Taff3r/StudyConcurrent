import java.util.concurrent.Semaphore;

public class Priorites {
    public static void main(String[] args) {
        try {

            Semaphore sem = new Semaphore(0);

            Thread hi = new Thread(() -> {
                try {
                    sem.acquire();
                    System.out.println("hi");
                    // ...
                } catch (InterruptedException e) {
                    // hush
                }
            });

            Thread lo = new Thread(() -> {
                sem.release();
                System.out.println("lo");
                // ...
            });

            hi.setPriority(Thread.MAX_PRIORITY);
            lo.setPriority(Thread.MIN_PRIORITY);
            hi.start();
            Thread.sleep(1000); // ensure thread 'hi' actually reaches line 5 before we continue.
            lo.start();
        } catch (InterruptedException e){
            throw new Error(e);
        }
    }
}
