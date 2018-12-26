package message;

public class AcknowledgeMessage  extends Message {
    public AcknowledgeMessage(String uuid) {
        this. uuid = uuid;
        makeAnswer(TYPE_AND_UUID);
        length = buf.length;
    }

    public AcknowledgeMessage(String uuid, String myName) {
        this. uuid = uuid;
        byte[] name;
        name = myName.getBytes();
        makeAnswer(TYPE_AND_UUID + name.length);
        System.arraycopy(name, 0, buf, TYPE_AND_UUID, name.length);
        length = buf.length;
    }

    private void makeAnswer(int sizeOfBuf) {
        buf = new byte[sizeOfBuf];
        //type of message
        buf[0] = 4;
        //make uuid
        System.arraycopy(uuid.getBytes(), 0, buf, 1, 36);
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
