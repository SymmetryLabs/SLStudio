package com.symmetrylabs.slstudio.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpcSocket implements Closeable {
    public static final int DEFAULT_PORT = 7890;
    public final InetSocketAddress address;

    interface Callback {
        void receive(InetAddress src, OpcMessage reply);
    }

    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected DatagramSocket socket = null;
    protected final DatagramPacket reply = new DatagramPacket(new byte[65535], 65535);

    public OpcSocket(InetAddress host, int port) {
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
        final DatagramPacket packet = new DatagramPacket(message.bytes, message.bytes.length, address);
        executor.submit(() -> {
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
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
        executor.submit(() -> {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        });
    }

    protected void listen(final int timeoutMillis, final boolean allowMultipleReplies, final Callback callback) {
        if (socket == null) {
            throw new IllegalStateException("Socket is no longer available");
        }

        executor.submit(() -> {
            try {
                try {
                    socket.setSoTimeout(timeoutMillis);
                    do {
                        socket.receive(reply);  // throws SocketTimeoutException upon a timeout
                        callback.receive(reply.getAddress(), new OpcMessage(reply.getData()));
                    } while (allowMultipleReplies);
                } finally {
                    socket.close();
                    socket = null;
                }
            } catch (SocketTimeoutException e) {
                // ignore; no need to print the stack trace
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
