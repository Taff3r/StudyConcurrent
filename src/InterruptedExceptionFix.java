public class InterruptedExceptionFix {

    public static void main(String[] args) {
        System.out.println("go");
       Thread t2 = new Thread(() -> t2run(new Queue()));
       t2.interrupt();
    }

    public static void t2run(Queue q){
        System.out.println("hello1");
        try {
            while (true){
                System.out.println("hello");
                String m = q.fetch();
                System.out.println("Received " + m);
            }
        } catch (InterruptedException e) {
            System.out.println("Terminated");
        }
    }

}