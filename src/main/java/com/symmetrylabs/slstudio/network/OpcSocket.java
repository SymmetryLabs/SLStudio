package com.symmetrylabs.slstudio.network;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A socket that can send and receive OPC messages.  The supported operations
 * are send(), listen(), listenMultiple(), and close().  send() is non-terminal;
 * the other operations are terminal.  To use an OpcSocket, acquire it with a
 * try-with-resources block; within the block, call send() any number of times,
 * then optionally call listen() or listenMultiple() exactly once.
 */
public class OpcSocket implements Closeable {
    public static final int DEFAULT_PORT = 7890;
    public final InetSocketAddress address;

    interface Callback {
        void receive(InetAddress src, OpcMessage reply);
    }

    protected final ExecutorService executor = Executors.newSingleThreadExecutor();
    protected DatagramSocket socket = null;

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

    /** Sends a message asynchronously. */
    public void send(final OpcMessage message) {
        requireSocket();
        executor.submit(new SendTask(
              socket, new DatagramPacket(message.bytes, message.bytes.length, address)));
    }

    /**
     * Waits in the background for up to timeoutMillis for a single reply,
     * which will be passed to callback.receive() on the background thread.
     * No further operations may be performed on this OpcSocket; the background
     * thread takes ownership of the socket and will automatically close it
     * after a reply is received or the timeout expires.
     */
    public void listen(final int timeoutMillis, final Callback callback) {
        requireSocket();
        executor.submit(new ListenTask(socket, timeoutMillis, false, callback));
        socket = null;
    }

    /**
     * Waits in the background for up to timeoutMillis for any number of
     * replies; callback.receive() is invoked on the background thread for each
     * reply.  No further operations may be performed on this OpcSocket; the
     * background thread takes ownership of the socket and will automatically
     * close it after the timeout expires.
     */
    public void listenMultiple(int timeoutMillis, Callback callback) {
        requireSocket();
        executor.submit(new ListenTask(socket, timeoutMillis, true, callback));
        socket = null;
    }

    /**
     * Relinquishes any resources that this OpcSocket is holding.  Idempotent.
     * No further operations may be performed on this OpcSocket.
     */
    public void close() {
        if (socket != null) {
            executor.submit(new CloseTask(socket));
            socket = null;
        }
    }

    protected void requireSocket() {
        if (socket == null) {
            throw new IllegalStateException("Socket is already closed");
        }
    }

    protected static class SendTask implements Runnable {
        final DatagramSocket socket;
        final DatagramPacket packet;

        SendTask(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;
        }

        public void run() {
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static class ListenTask implements Runnable {
        final DatagramSocket socket;
        final int timeoutMillis;
        final boolean allowMultipleReplies;
        final Callback callback;
        final DatagramPacket reply = new DatagramPacket(new byte[65535], 65535);

        ListenTask(DatagramSocket socket, int timeoutMillis, boolean allowMultipleReplies, Callback callback) {
            this.socket = socket;
            this.timeoutMillis = timeoutMillis;
            this.allowMultipleReplies = allowMultipleReplies;
            this.callback = callback;
        }

        public void run() {
            try {
                try {
                    socket.setSoTimeout(timeoutMillis);
                    do {
                        socket.receive(reply);  // throws SocketTimeoutException upon a timeout
                        callback.receive(
                            reply.getAddress(), new OpcMessage(reply.getData(), reply.getLength()));
                    } while (allowMultipleReplies);
                } finally {
                    socket.close();
                }
            } catch (SocketTimeoutException e) {
                // ignore; no need to print the stack trace
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static class CloseTask implements Runnable {
        final DatagramSocket socket;

        CloseTask(DatagramSocket socket) {
            this.socket = socket;
        }

        public void run() {
            socket.close();
        }
    }
}
