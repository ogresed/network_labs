package server;
import mess.MessageIA;
import user.User;
import user.UserWithoutToken;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static us.Online.NULL;

class ServerEntity {
    private static final long TIME_TO_LIVE = 30 * 10000;//30 SEC
    private static final long TIME_TO_DELETE = 5 * 60 * 1000;// 5 min

    private static ConcurrentHashMap<User, Long> users;
    private static CopyOnWriteArrayList<UserWithoutToken> uwt;
    private static CopyOnWriteArrayList<MessageIA> history;

    ServerEntity () {
        users = new ConcurrentHashMap<>();
        uwt = new CopyOnWriteArrayList<>();
        history = new CopyOnWriteArrayList<MessageIA>();
    }

    boolean nameExist(String userName) {
        for(UserWithoutToken user : uwt)
            if(userName.equals(user.getName())) {
                return true;
            }
        return false;
    }

    int getHistorySize () {
        return history.size();
    }

    CopyOnWriteArrayList<UserWithoutToken> getUsers() {
        return uwt;
    }

    boolean rmUser(String token) {
        return users.entrySet().removeIf(entry-> token.equals(entry.getKey().getToken())
                && uwt.remove(entry.getKey().getUserWithoutToken()));

    }

    void checkAlive() {
        long currentTime = System.currentTimeMillis();
        users.forEach((user, time) -> {
            if(currentTime - time > TIME_TO_LIVE) {
                user.setOnline(NULL);
            }
        });
    }

    boolean tokenExists(String token) {
        for(User user : users.keySet())
            if(token.equals(user.getToken())) {
                return true;
            }
        return false;
    }

    User addUser(String userName) {
        UserWithoutToken user =  new UserWithoutToken(userName);
        uwt.add(user);

        User u = new User(user);
        users.put(u, System.currentTimeMillis());
        return u;
    }

    UserWithoutToken getUserByID(int id) {
        UserWithoutToken user = null;
        for (UserWithoutToken userWithoutToken : uwt) {
            if(userWithoutToken.getId() == id) {
                user = userWithoutToken;
                break;
            }
        }
        return user;
    }

    void deleteOlds() {
        long current_time = System.currentTimeMillis();
        users.entrySet().removeIf(entry -> current_time - entry.getValue() > TIME_TO_DELETE);
    }

    int putMessage(String message, String author) {
        MessageIA messageIA = new MessageIA(message, author);
        System.out.println(author+": "+message + "  "+messageIA.getID());
        history.add(messageIA);
        return messageIA.getID();
    }

    String getNameByToken(String token) {
        String name = null;
        for (Map.Entry<User, Long> entry : users.entrySet()) {
            if(entry.getKey().getToken().equals(token)) {
                name = entry.getKey().getName();
            }
        }
        return name;
    }

    MessageIA getMessage(int index) {
        return history.get(index);
    }

    void timeUpdate(String token) {
        //update time by token
        for(Map.Entry<User, Long> entry : users.entrySet()) {
            if(token.equals(entry.getKey().getToken())) {
                users.replace(entry.getKey(), System.currentTimeMillis());
            }
        }
    }
}
