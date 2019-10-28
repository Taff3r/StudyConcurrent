public class BankAccountSync {
    private long balance = 0;
    public synchronized void deposit(long n){
        balance += n;
    }
    public synchronized void withdraw(long n){
        balance -= n;
    }

    public synchronized long getBalance(){
        return balance;
    }

    public static void main(String[] args) {
        BankAccountSync account = new BankAccountSync();
        Thread t1 = new Thread(() -> {
            for(int i = 0; i < 1000; i++){
                account.deposit(10);
            }
        });
        Thread t2 = new Thread(() -> {
            for(int i = 0; i < 1000; i++){
                account.withdraw(10);
            }
        });
        Thread t3 = new Thread(() -> {
            for(int i = 0; i < 1000; i++){
                account.deposit(account.getBalance() - account.getBalance());
            }
        });
        t1.start();
        t2.start();
        t3.start();
        try {
            t1.join();
            t2.join();
            t3.join();
            System.out.println(account.getBalance());
        }catch (InterruptedException e ){
            throw new Error(e);
        }
    }
}
