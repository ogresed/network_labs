package message;

import java.util.UUID;

public class HelloMessage extends Message {
    public HelloMessage(String name) {
        buf = new byte[TYPE_AND_UUID + name.getBytes().length];
        //type of message
        buf[0] = 1;
        //make uuid
        uuid = UUID.randomUUID().toString();
        System.arraycopy(uuid.getBytes(), 0, buf, 1, 36);
        System.arraycopy(name.getBytes(), 0, buf, 37, name.getBytes().length);
        length = buf.length;
    }

    @Override
    public byte[] getBytes() {
        return buf;
    }

    @Override
    public int length() {
        return length;
    }

    public String getUUID() {
        return uuid;
    }
}
