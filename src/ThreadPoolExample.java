import java.util.LinkedList;
import java.util.List;

public class ThreadPoolExample {

    public static void main(String[] args) throws InterruptedException {
        ThreadPool tp = new ThreadPool(3);
        tp.submit(() -> {
            System.out.println("Hello there!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        tp.submit(() -> {
            System.out.println("General Kenobi!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        tp.submit(() -> {
            System.out.println("You are a bold one!");
            System.out.println("Notice that Thread.sleep() does nothing here since they run concurrently.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        Thread.sleep(5000);
        System.out.println("However if we only have 2 worker threads...");
        Thread.sleep(5000);
        ThreadPool tp2 = new ThreadPool(2);
        tp2.submit(() -> {
            System.out.println("Hello there!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        tp2.submit(() -> {
            System.out.println("General Kenobi!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        tp2.submit(() -> {
            System.out.println("You are a bold one!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
       Thread.sleep(1000);
       System.out.println("There is a delay between the second and third line");
    }

}