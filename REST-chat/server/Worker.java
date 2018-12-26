package server;

import com.google.gson.Gson;
import mess.Message;
import mess.MessageD;
import mess.MessageIA;
import user.User;
import user.UserName;
import user.UserWithoutToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import static us.Answers.*;

public class Worker implements Runnable {
    private static final String C_TYPE = "Content-Type: application/json";
    private static final String AUTH = "Authorization:";
    private static final String TOKEN = "Token";
    private static Socket socket;
    private static DataOutputStream dataOutputStream;
    private static ServerEntity entity;
    private static String[] fields;
    private static final Gson gson = new Gson();

    Worker(Socket socket, ServerEntity entity) {
        Worker.socket = socket;
        Worker.entity = entity;
    }

    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            byte[] buf = new byte[4096];
            int totalRead = dataInputStream.read(buf);
            String request = new String(buf, 0, totalRead);
            fields = request.split("\r\n");
            String[] method = fields[0].split(" ");

            if(method.length < 2)
                return;

            if("POST".equals(method[0])) {
                postHandler(method[1]);
                return;
            }

            if("GET".equals(method[0])) {
                getHandler(method[1]);
                return;
            }

            answer(UNKNOWN_METHOD);

        } catch (IOException ignore) {
        }
    }

    private void answer(String httpAnswer) throws IOException {
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write(httpAnswer.getBytes());
        dataOutputStream.flush();
        dataOutputStream.close();
    }

    private void getHandler(String uri) throws IOException {
        String [] u = uri.split("/");
        try {
            if(u.length == 3) {
                if("users".equals(u[1])) {
                    usersHandler(u[2]);
                    return;
                }
            }
            else if(u.length == 2) {
                if ("users".equals(u[1])) {
                    usersHandler();
                    return;
                }
                else {

                    String[] messUri = u[1].split( "\\?" );
                    if("messages".equals(messUri[0])) {
                        String[] param = messUri[1].split("&");
                        Map<String, String> par = new HashMap<>();
                        par.put(param[0].split("=")[0], param[0].split("=")[1]);
                        par.put(param[0].split("=")[1], param[1].split("=")[1]);
                        int offset = Integer.parseInt(par.get("offset"));
                        int count = Integer.parseInt(par.get("count"));
                        getMessageHandler(offset, count);
                        return;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ignore) {
        }
        answer(BAD_REQUEST);
    }

    private void getMessageHandler(int offset, int count) throws IOException {
        String[] auth = fields[1].split(" ");
        if(AUTH.equals(auth[0]) && TOKEN.equals(auth[1])) {
            if(entity.tokenExists(auth[2])) {
                if(offset == -1) {
                    int mSize = entity.getHistorySize() >= 100 ? 100 : entity.getHistorySize();
                    MessageIA[] messageIAS = new MessageIA[mSize];
                    for(int i = 0; i < mSize; i++) {
                        messageIAS[i] = entity.getMessage(entity.getHistorySize() - mSize + i);
                    }
                    entity.timeUpdate(auth[2]);
                    String jsonString = gson.toJson(messageIAS);
                    sendOk(jsonString);
                }
                else {
                    int mSize = offset + count > entity.getHistorySize() ? Math.abs(entity.getHistorySize() - offset) : count;
                    MessageIA[] messageIAS = new MessageIA[mSize];
                    for(int i = 0; i < mSize; i++) {
                        messageIAS[i] = entity.getMessage(offset + i);
                    }
                    String jsonString = gson.toJson(messageIAS);
                    sendOk(jsonString);
                }
            }
            else {
                answer(UNKNOWN_TOKEN);
            }
        }

    }

    private void usersHandler(String id) throws IOException {
        String[] auth = fields[1].split(" ");
        if(AUTH.equals(auth[0]) && TOKEN.equals(auth[1]) && entity.tokenExists(auth[2])) {
            int ID = Integer.parseInt(id);
            UserWithoutToken user = entity.getUserByID(ID);
            if(user == null) {
                answer(NOT_FOUND);
            }
            else {
                String jsonString = gson.toJson(user);
                entity.timeUpdate(auth[2]);
                sendOk(jsonString);
            }
        }
    }

    private void usersHandler() throws IOException {
        String[] auth = fields[1].split(" ");

        if(AUTH.equals(auth[0]) && TOKEN.equals(auth[1])) {
            if(entity.tokenExists(auth[2])) {
                String jsonString = gson.toJson(entity.getUsers());
                entity.timeUpdate(auth[2]);
                sendOk(jsonString);
            }
            else {
                answer(UNKNOWN_TOKEN);
            }
        }
    }

    private void postHandler(String uri) throws IOException {
        try {
            if ("/login".equals(uri)) {
                loginHandler();
                return;
            }

            if ("/logout".equals(uri)) {
                logoutHandler();
                return;
            }
            if("/messages".equals(uri)) {
                messageHandler();
                return;
            }
            //кидается исключение если строчек в запросе меньше чем положено по протоколу - плохой запрос
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        answer(BAD_REQUEST);
    }

    private void logoutHandler() throws IOException {
        String[] auth = fields[1].split(" ");
        if(AUTH.equals(auth[0]) && TOKEN.equals(auth[1])) {
            if(entity.rmUser(auth[2])) {
                sendOk(gson.toJson(new Message("bye!")));
            }
            else {
                answer(UNKNOWN_TOKEN);
            }
        }
    }

    private void loginHandler() throws IOException {
        if(C_TYPE.equals(fields[1]) && "".equals(fields[2])) {
            UserName name = gson.fromJson(fields[3], UserName.class);
            if(entity.nameExist(name.userName)) {
                answer(NAME_EXIST);
                return;
            }
            User user =  entity.addUser(name.userName);
            String jsonString = gson.toJson(user);
            sendOk(jsonString);
        }
        else {
            answer(BAD_REQUEST);
        }
    }


    private void messageHandler() throws IOException {
        String[] auth = fields[1].split(" ");
        if(AUTH.equals(auth[0]) && TOKEN.equals(auth[1]) &&
                C_TYPE.equals(fields[2]) && "".equals(fields[3])) {
            String author = entity.getNameByToken(auth[2]);
            if(author == null) {
                answer(UNKNOWN_TOKEN);
                return;
            }
            Message message = gson.fromJson(fields[4], Message.class);
            int ID = entity.putMessage(message.getMessage(), author);

            String jsonString = gson.toJson(new MessageD(message.getMessage(), ID));
            entity.timeUpdate(auth[2]);
            sendOk(jsonString);
        }
            answer(BAD_REQUEST);
    }

    private void sendOk(String jsonString) throws IOException {
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        String response = OK + C_TYPE + "\r\n\r\n" + jsonString;
        dataOutputStream.write(response.getBytes());
        dataOutputStream.flush();
        dataOutputStream.close();
    }
}
