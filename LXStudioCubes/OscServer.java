package heronarts.lx.osc;

import java.util.List;
import java.util.Deque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;
import heronarts.lx.osc.OscPacket;
import heronarts.lx.osc.OscBundle;

/**
 * OSC server using TCP sockets.
 */

public class OscServer {
        private static final Log log = LogFactory.getLog(OscServer.class);
        private static final int DEFAULT_BUFFER_SIZE = 8192;

        private final int bufferSize;
        private ServerSocket serverSocket;
        private OscServerThread serverThread;

        private List<ConnectionWatcher> connectionWatchers = new ArrayList<>();

        public OscServer(int port, int bufferSize) throws SocketException {
                this.bufferSize = bufferSize;

                try {
                        log.info("Starting OSC server on port " + port);
                        serverSocket = new ServerSocket(port);
                }
                catch (IOException e) {
                        log.error("Error while creating OSC server socket", e);
                }
        }

        public OscServer(int port) throws SocketException {
                this(port, DEFAULT_BUFFER_SIZE);
        }

        public OscServer start() {
                serverThread = new OscServerThread();
                serverThread.start();

                return this;
        }

        public OscServer stop() {
                if (serverThread != null) {
                        serverThread.shutdown();
                        serverThread = null;
                }

                return this;
        }

        public OscServer addConnectionWatcher(ConnectionWatcher w) {
                connectionWatchers.add(w);

                return this;
        }

        public static Connection directConnection(InetAddress address, int port, int bufferSize) throws IOException {

                return new Connection(address, port, bufferSize).setAutoReconnect(true).start();
        }

        public static Connection directConnection(InetAddress address, int port) throws IOException {
                return directConnection(address, port, DEFAULT_BUFFER_SIZE);
        }

        public static Connection directConnection(String host, int port, int bufferSize) throws IOException {
                return directConnection(InetAddress.getByName(host), port, bufferSize);
        }

        public static Connection directConnection(String host, int port) throws IOException {
                return directConnection(host, port, DEFAULT_BUFFER_SIZE);
        }

        public interface ConnectionWatcher {
                public void onConnection(Connection c);
        }

        public static class Connection implements Runnable {
                private static final int RECONNECT_DELAY_MS = 1000;

                private Socket socket;
                private InputStream input;
                private OutputStream output;
                private InetAddress address;
                private int port;
                private Thread thread;
                private final ByteBuffer inputBuffer, outputBuffer;
                private final List<LXOscListener> listeners = new ArrayList<>();
                private boolean running = true;
                private boolean autoReconnect = false;
                private boolean delayReconnect = false;

                private Connection(InetAddress address, int port, int bufferSize) throws IOException {
                        this(new Socket(address, port), bufferSize);
                }

                private Connection(Socket socket, int bufferSize) throws IOException {
                        this.socket = socket;

                        address = socket.getInetAddress();
                        port = socket.getPort();

                        input = socket.getInputStream();
                        output = socket.getOutputStream();

                        inputBuffer = ByteBuffer.allocate(bufferSize);
                        outputBuffer = ByteBuffer.allocate(bufferSize);
                }

                public InetAddress getAddress() {
                        return address;
                }

                public int getPort() {
                        return port;
                }

                public boolean getAutoReconnect() {
                        return autoReconnect;
                }
                public Connection setAutoReconnect(boolean autoReconnect) {
                        this.autoReconnect = autoReconnect;
                        return this;
                }

                public Connection addListener(LXOscListener listener) {
                        listeners.add(listener);
                        return this;
                }

                public Connection start() {
                        if (socket.isClosed() && autoReconnect) {
                                reconnect();
                        }

                        if (!socket.isClosed()) {
                                running = true;
                                thread = new Thread(this);
                                thread.start();
                        }

                        return this;
                }

                public void shutdown() {
                        running = false;

                        if (thread != null) {
                                thread.interrupt();
                        }

                        try {
                                socket.close();
                        }
                        catch (IOException e) {
                                log.error("Error while closing OSC client socket", e);
                        }
                }

                public boolean reconnect() {
                        if (delayReconnect) {
                                try { Thread.sleep(RECONNECT_DELAY_MS); }
                                catch (InterruptedException e) { /* pass */ }
                        }

                        try {
                                socket = new Socket(address, port);

                                input = socket.getInputStream();
                                output = socket.getOutputStream();

                                delayReconnect = false;

                                return true;
                        }
                        catch (IOException e) {
                                log.error("Error while reconnecting socket", e);
                                delayReconnect = true;
                        }

                        return false;
                }

