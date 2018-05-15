package com.symmetrylabs.slstudio.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;

public class OpcSocket implements Closeable {
    public static final int DEFAULT_PORT = 7890;
    public final InetSocketAddress address;

    public static abstract class Callback {
        void receive(InetAddress src, OpcMessage reply) { }
    }

    protected DatagramSocket socket = null;
    protected static ExecutorService executor = NetworkManager.getInstance().getExecutor();
    protected final DatagramPacket reply = new DatagramPacket(new byte[65535], 65535);

    OpcSocket(InetAddress host, int port) {
        address = new InetSocketAddress(host, port);
        try {
            socket = new DatagramSocket();
        } catch (IOException e) {
            socket = null;
        }
    }

    public OpcSocket(InetAddress addr) {
        this(addr, DEFAULT_PORT);
    }

    /** Sends a message without listening for a reply. */
    public void send(final OpcMessage message) {
        if (socket == null) {
            throw new IllegalStateException("Socket is no longer available");
        }
        final DatagramSocket s = socket;
        final DatagramPacket packet = new DatagramPacket(message.bytes, message.bytes.length, address);
        executor.submit(new Runnable() {
            public void run() {
                try {
                    s.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Sends a message, waiting in the background for up to timeoutMillis for
     * a single reply, which will be passed to callback.receive().  After a
     * reply is received or the timeout expires, callback.finish() is invoked.
     */
    public void listen(final int timeoutMillis, final Callback callback) {
        listen(timeoutMillis, false, callback);
    }

    /**
     * Sends a message, waiting in the background for up to timeoutMillis for
     * any number of replies; callback.receive() is invoked for each reply.
     * After the timeout expires, callback.finish() is invoked.
     */
    public void listenMultiple(int timeoutMillis, Callback callback) {
        listen(timeoutMillis, true, callback);
    }

    /** Relinquishes any resources that this OpcSocket is holding. */
    public void close() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    protected void listen(final int timeoutMillis, final boolean allowMultipleReplies, final Callback callback) {
        if (socket == null) {
            throw new IllegalStateException("Socket is no longer available");
        }
        final DatagramSocket s = socket;
        socket = null;  // relinquish responsibility for closing the socket
        executor.submit(new Runnable() {
            public void run() {
                try {
                    try {
                        s.setSoTimeout(timeoutMillis);
                        do {
                            s.receive(reply);  // throws SocketTimeoutException upon a timeout
                            callback.receive(reply.getAddress(), new OpcMessage(reply.getData()));
                        } while (allowMultipleReplies);
                    } finally {
                        s.close();
                    }
                } catch (SocketTimeoutException e) {
                    // ignore; no need to print the stack trace
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
