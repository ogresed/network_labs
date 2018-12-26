package message;

public class QuitMessage extends Message  {

    public QuitMessage() {
        buf = new byte[1];
        buf[0] = 5;
    }
    @Override
    public byte[] getBytes() {
        return buf;
    }

    @Override
    public int length() {
       return  1;
    }

    @Override
    public String getUUID() {
        return null;
    }
}
