package mess;

public class MessageIA {
    private static int idCounter = 0;

    private String message;
    private String author;
    private int id;

    public MessageIA(String message, String author) {
        this.author = author;
        this.message = message;
        id = idCounter;
        idCounter++;
    }

    public String getMessage() {
        return message;
    }


    public String getAuthor() {
        return author;
    }

    public int getID() {
        return id;
    }
}
