package us;

public class Answers {
    public static final String UNKNOWN_METHOD =  "HTTP 405 unknown method\r\n";
    public static final String BAD_REQUEST =  "HTTP 400 bad request\r\n";
    public static final String NAME_EXIST = "HTTP 401\r\n" +
            "WWW-Authenticate: Token realm='Username is already in use'\r\n";
    public static final String OK = "HTTP 200 OK\r\n";
    public static final String UNKNOWN_TOKEN = "HTTP 403 unknown token\r\n";
    public static final String NOT_FOUND = "HTTP 404 id resource not found\r\n";

    public static final String OK_CODE = "200";
    public static final String BAD_REQEST_CODE = "400";
    public static final String N_EXIST_CODE = "401";
    public static final String UNKNOWN_TOKEN_CODE = "403";
    public static final String NOT_FOUND_CODE = "404";
}
