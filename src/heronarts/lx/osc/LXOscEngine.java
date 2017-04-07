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
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXEngine;
import heronarts.lx.LXMasterChannel;
import heronarts.lx.LXModulationEngine;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.color.LXPalette;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.output.LXOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.StringParameter;

public class LXOscEngine extends LXComponent {

    private static final String ROUTE_LX = "lx";
    private static final String ROUTE_ENGINE = "engine";
    private static final String ROUTE_OUTPUT = "output";
    private static final String ROUTE_PALETTE = "palette";
    private static final String ROUTE_MODULATION = "modulation";
    private static final String ROUTE_AUDIO = "audio";
    private static final String ROUTE_MASTER = "master";
    private static final String ROUTE_CHANNEL = "channel";
    private static final String ROUTE_PATTERN = "pattern";
    private static final String ROUTE_EFFECT = "effect";
    private static final String ROUTE_FOCUSED = "focused";
    private static final String ROUTE_ACTIVE = "active";

    public final static int DEFAULT_PORT = 3030;
    private final static int DEFAULT_MAX_PACKET_SIZE = 8192;

    public final DiscreteParameter port =
        new DiscreteParameter("Port", DEFAULT_PORT, 1, 9999)
        .setDescription("UDP port on which the engine listens for OSC message");

    public final BooleanParameter active =
        new BooleanParameter("Active", false)
        .setDescription("Enables or disabled the OSC engine");

    private final List<Receiver> receivers = new ArrayList<Receiver>();

    private Receiver engineReceiver;
    private final EngineListener engineListener = new EngineListener();

    private final LX lx;

    public LXOscEngine(LX lx) {
        super(lx);
        this.lx = lx;
        this.label.setValue("OSC");
        addParameter("port", this.port);
        addParameter("active", this.active);
    }

    public static String getOscAddress(LXComponent component) {
        if (component instanceof LXEngine) {
            return "/lx/engine";
        } else if (component instanceof LXPalette) {
            return "/lx/palette";
        } else if (component instanceof LXAudioEngine) {
            return "/lx/audio";
        } else if (component instanceof LXOutput) {
            return "/lx/output";
        } else if (component instanceof LXModulationEngine) {
            return "/lx/modulation";
        } else if (component instanceof LXMasterChannel) {
            return "/lx/master";
        } else if (component instanceof LXChannel) {
            return "/lx/channel/" + ((LXChannel) component).getIndex();
        } else if (component instanceof LXPattern) {
            LXPattern pattern = (LXPattern) component;
            return getOscAddress(pattern.getChannel()) + "/pattern/" + pattern.getIndex();
        } else if (component instanceof LXEffect) {
            LXEffect effect = (LXEffect) component;
            return getOscAddress(effect.getBus()) + "/effect/" + effect.getIndex();
        } else if (component instanceof LXModulator) {
            String componentAddress = getOscAddress(component.getParent());
            if (componentAddress != null) {
                return componentAddress + "/" + component.getLabel();
            }
        }
        return null;
    }

    /**
     * Gets the OSC address pattern for a parameter
     *
     * @param p parameter
     * @return OSC address
     */
    public static String getOscAddress(LXParameter p) {
        String componentAddress = getOscAddress(p.getComponent());
        if (componentAddress != null) {
            return componentAddress + "/" + p.getPath();
        }
        return null;
    }

    private class EngineListener implements LXOscListener {

        @Override
        public void oscMessage(OscMessage message) {
            try {
                String[] parts = message.getAddressPattern().getValue().split("/");
                if (parts[1].equals(ROUTE_LX)) {
                    if (parts[2].equals(ROUTE_ENGINE)) {
                        oscComponent(message, lx.engine, parts, 3);
                    } else if (parts[2].equals(ROUTE_OUTPUT)) {
                        oscComponent(message, lx.engine.output, parts, 3);
                    } else if (parts[2].equals(ROUTE_AUDIO)) {
                        oscComponent(message, lx.engine.audio, parts, 3);
                    } else if (parts[2].equals(ROUTE_PALETTE)) {
                        oscComponent(message, lx.palette, parts, 3);
                    } else if (parts[2].equals(ROUTE_MODULATION)) {
                        oscComponent(message, lx.engine.modulation.getModulator(parts[3]), parts, 4);
                    } else if (parts[2].equals(ROUTE_MASTER)) {
                        oscChannel(message, lx.engine.masterChannel, parts, 3);
                    } else if (parts[2].equals(ROUTE_CHANNEL)) {
                        if (parts[3].equals(ROUTE_FOCUSED)) {
                            oscChannel(message, lx.engine.getFocusedChannel(), parts, 4);
                        } else if (parts[3].matches("\\d+")) {
                            oscChannel(message, lx.engine.getChannel(Integer.parseInt(parts[3])), parts, 4);
                        } else {
                            oscChannel(message, lx.engine.getChannel(parts[3]), parts, 4);
                        }
                    }
                }
            } catch (Exception x) {
                System.err.println("[OSC] Could not route message: " + message.getAddressPattern().getValue());
            }
        }

