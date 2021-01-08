package cs455.scaling.server;

import cs455.scaling.client.Client;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.TimerTask;

public class ServerStatsReporter extends TimerTask {

    private ThreadPool threadPool;
    private ArrayList<Integer> sums;

    public ServerStatsReporter(ThreadPool threadPool){
        this.threadPool = threadPool;
    }

    public void run(){
        ServerStatistic serverStatistic = threadPool.getServerStatistics();
        int receivedSum = 0;
        ArrayList<Integer> clientsArray = new ArrayList<>();
        sums = serverStatistic.getArray();

        for(int i = 0; i < sums.size(); i++){
            receivedSum += sums.get(i);
            clientsArray.add(sums.get(i));
        }
        int sentSum = serverStatistic.getSentSum();
        int totalClients = serverStatistic.getTotalClients();
        double mean = 0;
        double stdDev = 0;

        mean = sentSum/totalClients;
        stdDev = calculateSD(clientsArray, mean);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.printf("%s\tServer Throughput: %.2f, Active Client Connections: %d, Mean Perclient Throughput: %f,\n" +
                "Std. Dev. Of Per-client Throughput: %f\n", timestamp, (double)sentSum/20, totalClients, mean, stdDev);
    }

    public static double calculateSD(ArrayList<Integer> table, double mean){
        double temp = 0;

        for (int i = 0; i < table.size(); i++) {
            int val = table.get(i);

            double squrDiffToMean = Math.pow(val - mean, 2);

            temp += squrDiffToMean;
        }

        double meanOfDiffs = (double) temp / (double) (table.size());
        return Math.sqrt(meanOfDiffs);
    }
}
