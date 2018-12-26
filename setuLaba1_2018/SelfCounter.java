import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class SelfCounter implements Constants{
    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length < 1)
            return;
        String message = GetMessage.getMessage();
        byte [] buf = new byte[19];

        InetAddress group = InetAddress.getByName(args[0]);
        MulticastSocket sendMulticsSocket = new MulticastSocket(DEFAULT_PORT);
        MulticastSocket receiveMulticsSocket = new MulticastSocket(DEFAULT_PORT);
        receiveMulticsSocket.joinGroup(group);

        DatagramPacket sendPacket = new DatagramPacket(message.getBytes(), message.length(), group, DEFAULT_PORT);
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

        Map<String, Long> map = new HashMap<>();
        long time = 0;
        while(true) {
            long currentTime = System.currentTimeMillis();
            if(time < currentTime) {
                sendMulticsSocket.send(sendPacket);
                time = currentTime + (4 * DEFAULT_TIMEOUT);
            }
            receiveMulticsSocket.setSoTimeout((DEFAULT_TIMEOUT * 4)/10);

            try {
                receiveMulticsSocket.receive(receivePacket);
            }
            catch (SocketTimeoutException ignored) {
            }

            map.entrySet().removeIf(entry -> {
                if(8 * DEFAULT_TIMEOUT < currentTime - entry.getValue()) {
                    System.out.println("App "+entry.getKey()+" leave");
                    System.out.println((map.size() - 1) + " alive apps");
                    return true;
                }
                return false;
            });
            String tempKey = new String(buf);
            if(map.containsKey(tempKey))
                map.replace(tempKey, currentTime);
            else {
                map.put(tempKey, currentTime);
                System.out.println("New app: "+ tempKey);
                System.out.println(map.size() + " alive apps");
                Thread.sleep(DEFAULT_TIMEOUT);
            }
        }
    }
}