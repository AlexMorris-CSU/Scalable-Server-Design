package cs455.scaling.server;

import cs455.scaling.util.Task;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")

public class ThreadPool {
    private int threadCount;
    private int batchSize;
    public int batchTime;
    private Worker[] workers;
    private Selector selector;
    public ArrayList<Integer> availableWorkers;
    private ArrayList<Task> batch;
    private ArrayList<ArrayList> batchesArray;
    private Server server;
    private long lastTime;
    private long currentTime;
    private Map<SocketAddress, Integer> clientsMap;
    public AtomicInteger sentSum;

    public ThreadPool(Server server, int threadCount, Selector selector, int batchSize, int batchTime){
        this.threadCount = threadCount;
        this.batchSize = batchSize;
        this.batchTime = batchTime * 1000;
        this.server = server;
        workers = new Worker[threadCount];
        this.selector = selector;
        availableWorkers = new ArrayList<Integer>();
        startWorkers(threadCount);
        clientsMap= new ConcurrentHashMap<SocketAddress, Integer>();
        sentSum = new AtomicInteger(0);
        try {
            listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startWorkers(int threadCount){
        for(int i = 0; i < threadCount; i++) {
            Worker worker = new Worker(this, i);
            workers[i] = worker;
            Thread workerThread = new Thread(worker);
            workerThread.start();
        }
    }

    public synchronized void setThreadAvailable(int threadID){
        availableWorkers.add(threadID);
        //System.out.println("ava");
    }

    public synchronized void setThreadBusy(int threadID){
        availableWorkers.remove(Integer.valueOf(threadID));
        //System.out.println("not ava");
    }

    public void listen() throws IOException {
        batch = new ArrayList<Task>();
        batchesArray = new ArrayList<>();
        lastTime = System.currentTimeMillis();
        System.out.println("Server Started");
        startStats();
        while(true) {
            this.selector.selectNow();
            Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
            while(iter.hasNext()){
                SelectionKey key = iter.next();
                if(key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(this.selector, SelectionKey.OP_READ);
                    clientsMap.putIfAbsent(client.getRemoteAddress(), 0);
                    //System.out.println("Successfully connected to " + client.getRemoteAddress());
                }else if(key.isReadable()){
                    //System.out.println("New Readable");
                    SocketChannel client = (SocketChannel) key.channel();
                    clientsMap.compute(client.getRemoteAddress(), (k, v) -> v + 1);
                    //System.out.println(clientsMap.size());
                    key.interestOps(SelectionKey.OP_WRITE);
                    Task newTask = new Task(key);
                    batch.add(newTask);
                }
                iter.remove();
            }
            currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastTime;
            if(batch.size() == batchSize || timeDiff > batchTime){
                if(batch.size() == 0) {
                    continue;
                }else if(availableWorkers.size() == 0){
                    batchesArray.add(batch);
                    batch = new ArrayList<Task>();
                    continue;
                }else {
                    lastTime = currentTime;
                    if(batchesArray.size() != 0){
                        int nextWorker = availableWorkers.remove(0);
                        workers[nextWorker].addBatch(batchesArray.remove(0));
                    }else{
                        int nextWorker = availableWorkers.remove(0);
                        //System.out.println("sent to worker " + nextWorker);
                        workers[nextWorker].addBatch(batch);
                        setThreadBusy(nextWorker);
                        batch = new ArrayList<Task>();
                    }
                }
            }
        }
    }

    public synchronized ServerStatistic getServerStatistics(){
        int totalClients = clientsMap.size();
        //System.out.println(totalClients);
        Set<SocketAddress> keySet = clientsMap.keySet();
        ServerStatistic serverStatistic = new ServerStatistic(sentSum.get(), totalClients);

        for(Object key: keySet){
            serverStatistic.addToArray(clientsMap.get(key));
            //System.out.println(clientsMap.get(key));
            clientsMap.compute((SocketAddress) key, (k, v) -> v - v);
            //System.out.println(clientsMap.get(key));
        }
        sentSum.set(0);
        return serverStatistic;
    }

    public void startStats(){
        Timer timer = new Timer();
        ServerStatsReporter taskReport = new ServerStatsReporter(this);
        timer.scheduleAtFixedRate(taskReport, 20000L, 20000L);
    }

}
