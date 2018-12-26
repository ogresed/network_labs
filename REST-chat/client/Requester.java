package client;

import com.google.gson.Gson;
import mess.Message;
import mess.MessageIA;
import us.Online;
import user.User;
import user.UserName;
import user.UserWithoutToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import static us.Answers.*;

class Requester {
    private  String host;
    private  int port;
    private static final String C_TYPE = "Content-Type: application/json";
    private static final String AUTH = "Authorization: Token ";
    private static final Gson gson = new Gson();
    private String [] response;
    private String[] httpStatus;
    private Timer reqCurrMess  = new Timer();

    private static int historyOffset;

    Requester (String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Socket sendRequest(byte[] request) throws IOException {
        Socket socket = new Socket(host, port);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write(request);
        dataOutputStream.flush();
        return socket;
    }

    private void receiveResponse(Socket socket) throws IOException {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            byte[] buf = new byte[4096];
            int totalRead = dataInputStream.read(buf);
            response = new String(buf, 0, totalRead).split("\r\n");

            httpStatus = response[0].split(" ");
        } catch (StringIndexOutOfBoundsException ignore) {

        }
    }

    boolean login(String name) throws IOException {
        UserName myName  = new UserName(name);
        String jsonString = gson.toJson(myName);
        Socket socket = sendRequest(("POST /login\r\n" +
                C_TYPE + "\r\n\r\n" + jsonString).getBytes());

        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            User me = gson.fromJson(response[3], User.class);
            Client.setToken(me.getToken());
            Client.setId(me.getId());

            //request history of messages
            historyOffset = requestHistory(-1, 100);
            //run timer task to requesting current messages
            reqCurrMess.scheduleAtFixedRate(new RequesterMessages(), 1, 2000);
            return true;
        }
        else if(N_EXIST_CODE.equals(httpStatus[1])) {
            System.out.println(name+": name is busy");
        }
            else  {
            System.out.println(response[0]);
        }
        return false;
    }

    private int requestHistory(int offset, int count) throws IOException {
        Socket socket = sendRequest(("GET /messages?offset=" + String.valueOf(offset)+"&count="+String.valueOf(count)
        +"\r\n" + AUTH + Client.getToken()).getBytes());
        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            MessageIA[] messageIAs = gson.fromJson(response[3], MessageIA[].class);
            for(MessageIA ia : messageIAs) {
                System.out.println(ia.getAuthor() + ": "+ ia.getMessage());
            }
         return messageIAs.length;
        }
        else {
            //System.out.println(response[0] + "qwerty");
        }
        return -1;
    }

    void logout() throws IOException {
        reqCurrMess.cancel();

        Socket socket = sendRequest(("POST /logout\r\n" + AUTH + Client.getToken()).getBytes());
        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            //Message message = gson.fromJson(response[3], Message.class);
            //System.out.println(message.getMessage());
        }
        else {
            System.out.println(response[0]);
        }
    }

    void users() throws IOException {
        Socket socket = sendRequest(("GET /users\r\n" + AUTH + Client.getToken()).getBytes());
        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            UserWithoutToken[] users = gson.fromJson(response[3], UserWithoutToken[].class);
            for(UserWithoutToken userWithoutToken : users) {
                System.out.println(userWithoutToken.getName()+"     id: "+
                    userWithoutToken.getId()+"      online: "+ userWithoutToken.isOnline());
            }
        }
        else {
            System.out.println(response[0]);
        }
    }

    void users(String id) throws IOException {
        Socket socket = sendRequest(("GET /users/"+id + "\r\n" + AUTH + Client.getToken()).getBytes());
        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            UserWithoutToken user = gson.fromJson(response[3], UserWithoutToken.class);
                System.out.println(user.getName()+"     id: "+
                        user.getId()+"      online: "+ user.isOnline());
        }
        else if(NOT_FOUND_CODE.equals(httpStatus[1])) {
            System.out.println("No user with id "+ id);

        }
        else {
            System.out.println(response[0]);
        }
    }



    void logout(String oldToken) throws IOException {
        Socket socket = sendRequest(("POST /logout\r\n" + AUTH + oldToken).getBytes());
        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            Message message = gson.fromJson(response[3], Message.class);
            System.out.println(message.getMessage());
        }
        else {
            System.out.println(response[0]);
        }
    }

    void list() throws IOException{
        Socket socket = sendRequest(("GET /users\r\n" + AUTH + Client.getToken()).getBytes());
        receiveResponse(socket);

        if(OK_CODE.equals(httpStatus[1]) && C_TYPE.equals(response[1])) {
            UserWithoutToken[] users = gson.fromJson(response[3], UserWithoutToken[].class);
            for(UserWithoutToken userWithoutToken : users) {
                if(userWithoutToken.isOnline() == Online.TRUE) {
                    System.out.println(userWithoutToken.getName() + "     id: " +
                            userWithoutToken.getId());
                }
            }
        }
        else {
            System.out.println(response[0]);
        }
    }

    void sendMessage(String messageString) throws IOException {
        Message message = new Message(messageString);
        String jsonString = gson.toJson(message);
        Socket socket = sendRequest(("POST /messages\r\n" + AUTH + Client.getToken() +"\r\n" +
                C_TYPE + "\r\n\r\n" + jsonString).getBytes());
        receiveResponse(socket);
        if(!OK_CODE.equals(httpStatus[1]))
            System.out.println(response[0]);
        /*
        switch (httpStatus[1]) {
            //case ""
        }*/
    }

    private class RequesterMessages extends TimerTask {
        @Override
        public void run() {
            try {
                int tmp;
                tmp = requestHistory(historyOffset + 1, 100);
                historyOffset+=tmp;
            } catch (IOException e) {
                System.out.println("Impossible connect with server");
            }
        }
    }
}
