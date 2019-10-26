public class SequentialExecution {
    public static void main(String[] args) {
        int x = 2;
        while(x > 1){
            if(x == 10){
                x = 0;
            }
            x++;
        }
        System.out.println(x); // What should be printed, why isn't it 0?
    }
}