        private void oscChannel(OscMessage message, LXBus channel, String[] parts, int index) {
            if (channel instanceof LXChannel && parts[index].equals(ROUTE_PATTERN)) {
                if (parts[index+1].equals(ROUTE_ACTIVE)) {
                    oscPattern(message, ((LXChannel) channel).getActivePattern(), parts, index+2);
                } else if (parts[index+1].matches("\\d+")) {
                    oscPattern(message, ((LXChannel) channel).getPattern(Integer.parseInt(parts[index+1])), parts, index+2);
                } else {
                    oscPattern(message, ((LXChannel) channel).getPattern(parts[index+1]), parts, index+2);
                }
                return;
            }
            if (parts[index].equals(ROUTE_EFFECT)) {
                if (parts[index+1].matches("\\d+")) {
                    oscEffect(message, channel.getEffect(Integer.parseInt(parts[index+1])), parts, index+2);
                } else {
                    oscEffect(message, channel.getEffect(parts[index+1]), parts, index+2);
                }
                return;
            }
            oscComponent(message, channel, parts, index);
        }

        private void oscEffect(OscMessage message, LXEffect effect, String[] parts, int index) {
            oscComponent(message, effect, parts, index);
        }

        private void oscPattern(OscMessage message, LXPattern pattern, String[] parts, int index) {
            oscComponent(message, pattern, parts, index);
        }

        private void oscComponent(OscMessage message, LXComponent component, String[] parts, int index) {
            LXParameter parameter = component.getParameter(parts[index]);
            if (parameter == null) {
                System.err.println("[OSC] Component " + component + " does not have parameter: " + parts[index]);
                return;
            }
            if (parameter instanceof BooleanParameter) {
                ((BooleanParameter)parameter).setValue(message.getBoolean());
            } else if (parameter instanceof DiscreteParameter) {
                OscArgument arg = message.get();
                if (arg instanceof OscInt) {
                    parameter.setValue(arg.toInt());
                } else {
                    ((DiscreteParameter)parameter).setNormalized(arg.toFloat());
                }
            } else if (parameter instanceof LXNormalizedParameter) {
                ((LXNormalizedParameter)parameter).setNormalized(message.getFloat());
            } else if (parameter instanceof StringParameter) {
                ((StringParameter) parameter).setValue(message.getString());
            }
        }
    }

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
                while (!isInterrupted()) {
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
                        if (!isInterrupted()) {
                            System.err.println("Exception in OSC listener on port " + port + ":" + iox.getMessage());
                        }
                    }
                }
                socket.close();
                System.out.println("Stopped OSC listener on port " + port);
            }
        }

        private void dispatch() {
            this.engineThreadEventQueue.clear();
            synchronized (this.threadSafeEventQueue) {
                this.engineThreadEventQueue.addAll(this.threadSafeEventQueue);
                this.threadSafeEventQueue.clear();
            }
            // TODO(mcslee): do we want to handle NTP timetags?

            // NOTE(mcslee): we iterate this way so that listeners can modify the listener list
            this.listenerSnapshot.clear();
            this.listenerSnapshot.addAll(this.listeners);
            for (OscMessage message : this.engineThreadEventQueue) {
                for (LXOscListener listener : this.listenerSnapshot) {
                    listener.oscMessage(message);
                }
            }
        }

        public void stop() {
            this.thread.interrupt();
            this.socket.close();
            this.listeners.clear();
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == this.port) {
            if (this.engineReceiver != null) {
                start();
            }
        } else if (p == this.active) {
            if (this.active.isOn()) {
                start();
            } else {
                stop();
            }
        }
    }

    private void start() {
        if (this.engineReceiver != null) {
            stop();
        }
        try {
            this.engineReceiver = receiver(this.port.getValuei());
            this.engineReceiver.addListener(this.engineListener);
            System.out.println("Started OSC listener on port " + this.engineReceiver.port);
        } catch (SocketException sx) {
            System.err.println("Failed to start OSC receiver: " + sx.getLocalizedMessage());
        }
    }

    private void stop() {
        if (this.engineReceiver != null) {
            this.engineReceiver.stop();
            this.engineReceiver = null;
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
