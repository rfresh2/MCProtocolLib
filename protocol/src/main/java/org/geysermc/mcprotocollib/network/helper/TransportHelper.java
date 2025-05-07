package org.geysermc.mcprotocollib.network.helper;

import io.netty.channel.epoll.Epoll;

public class TransportHelper {
    public enum TransportMethod {
        NIO, EPOLL, IO_URING
    }

    public static TransportMethod determineTransportMethod() {
        // iouring disabled as i dont think it gives better performance for small scale traffic
//        if (isClassAvailable("io.netty.channel.uring.IoUring") && IoUring.isAvailable()) return TransportMethod.IO_URING;
        if (isClassAvailable("io.netty.channel.epoll.Epoll") && Epoll.isAvailable()) return TransportMethod.EPOLL;
        return TransportMethod.NIO;
    }

    /**
     * Used so implementations can opt to remove these dependencies if so desired
     */
    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
