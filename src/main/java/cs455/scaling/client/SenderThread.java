package cs455.scaling.client;

import cs455.scaling.util.hashing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class SenderThread implements Runnable{
    private Client client;
    private int messageRate;
    private Random random;
    private hashing sha1;
    private SocketChannel socketChannel;
    private LinkedList<String> hashes;
    private Selector selector;

    public SenderThread(Client client, String serverIP, int serverPort, LinkedList<String> hashes, int messageRate) throws IOException {
        this.client = client;
        this.messageRate = messageRate;
        this.hashes = hashes;
        random = new Random();
        sha1 = new hashing();

        this.selector = Selector.open();
        this.socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(serverIP, serverPort));
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_WRITE);
    }

    public void run() {
        while(true) {
            try {
                this.selector.select();
                Iterator keys = selector.selectedKeys().iterator();
                while(keys.hasNext()) {
                    SelectionKey key = (SelectionKey) keys.next();
                    if (key.isWritable()) {
                        //System.out.println("write");
                        write(key);
                    }
                    if (key.isReadable()) {
                        read(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
    }

    public void write(SelectionKey key){
        //System.out.println("Sent");
        byte[] payload = createPayload();

        String hash = sha1.SHA1FromBytes(payload);
        //System.out.println(hash);

        synchronized(hashes) {
            hashes.add(hash);
        }

        sendMessageToServer(payload);

        key.interestOps(SelectionKey.OP_READ);
        client.incrementSent();


        try {
            Thread.sleep(1000 / messageRate);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    private byte[] createPayload() {
        byte[] payload = new byte[8192];
        random.nextBytes(payload);
        return payload;
    }

    public void sendMessageToServer(byte[] payload){
        ByteBuffer byteBuffer = ByteBuffer.wrap(payload);
        byteBuffer.rewind();

        while(byteBuffer.hasRemaining()) {
            try {
                socketChannel.write(byteBuffer);
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public void read(SelectionKey key) throws IOException {
        ByteBuffer buff = ByteBuffer.allocate(40);
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int read = 0;
        while(buff.hasRemaining() && read != -1 ) {
            read = socketChannel.read(buff);
        }
        buff.rewind();
        String receivedHash = new String(buff.array());
        synchronized(hashes) {
            hashes.remove(receivedHash);
        }
        //System.out.println(hashes.size());
        key.interestOps(SelectionKey.OP_WRITE);
        client.incrementReceived();

    }
}
