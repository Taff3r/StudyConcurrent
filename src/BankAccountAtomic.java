import java.util.concurrent.atomic.AtomicLong;

public class BankAccountAtomic {
    private AtomicLong balance = new AtomicLong(0);

    public void deposit(long n){
        balance.addAndGet(n);
    }

    public void withdraw(long n){
        balance.addAndGet(-n);
    }

    public long getBalance(){
        return balance.get();
    }

    public static void main(String[] args) {
        BankAccountAtomic account = new BankAccountAtomic();
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
                long l = account.getBalance();
                account.deposit(l - account.getBalance());
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
