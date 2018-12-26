import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client implements Constants{
    public static void main(String[] args) {
        //check arguments
        if(args.length != 3) {
            System.out.println("Set name of file, then host and port");
            return;
        }

        try {
            //load arguments
            String nameOfFile = args[0];
            InetAddress host = InetAddress.getByName(args[1]);
            int port = Integer.parseInt(args[2]);
            File file = new File(nameOfFile);
            FileInputStream fileInputStream = new FileInputStream(file);

            Socket socket = new Socket(host, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            //send metadata
            dataOutputStream.writeUTF(WorkWithFile.nameParser(nameOfFile));//name of file
            long sizeOfFile = file.length();
            dataOutputStream.writeLong(sizeOfFile);//length of file
            //check creating file in server
            String messageFromServer = dataInputStream.readUTF();
            //'P' - first letter of "Problem with creating..." sent from Server
            if(messageFromServer.getBytes()[0] == 'P') {
                System.out.println("Impossible send file. Server disconnected");
                //free resources
                fileInputStream.close();
                socket.close();
                dataInputStream.close();
                dataOutputStream.close();
                return;
            }
            System.out.println(messageFromServer);
            //send file
            int n;
            int totalRead = 0;
            byte[] fileBuf = new byte[BUFFER_SIZE];

            while ((n = fileInputStream.read(fileBuf, 0, BUFFER_SIZE)) > 0) {
                totalRead += n;
                dataOutputStream.write(fileBuf, 0, n);
                dataOutputStream.flush();
            }
            if(totalRead == sizeOfFile) {
                System.out.println("File successfully sent");
            }
            else
                System.out.println("Problem with sending file");
            //free resources
            fileInputStream.close();
            socket.close();
            dataInputStream.close();
            dataOutputStream.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        catch (NumberFormatException e) {
            System.out.println("Wrong port");
        }
        catch (IOException e) {
            System.out.println("Connection broken");
        }
    }
}
