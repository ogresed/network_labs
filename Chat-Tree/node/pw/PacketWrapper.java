package node.pw;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class PacketWrapper {
    private ByteBuffer byteBuffer;
    private InetSocketAddress address;
    private int numberOfResending = 0;

    public PacketWrapper(ByteBuffer byteBuffer, InetSocketAddress address) {
        this.byteBuffer = byteBuffer;
        this.address = address;
    }
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null || getClass() != obj.getClass())
            return false;
        PacketWrapper tmp = (PacketWrapper) obj;
        return byteBuffer.equals(tmp.byteBuffer) && address.equals(tmp.address);
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void incrementNumberOfResending() {
        numberOfResending++;
    }

    public int getNumberOfResending() {
        return numberOfResending;
    }
}