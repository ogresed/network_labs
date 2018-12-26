package user;

import us.Online;

import java.util.UUID;

public class User {
    private String token;
    private UserWithoutToken user;


    public User(UserWithoutToken user) {
        this.user = user;
        token = UUID.randomUUID().toString();
    }

    public int getId() {
        return user.getId();
    }

    public void setOnline(Online online) {
        user.setOnline(online);
    }

    public Online isOnline() {
        return user.isOnline();
    }

    public String getName() {
        return user.getName();
    }

    public String getToken() {
        return token;
    }

    public UserWithoutToken getUserWithoutToken() {
        return user;
    }
}
