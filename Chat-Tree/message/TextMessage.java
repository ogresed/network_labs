package message;

import java.util.UUID;

public class TextMessage extends Message {
    public TextMessage(String text) {
        uuid = UUID.randomUUID().toString();
        byte[] textBytes = text.getBytes();
        int lenOfText = textBytes.length;
        buf = new byte[TYPE_AND_UUID + lenOfText];
        //type of text message
        buf[0] = 2;
        //insert uuid in buf
        System.arraycopy(uuid.getBytes(), 0, buf, 1, 36);
        System.arraycopy(textBytes, 0, buf, 37, lenOfText);
        length = buf.length;
    }

    public TextMessage(byte [] bytes) {
        buf = new  byte[bytes.length];
        System.arraycopy(bytes, 0, buf, 0, bytes.length);
        this.length = bytes.length;
        uuid = new String(bytes, 1, 36);
    }

    @Override
    public byte[] getBytes() {
        return buf;
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public String getUUID() {
        return uuid;
    }
}
