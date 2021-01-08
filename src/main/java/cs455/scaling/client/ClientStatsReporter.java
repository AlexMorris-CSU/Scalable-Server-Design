package cs455.scaling.client;

import java.sql.Timestamp;
import java.util.TimerTask;

public class ClientStatsReporter extends TimerTask {

    private Client client;
    private int[] sums;

    public ClientStatsReporter(Client client){
        this.client = client;
    }
    public void run(){
        sums = client.getSums();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.printf("%s\tTotal Sent Count: %d, Total Received Count: %d\n", timestamp, sums[0], sums[1]);
    }
}