                public void send(OscPacket packet) throws IOException {
                        if (socket.isClosed() && autoReconnect) {
                                reconnect();
                        }

                        if (socket.isClosed())
                                return;

                        synchronized (outputBuffer) {
                                outputBuffer.position(4);
                                packet.serialize(outputBuffer);
                                int packetSize = outputBuffer.position() - 4;
                                outputBuffer.putInt(0, packetSize);
                                //log.info("Sending message with size " + packetSize + " in buffer with size " + outputBuffer.position());
                                output.write(outputBuffer.array(), outputBuffer.arrayOffset(), outputBuffer.position());
                                outputBuffer.clear();
                        }
                        output.flush();
                }

                private List<OscMessage> getPacketMessages(OscPacket packet) {
                        Deque<OscPacket> packets = new LinkedList<>();
                        packets.add(packet);

                        List<OscMessage> messages = new ArrayList<>();
                        while (!packets.isEmpty()) {
                                OscPacket p = packets.removeFirst();
                                if (p instanceof OscMessage) {
                                        messages.add((OscMessage)p);
                                }
                                else if (p instanceof OscBundle) {
                                        packets.addAll(((OscBundle)p).getElements());
                                }
                        }

                        return messages;
                }

                @Override
                public void run() {
                        while (running) {
                                try {
                                        int r = input.read(inputBuffer.array(),
                                                                inputBuffer.arrayOffset() + inputBuffer.position(),
                                                                inputBuffer.remaining());

                                        // close client connection at end of stream
                                        if (r < 0) {
                                                break;
                                        }

                                        if (r == 0) {
                                                try { Thread.sleep(100); }
                                                catch (InterruptedException e) { /* pass */ }
                                                continue;
                                        }

                                        inputBuffer.position(inputBuffer.position() + r);

                                        int packetSize = inputBuffer.getInt(0);

                                        if (packetSize > inputBuffer.capacity() - 4) {
                                                log.error("Cannot read OSC message larger than input buffer, skipping message with size=" + packetSize);
                                                input.skip(packetSize - (inputBuffer.position() - 4));
                                                inputBuffer.clear();
                                                continue;
                                        }

                                        if (packetSize > inputBuffer.position() - 4)
                                                continue;

                                        //log.info("Processing message with size " + packetSize + " in buffer with size " + inputBuffer.position());

                                        if (packetSize == 0) {
                                                log.error("Skipping empty packet");
                                        }
                                        else {
                                                try {
                                                        OscPacket p = OscPacket.parse(socket.getInetAddress(), inputBuffer.array(),
                                                                        inputBuffer.arrayOffset() + 4, packetSize + 4);

                                                        List<OscMessage> messages = getPacketMessages(p);
                                                        for (LXOscListener listener : listeners) {
                                                                for (OscMessage m : messages) {
                                                                        listener.oscMessage(m);
                                                                }
                                                        }
                                                }
                                                catch (OscException e) {
                                                        log.error("Caught exception while parsing OSC packet", e);

                                                        /*
                                                        System.out.print("{");
                                                        for (int i = 0; i < packetSize + 4; ++i) {
                                                                System.out.format("%02X ", inputBuffer.get(i));
                                                        }
                                                        System.out.println("}");
                                                        */
                                                }
                                        }

                                        int lastPosition = inputBuffer.position();
                                        inputBuffer.position(packetSize + 4);
                                        inputBuffer.compact();
                                        inputBuffer.position(lastPosition - (packetSize + 4));
                                }
                                catch (IOException e) {
                                        log.error("Error while reading OSC client socket", e);
                                        break;
                                }
                        }

                        try {
                                socket.close();
                        }
                        catch (IOException e) {
                                log.error("Error while closing OSC client socket", e);
                        }
                }
        }

        private class OscServerThread extends Thread {
                private volatile boolean running = true;

                @Override
                public void run() {
                        if (serverSocket == null) {
                                log.error("Failed to start OSC server: server socket is null");
                                return;
                        }

                        while (running) {
                                try {
                                        log.info("Waiting for OSC client connections...");
                                        Socket clientSocket = serverSocket.accept();
                                        clientSocket.setTrafficClass(0x10); // low-latency

                                        log.info("OSC client connected from " + clientSocket.getInetAddress());
                                        Connection c = new Connection(clientSocket, bufferSize);
                                        c.start();
                                        for (ConnectionWatcher w : connectionWatchers) {
                                                w.onConnection(c);
                                        }
                                }
                                catch (IOException e) {
                                        log.error("Error while accepting OSC client connection", e);
                                        try {
                                                Thread.sleep(1000);
                                        } catch (InterruptedException ie) { /* pass */ }
                                        continue;
                                }
                        }

                        try {
                                serverSocket.close();
                        }
                        catch (IOException e) {
                                log.error("Error while closing OSC server", e);
                        }
                }

                public void shutdown() {
                        running = false;
                        interrupt();

                        try {
                                serverSocket.close();
                        }
                        catch (IOException e) {
                                log.error("Error while closing OSC server", e);
                        }
                }
        }
}
