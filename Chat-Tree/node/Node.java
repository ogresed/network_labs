package node;

import message.*;
import node.pw.PacketWrapper;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Node {
    private static final int PERIOD_TO_RESENDING = 1000;
    private static final int PERIOD_TO_SEND_PING = 500;
    private static  final long TIME_TO_DEAF = 10000;
    private static final int BUF_SIZE = 2048;
    private static final int SIZE_OF_UUID = 36;
    private static final String NAME_OF_MESSENGER = "Сереграм";
    private static final String exitString = "мои полномочия всё";
    private static final int TIMEOUT_FOR_RECEIVING = 3000;
    private static final int NUMBER_OF_RESENDING = 1000;

    private static boolean work = true;
    int myPort;
    String myName;
    DatagramSocket datagramSocket;
    private final LinkedHashSet<String> messagesOnScreen;
    final Map<InetSocketAddress, Long> neighbours;
    private final LinkedList<PacketWrapper> waitingAcknowledgeMessages;
    private Timer pingTimer,
                    resending;
    private Random random;
    private int lossPercentage;

    public Node (String myName, int lossPercentage, int myPort) {
        this.myPort = myPort;
        this.waitingAcknowledgeMessages = new LinkedList<>();
        this.neighbours = new HashMap<>();
        this.pingTimer = new Timer();
        this.resending = new Timer();
        this.messagesOnScreen = new LinkedHashSet<>();
        this.myName = myName + ": ";
        this.random = new Random();
        this.lossPercentage = lossPercentage;
    }
    //run receiver, sender and ping sender
    public void start() {
        try {
            this.datagramSocket = new DatagramSocket(myPort);
            runThreads();
        } catch (SocketException e) {
            System.out.println("Impossible create socket");
        }
    }

    void runThreads() {
        //run receiver and sender
        new Thread(new Receiver()).start();
        new Thread(new Sender()).start();
        //run send ping
        pingTimer.scheduleAtFixedRate(new SendPingAndCheckNeighbours(), 1, PERIOD_TO_SEND_PING);
        resending.scheduleAtFixedRate(new Resending(), 1, PERIOD_TO_RESENDING);
    }
//resending messages
    class Resending extends TimerTask  {

    @Override
    public void run() {
        synchronized (waitingAcknowledgeMessages) {
            waitingAcknowledgeMessages.removeIf(packet -> {
               if(packet.getNumberOfResending() > NUMBER_OF_RESENDING)
                   return true;
               packet.incrementNumberOfResending();
               send(packet);
               return false;
            });
        }
    }
}
     class SendPingAndCheckNeighbours extends TimerTask {
        byte[] typeOfPingMessage;

        SendPingAndCheckNeighbours() {
            typeOfPingMessage = new byte[1];
            typeOfPingMessage[0] = 3;
        }
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            synchronized (neighbours) {
                neighbours.entrySet().removeIf(entry -> {
                    if (currentTime - entry.getValue() > TIME_TO_DEAF) {
                        System.out.println(entry.getKey() + " leave " + NAME_OF_MESSENGER + " by timeout");
                        return true;
                    }
                    return false;
                });

                neighbours.forEach((address, value) -> {
                    try {
                        datagramSocket.send(new DatagramPacket(typeOfPingMessage, 0, 1, address));
                    } catch (IOException e) {
                        System.out.println("Impossible send ping");
                    }
                });
            }
        }
    }
//send messages
    void send(Message message, InetSocketAddress address, boolean addToList) {
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), 0,
                message.length(), address);
        try {
            datagramSocket.send(datagramPacket);
            if(addToList) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(message.length());
                byteBuffer.put(message.getBytes(), 0, message.length());
                synchronized (waitingAcknowledgeMessages) {
                    waitingAcknowledgeMessages.add(new PacketWrapper(byteBuffer, address));
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible send helloMessage");
        }
    }
    private void send(PacketWrapper packet) {
        DatagramPacket datagramPacket = new DatagramPacket(packet.getByteBuffer().array(), 0,
                packet.getByteBuffer().array().length,
                packet.getAddress());
        try {
            datagramSocket.send(datagramPacket);
        }
        catch (IOException e) {
            System.out.println("Impossible send helloMessage");
        }
    }
