import java.io.*;
import java.net.Socket;
import java.util.Timer;

public class RunnableServer implements Runnable, Constants {
    private Socket socket;
    private long totalRead = 0;

    RunnableServer (Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        receive(socket);
    }

    private void receive(Socket socket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            //receive metadata
            String nameOfFile = dataInputStream.readUTF();//get file's name
            long fileSize = dataInputStream.readLong();// get file's size
            //creating file
            File file = WorkWithFile.createFile(nameOfDir + nameOfFile);
            if (file == null) {
                System.out.println("Impossible create new file");
                dataOutputStream.writeUTF("Problem with creating new file. Impossible receive file");
                //free resources
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
                return;
            }
            if (!nameOfFile.equals(file.getName())) {
                nameOfFile = file.getName();
            }
            System.out.println("Created new file: '" + nameOfFile+"'");
            dataOutputStream.writeUTF("File '" + nameOfFile +"' successfully created.");
            //speed test
            Timer timer = new Timer();  
            timer.scheduleAtFixedRate(new SpeedMeter(this), 10, PERIOD_OF_OUTPUT_OF_SPEED);
            //make stream to file
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            //receive file
            int n;
            //average speed test
            long dispatchTime = System.currentTimeMillis();
            byte[] fileBuf = new byte[BUFFER_SIZE];
                while (totalRead < fileSize && (n = dataInputStream.read(fileBuf, 0, BUFFER_SIZE)) > 0) {
                    totalRead += n;
                    fileOutputStream.write(fileBuf, 0, n);
                    fileOutputStream.flush();
                }
                //output average speed
            dispatchTime = System.currentTimeMillis() - dispatchTime;
            System.out.println("time to send  - "+dispatchTime+" milliseconds");
            System.out.println("Average speed - " + Math.round (((double)(totalRead) / (double)dispatchTime))+
                " byte per millisecond");


            if (totalRead == fileSize)
                System.out.println("File '" + nameOfFile + "' successfully received");
            else {
                System.out.println("Problem with saving file");
            }
            //free resources
            timer.cancel();
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            fileOutputStream.close();
        }
        catch (IOException e) {
            System.out.println("Connection broken");
        }
        finally {
            System.out.println("========================================");
        }
    }

    synchronized long getTotalRead() {
        return totalRead;
    }
}
