import java.util.LinkedList;
import java.util.List;

public class Queue {
    private List<String> list = new LinkedList<String>();

    public synchronized String fetch() throws InterruptedException{
        while (list.isEmpty()){
            System.out.println("waiting");
            wait();
        }
        return list.remove(0);
    }

    public synchronized void put(String m){
        list.add(m);
        notifyAll();
    }
}