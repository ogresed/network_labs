package client;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    private static String token = null;
    private static int id = -1;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Requester requester = new Requester("localhost", 1025);
        boolean work = true;
        String m;
        String[] line;
        while (work) {
            m = scanner.nextLine();
            line = m.split(" ");
            try {
                    try {
                        switch (line[0]) {
                            case "/login": {
                                if (line.length < 2) {
                                    System.out.println("enter name");
                                } else if (token != null) {
                                    String oldToken = token;
                                    if(requester.login(line[1])) {
                                        requester.logout(oldToken);
                                    }
                                } else {
                                    requester.login(line[1]);
                                }
                                break;
                            }
                            case "/logout": {
                                work = false;
                                requester.logout();
                                break;
                            }
                            case "/users": {
                                if(line.length < 2)
                                    requester.users();
                                else
                                    requester.users(line[1]);//users <id>
                                break;
                            }
                            case "/list": {
                                requester.list();
                                break;
                            }
                            case "/help": {
                                System.out.println(
                                        "login : /login <some name>\n" +
                                        "logout : /logout\n" +
                                        "info about all users : /users\n" +
                                        "info about user with id <id> : /user <id>\n" +
                                        "get online users : /list\n" +
                                                "send message : just enter message");
                                break;
                            }
                            default: {
                                requester.sendMessage(m);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Impossible connect with server");
                    }
            } catch (StringIndexOutOfBoundsException ignore) {
            }
        }
    }

    public static int getId() {
        return id;
    }

    static String getToken() {
        return token;
    }

    static void setToken(String token) {
        Client.token = token;
    }

    static void setId(int id) {
        Client.id = id;
    }
}
