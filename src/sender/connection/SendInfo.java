package sender.connection;

import com.sun.istack.internal.Nullable;

import java.net.InetSocketAddress;

public class SendInfo {
    @Nullable
    public final InetSocketAddress address;
    public final byte[] data;

    public final Runnable failListener;

    public SendInfo(InetSocketAddress address, byte[] data, Runnable failListener) {
        this.address = address;
        this.data = data;
        this.failListener = failListener;
    }
}
