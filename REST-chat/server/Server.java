package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class Server {


    private static final long TIME_TO_CHECK_LIVE = 2000;
    private static final long TIME_TO_DELETE_OLDS = 5 * 1000 * 60;// 5 min

    public static void main(String[] args) {
        ServerEntity entity = new ServerEntity();
        Timer checkAlive = new Timer();
        checkAlive.scheduleAtFixedRate(new CheckAlive(entity), 1, TIME_TO_CHECK_LIVE);
        Timer deleteOlds = new Timer();
        deleteOlds.scheduleAtFixedRate(new DeleteOlds(entity), 1, TIME_TO_DELETE_OLDS);

        try {
            ServerSocket server = new ServerSocket(1025);
            while (true) {
                try {
                    Socket socket = server.accept();
                    new Thread (new Worker(socket, entity)).start();
                } catch (IOException ignore) {
                }
            }
        } catch (IOException e) {
            System.out.println("Impossible create server socket");
        }
    }

    private static class CheckAlive extends TimerTask {
        private static ServerEntity entity;

        CheckAlive(ServerEntity entity) {
            CheckAlive.entity = entity;
        }

        @Override
        public void run() {
            entity.checkAlive();
        }
    }

    private static class DeleteOlds extends TimerTask {
        private static ServerEntity entity;
        DeleteOlds(ServerEntity entity) {
            DeleteOlds.entity = entity;
        }

        @Override
        public void run() {
            entity.deleteOlds();
        }
    }
}
