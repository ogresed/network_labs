import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Constants {
    public static void main(String[] args) {
    	//check argument
    	if (1 > args.length) {
    		System.out.println("Enter port");
    		return;
    	}
        //creating directory
        File dir = new File(nameOfDir);
        if(!dir.exists()) {
            if(!dir.mkdir()) {
                System.out.println("Impossible create directory");
                return;
            }
        }
        try {
            //parse port
            int port = Integer.parseInt(args[0]);
            //listening and connect
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                RunnableServer runnableServer = new RunnableServer(socket);
                new Thread(runnableServer).start();
            }
        }
        catch (NumberFormatException e) {
            System.out.println("Wrong port");
        }
        catch (IOException ee) {
            System.out.println("Problem with creating connection");
        }
    }
}
