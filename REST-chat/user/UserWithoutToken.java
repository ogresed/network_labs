package user;

import us.Online;

import static us.Online.TRUE;

public class UserWithoutToken {
    private static int idCounter = 0;

    private String name;
    private int id;
    private Online online;

    public UserWithoutToken(String name) {
        this.name = name;
        this.id = idCounter;
        idCounter++;
        this.online = TRUE;
    }

    public int getId() {
        return id;
    }

    public Online isOnline() {
        return online;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setOnline(Online online) {
        this.online = online;
    }

    void setId(int id) {
        this.id = id;
    }
}
