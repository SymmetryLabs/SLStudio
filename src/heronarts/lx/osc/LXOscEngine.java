/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.osc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import heronarts.lx.LX;

public class LXOscEngine {

    private final List<LXOscListener> listeners = new ArrayList<LXOscListener>();

    public LXOscEngine(LX lx) {}

    private final List<Receiver> receivers = new ArrayList<Receiver>();

    private final static int DEFAULT_MAX_PACKET_SIZE = 8192;

    public class Transmitter {

        private final byte[] bytes;
        private final ByteBuffer buffer;
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        private Transmitter(InetAddress address, int port, int bufferSize) throws SocketException {
            this.bytes = new byte[bufferSize];
            this.buffer = ByteBuffer.wrap(this.bytes);
            this.packet = new DatagramPacket(this.bytes, this.bytes.length, address, port);
            this.socket = new DatagramSocket();
        }

        public void send(OscPacket packet) throws IOException {
            this.buffer.rewind();
            packet.serialize(this.buffer);
            this.packet.setLength(this.buffer.position());
            this.socket.send(this.packet);
        }
    }

    public class Receiver {

        public final int port;
        private final DatagramSocket socket;
        private final DatagramPacket packet;
        private final byte[] buffer;
        private final ReceiverThread thread;

        private final List<OscMessage> threadSafeEventQueue =
            Collections.synchronizedList(new ArrayList<OscMessage>());

        private final List<OscMessage> engineThreadEventQueue =
            new ArrayList<OscMessage>();

        private final List<LXOscListener> listeners = new ArrayList<LXOscListener>();
        private final List<LXOscListener> listenerSnapshot = new ArrayList<LXOscListener>();

        private Receiver(int port, InetAddress address, int bufferSize) throws SocketException {
            this(new DatagramSocket(port, address), port, bufferSize);
        }

        private Receiver(int port, int bufferSize) throws SocketException {
            this(new DatagramSocket(port), port, bufferSize);
        }

        private Receiver(DatagramSocket socket, int port, int bufferSize) throws SocketException {
            this.socket = socket;
            this.port = port;
            this.buffer = new byte[bufferSize];
            this.packet = new DatagramPacket(this.buffer, bufferSize);
            this.thread = new ReceiverThread();
            this.thread.start();
        }

        public Receiver addListener(LXOscListener listener) {
            this.listeners.add(listener);
            return this;
        }

        public Receiver removeListener(LXOscListener listener) {
            this.listeners.remove(listener);
            return this;
        }

        class ReceiverThread extends Thread {
            @Override
            public void run() {
                while (true) {
                    if (isInterrupted()) {
                        socket.close();
                        return;
                    }
                    try {
                        socket.receive(packet);
                        try {
                            // Parse the OSC packet
                            OscPacket oscPacket = OscPacket.parse(packet);

                            // Add all messages in the packet to the queue
                            if (oscPacket instanceof OscMessage) {
                                threadSafeEventQueue.add((OscMessage) oscPacket);
                            } else if (oscPacket instanceof OscBundle) {
                                for (OscMessage message : (OscBundle) oscPacket) {
                                    threadSafeEventQueue.add(message);
                                }
                            }
                        } catch (OscException oscx) {
                            System.err.println("OSC exception: " + oscx.getMessage());
                        }
                    } catch (IOException iox) {
                        if (isInterrupted()) {
                            socket.close();
                            return;
                        } else {
                            System.err.println("Exception in OSC listener on port " + port + ":" + iox.getMessage());
                        }
                    }
                }
            }
        }

        private void dispatch() {
            this.engineThreadEventQueue.clear();
            synchronized (this.threadSafeEventQueue) {
                this.engineThreadEventQueue.addAll(this.threadSafeEventQueue);
                this.threadSafeEventQueue.clear();
            }
            // TODO(mcslee): do we want to handle NTP timetags?
            for (OscMessage message : this.engineThreadEventQueue) {
                dispatch(message);
            }
        }

        private void dispatch(OscMessage message) {
            // NOTE(mcslee): we iterate this way so that listeners can modify the listener list
            this.listenerSnapshot.clear();
            this.listenerSnapshot.addAll(this.listeners);
            for (LXOscListener listener : this.listenerSnapshot) {
                listener.oscMessage(message);
            }
        }

        public void stop() {
            this.thread.interrupt();
            this.socket.close();
        }
    }

    public Receiver receiver(int port, InetAddress address) throws SocketException {
        return receiver(port, address, DEFAULT_MAX_PACKET_SIZE);
    }

    public Receiver receiver(int port, InetAddress address, int bufferSize) throws SocketException {
        Receiver receiver = new Receiver(port, address, bufferSize);
        this.receivers.add(receiver);
        return receiver;
    }

    public Receiver receiver(int port) throws SocketException {
        return receiver(port, DEFAULT_MAX_PACKET_SIZE);
    }

    public Receiver receiver(int port, int bufferSize) throws SocketException {
        Receiver receiver = new Receiver(port, bufferSize);
        this.receivers.add(receiver);
        return receiver;
    }

    public Transmitter transmitter(InetAddress address, int port) throws SocketException {
        return transmitter(address, port, DEFAULT_MAX_PACKET_SIZE);
    }

    public Transmitter transmitter(InetAddress address, int port, int bufferSize) throws SocketException {
        return new Transmitter(address, port, bufferSize);
    }

    public LXOscEngine addListener(LXOscListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public LXOscEngine removeListener(LXOscListener listener) {
        this.listeners.remove(listener);
        return this;
    }

    /**
     * Invoked by the main engine to dispatch all OSC messages on the
     * input queue.
     */
    public void dispatch() {
        for (Receiver receiver : this.receivers) {
            receiver.dispatch();
        }
    }

}
