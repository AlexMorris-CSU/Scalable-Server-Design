package cs455.scaling.client;

import cs455.scaling.util.hashing;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;

public class Client {

    private String serverIP;
    private int serverPort;
    private int messageRate;
    private LinkedList<String> hashes;
    private hashing hasher;
    private int sentSum;
    private int receiveSum;

    public Client(String serverIP, int serverPort, int messageRate){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.messageRate = messageRate;
        hashes = new LinkedList<String>();
        hasher = new hashing();
        sentSum = 0;
        receiveSum = 0;
    }

    public static void main(String[] args) throws IOException {
        Client client;
        client = new Client(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        client.startSocketChannel();
    }

    private void startSocketChannel() throws IOException {
        Thread messageSenderThread = new Thread(new SenderThread(this, serverIP, serverPort, hashes, messageRate));
        messageSenderThread.start();
        Timer timer = new Timer();
        ClientStatsReporter taskReport = new ClientStatsReporter(this);
        timer.scheduleAtFixedRate(taskReport, 0L, 20000L);
    }
    public synchronized void incrementSent(){
        sentSum++;
    }
    public synchronized void incrementReceived(){
        receiveSum++;
    }
    public synchronized int[] getSums(){
        int[] sums = {sentSum, receiveSum};
        sentSum = 0;
        receiveSum = 0;
        return sums;
    }
}
