import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

class GetMessage {
    static String getMessage () throws UnknownHostException {
        String message = InetAddress.getLocalHost().getHostAddress();
        message += ". Key: ";
        int key;
        key = new Random().nextInt();
        key = key < 0 ? -key : key;
        key %= 1000;
        message+= String.valueOf(key);
        return message;
    }
}