//receive and parse all messages
    private class Receiver implements Runnable {
        byte[] receiveBuf;
        DatagramPacket datagramPacket;
        InetSocketAddress address;

        Receiver () {
            receiveBuf = new byte[BUF_SIZE];
            datagramPacket = new DatagramPacket(receiveBuf, 0,  BUF_SIZE);
        }
        @Override
        public void run() {
            while (work) {
                try {
                    datagramSocket.setSoTimeout(TIMEOUT_FOR_RECEIVING);
                    datagramSocket.receive(datagramPacket);
                    //imitation loss packet's
                    if(Math.abs(random.nextInt()) % 100 < lossPercentage)
                        continue;
                    //get address from who received message
                    address = new InetSocketAddress(datagramPacket.getAddress(), datagramPacket.getPort());
                    //get data
                        byte[] usefulBytes = new byte[datagramPacket.getLength()];
                        System.arraycopy(datagramPacket.getData(), 0, usefulBytes, 0, datagramPacket.getLength());
                        //parse
                        parser(usefulBytes, address);
                } catch (SocketTimeoutException ignore) {
                }
                catch (IOException e) {
                    System.out.println("Impossible receive packet");
                    work = false;
                }
            }
            //System.out.println("Receiver finished work");
            System.out.println("App closed");
        }
    }
//choose action with message
    private void parser(byte[] bytes, InetSocketAddress address) {

        byte typeOfMessage = bytes[0];
        switch (typeOfMessage) {
            //ping
            case 3: {
                //can add to neighbors without helloMessage if
                // do check contain address every time received ping

                //if key contains in map do put(key, value)
                synchronized (neighbours) {
                    if (neighbours.containsKey(address))
                        neighbours.put(address, System.currentTimeMillis());
                }
                break;
            }
            //text
            case 2: {
                //get uuid
                String uuid = new String(bytes, 1, 36);
                //if message received in first time
                synchronized (messagesOnScreen) {
                    if (!messagesOnScreen.contains(uuid)) {
                        messagesOnScreen.add(uuid);
                        //send acknowledgement
                        Message acknowledgeMessage = new AcknowledgeMessage(uuid);
                        send(acknowledgeMessage, address, false);
                        //get text
                        String textInString = new String(bytes, 37, bytes.length - 37);
                        System.out.println(textInString);
                        //send message all neighbours
                        Message textMessage = new TextMessage(bytes);
                        synchronized (neighbours) {
                            neighbours.forEach((host, value) -> send(textMessage, host, true));
                        }
                    }
                }
                break;
            }
            //acknowledge
            case 4: {
                ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
                byteBuffer.put(bytes, 0, bytes.length);
                PacketWrapper tmp = new PacketWrapper(byteBuffer, address);

                synchronized (waitingAcknowledgeMessages) {
                    waitingAcknowledgeMessages.remove(tmp);
                }
                break;
            }
            //hello
            case 1: {
                //add neighbour to list
                synchronized (neighbours) {
                    if (!neighbours.containsKey(address)) {
                        neighbours.put(address, System.currentTimeMillis());
                        System.out.println(address + "  joined " + NAME_OF_MESSENGER);
                        //send acknowledgement
                        String uuid = new String(bytes, 1, SIZE_OF_UUID);
                        Message acknowledgeMessage = new AcknowledgeMessage(uuid);
                        send(acknowledgeMessage, address, false);
                    }
                }
                break;
            }
            //quit
            case 5: {
                synchronized (neighbours) {
                    neighbours.remove(address);
                }
                System.out.println(address + "  leave "+ NAME_OF_MESSENGER);
                break;
            }
        }
    }
//send only user's messages
    private class Sender implements Runnable {
        Scanner scanner;
        String message;
        Sender() {
            scanner = new Scanner(System.in);
            message = null;
        }
        @Override
        public void run() {
            while (true) {
                    message = scanner.nextLine();
                    if ("exit".equals(message) || exitString.equals(message)) {
                        System.out.println("Completion...");
                        synchronized (neighbours) {
                            neighbours.forEach(((address, value) -> send(new QuitMessage(), address, false)));
                        }
                        pingTimer.cancel();
                        resending.cancel();
                        work = false;
                        break;
                    }
                    message = myName + message;
                    //make new message
                    Message textMessage = new TextMessage(message);
                    synchronized (messagesOnScreen) {
                        messagesOnScreen.add(textMessage.getUUID());
                    }
                       //send all neighbours
                    neighbours.forEach((address, value) -> send(textMessage, address, true));
            }
        }
    }
}
