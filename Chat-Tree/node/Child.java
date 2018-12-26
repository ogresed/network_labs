package node;

import message.HelloMessage;
import message.Message;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class Child extends Node {
    private String parentHost;
    private int parentPort;

    public Child (String myName, int lossPercentage, int myPort, String parentHost, int parentPort) {
        super(myName, lossPercentage, myPort);
        this.parentHost = parentHost;
        this.parentPort = parentPort;
    }

    @Override
    public void start() {
        try {
            this.datagramSocket = new DatagramSocket(myPort);
            Message helloMessage = new HelloMessage(myName);
            send(helloMessage, new InetSocketAddress(parentHost, parentPort), true);
            neighbours.put(new InetSocketAddress(parentHost, parentPort), System.currentTimeMillis());
            runThreads();
        } catch (SocketException e) {
            System.out.println("Impossible create socket");
        }
    }
}
