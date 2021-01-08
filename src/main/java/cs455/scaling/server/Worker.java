package cs455.scaling.server;

import cs455.scaling.util.Task;
import cs455.scaling.util.hashing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;

public class Worker implements Runnable{

    private int workerID;
    ThreadPool threadPool;
    private ArrayList<Task> batch;
    private boolean isWorking;
    private hashing hasher;

    public Worker(ThreadPool threadPool, int workerID){
        this.workerID = workerID;
        this.threadPool = threadPool;
        isWorking = false;
        hasher = new hashing();
    }

    @Override
    public void run() {
        //System.out.println("Worker Started " + workerID);
        batch = new ArrayList<Task>();
        threadPool.setThreadAvailable(workerID);
        while(true){
            if(batch.size() != 0){
                isWorking = true;
                Task currentTask = batch.remove(0);
                //System.out.println("HERE " + workerID);
                try {
                    readComputeWrite(currentTask);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(isWorking && batch.size() == 0){
                isWorking = false;
                threadPool.setThreadAvailable(workerID);
            }else{
                try{
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void addBatch(ArrayList<Task> batch1){
        this.batch = batch1;
        //System.out.println("BATCH: " + batch.size());
    }

    private void readComputeWrite(Task task) throws IOException {
        SelectionKey key = task.getKey();
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        buffer.clear();
        while(buffer.hasRemaining()) {
            client.read(buffer);
        }

        byte[] unhash = buffer.array();
        String unhashed = hasher.SHA1FromBytes(unhash);
        //System.out.println(unhashed);

        byte[] newHash = unhashed.getBytes();
        threadPool.sentSum.getAndIncrement();

        ByteBuffer sendBuffer = ByteBuffer.wrap(newHash);
        while(sendBuffer.hasRemaining()){
            client.write(sendBuffer);
        }

        key.interestOps(SelectionKey.OP_READ);

        // Places read data into a hash task to be processed using the Hash.java class

        // set the key's interest set to writing
    }
}
