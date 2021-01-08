package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Timer;

public class Server {
    private int port;
    private String IP;
    private int threadCount;
    private int batchSize;
    private int batchTime;
    private ThreadPool threadPool;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private boolean first;

    public Server(int port, int threadCount, int batchSize, int batchTime){
        this.port = port;
        this.threadCount = threadCount;
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        this.first = true;
        try {
            this.selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            InetAddress host = InetAddress.getLocalHost();
            serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        threadPool = new ThreadPool(this, threadCount, selector, batchSize, batchTime);
    }

    public static void main(String[] args){
        Server server = new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }

}
