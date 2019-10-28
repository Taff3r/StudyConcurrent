public class InterruptedTest {
    public static void main(String[] args) throws InterruptedException {
        TestThread t1 = new TestThread();
        t1.start();

        t1.interrupt();
        if(!t1.isAlive()){
            System.out.println("t1 has been interrupted");
        }
    }

    private static class TestThread extends Thread{
        @Override
        public void run(){
            while(true){
                System.out.println("hello");
            }
        }
    }
}
