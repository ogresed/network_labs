package message;

public class Message {
    static final int TYPE_AND_UUID = 37;
    byte[] buf;
    int length;
    String uuid;
        Message() {

        }

        public Message (byte[] bytes) {
            this.buf = bytes;
            this.length = bytes.length;
        }
        public  byte[] getBytes() {
            return buf;
        }
        public  int length() {
            return buf.length;
        }

        public  String getUUID() {
            return new String(buf, 1, 36);
        }
}
