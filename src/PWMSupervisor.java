import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

public class PWMSupervisor{
    private List<Double> voltages = new ArrayList<>();
    private double average = 0;

    public PWMSupervisor(){
        Thread t = new SensorThread(this);
        t.start();
    }

    public synchronized getAverageVoltage(){
        return average;
    }

    public synchronized void reportVoltage(double voltage) throws InterruptedException{
        voltages.add(voltage);
        if(voltages.size() > 5){
            voltages.remove(0);
        }
        double total = 0;
        for(double v : voltages){
            total += v;
        }
        this.average = total / voltages.size();
        notifyAll();
    }


    public synchronized double awaitChangeFrom(double reference) throws InterruptedException{
        while(Math.abs(this.average - reference) / reference < 0.05){
            wait();
        }
        return this.average;
    }
}


public class SensorThread extends Thread{
    private PWMSupervisor supervisor;
    private Sensor sensor = new Sensor();
    private final double VOLTAGE = 10.0;
    public SensorThread(PWMSupervisor supervisor){
        this.supervisor = supervisor;
    }

    @Override
    public void run(){
        long startOfLastPeriod = -1;
        long lastFallingFlank = -1;
        while(true){
            Flank f = sensor.awaitFlank();
            if(f.rising()){
                if(startOfLastPeriod >= 0 && lastFallingFlank >= 0){
                    long periodTime = f.timestamp() - startOfLastPeriod;
                    long pulseWidth = periodTime - (f.timestamp() - lastFallingFlank);
                    supervisor.reportVoltage(VOLTAGE * (pulseWidth / periodTime));
                    startOfLastPeriod = f.timestamp();
                }else{
                    startOfLastPeriod = f.timestamp();
                }
            }else{
                lastFallingFlank = f.timestamp();
            }

        }
    }
}