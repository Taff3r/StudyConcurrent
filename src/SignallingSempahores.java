import java.util.concurrent.Semaphore;

public class SignallingSempahores {
    public static void doStuff(String name, Semaphore a, Semaphore b){
        while(true) {
            try {
                a.acquire();
                for (int i = 0; i < 1000; i++) {
                    System.out.println(name);
                }
                b.release();
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
    }


    public static void main(String[] args) {
       Semaphore a = new Semaphore(1);
       Semaphore b = new Semaphore(0);

       Thread t1 = new Thread(() -> doStuff("Simon", a, b));
       Thread t2 = new Thread(() -> doStuff("Julia", b, a));
       t1.start();
       t2.start();
    }
}
