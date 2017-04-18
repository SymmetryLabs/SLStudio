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
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXEngine;
import heronarts.lx.LXModulationEngine;
import heronarts.lx.LXPattern;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.midi.MidiControlChange;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.midi.MidiPitchBend;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXTriggerModulation;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.parameter.StringParameter;

public class LXOscEngine extends LXComponent {

    private static final String ROUTE_LX = "lx";
    private static final String ROUTE_ENGINE = "engine";
    private static final String ROUTE_OUTPUT = "output";
    private static final String ROUTE_PALETTE = "palette";
    private static final String ROUTE_MODULATION = "modulation";
    private static final String ROUTE_AUDIO = "audio";
    private static final String ROUTE_METER = "meter";
    private static final String ROUTE_MIDI = "midi";
    private static final String ROUTE_NOTE = "note";
    private static final String ROUTE_CC = "cc";
    private static final String ROUTE_PITCHBEND = "pitchbend";
    private static final String ROUTE_MASTER = "master";
    private static final String ROUTE_CHANNEL = "channel";
    private static final String ROUTE_ACTIVE_PATTERN = "activePattern";
    private static final String ROUTE_NEXT_PATTERN = "nextPattern";
    private static final String ROUTE_PATTERN = "pattern";
    private static final String ROUTE_EFFECT = "effect";
    private static final String ROUTE_FOCUSED = "focused";
    private static final String ROUTE_ACTIVE = "active";

    public final static int DEFAULT_RECEIVE_PORT = 3030;
    public final static int DEFAULT_TRANSMIT_PORT = 3131;

    public final static String DEFAULT_RECEIVE_HOST = "0.0.0.0";
    public final static String DEFAULT_TRANSMIT_HOST = "localhost";

    private final static int DEFAULT_MAX_PACKET_SIZE = 8192;

    public final StringParameter receiveHost =
        new StringParameter("RX Host", DEFAULT_RECEIVE_HOST)
        .setDescription("Hostname to which OSC input socket is bound");

    public final DiscreteParameter receivePort =
        new DiscreteParameter("RX Port", DEFAULT_RECEIVE_PORT, 1, 9999)
        .setDescription("UDP port on which the engine listens for OSC message");

    public final DiscreteParameter transmitPort =
        new DiscreteParameter("TX Port", DEFAULT_TRANSMIT_PORT, 1, 9999)
        .setDescription("UDP port on which the engine transmits OSC messages");

    public final StringParameter transmitHost =
        new StringParameter("TX Host", DEFAULT_TRANSMIT_HOST)
        .setDescription("Hostname to which OSC messages are sent");

    public final BooleanParameter receiveActive =
        new BooleanParameter("RX Active", false)
        .setDescription("Enables or disables OSC engine input");

    public final BooleanParameter transmitActive =
        new BooleanParameter("TX Active", false)
        .setDescription("Enables or disables OSC engine output");

    private final List<Receiver> receivers = new ArrayList<Receiver>();

    private Receiver engineReceiver;
    private final EngineListener engineListener = new EngineListener();

    private EngineTransmitter engineTransmitter;

    private final LX lx;

    public LXOscEngine(LX lx) {
        super(lx, "OSC");
        this.lx = lx;
        addParameter("receiveHost", this.receiveHost);
        addParameter("receivePort", this.receivePort);
        addParameter("receiveActive", this.receiveActive);
        addParameter("transmitHost", this.transmitHost);
        addParameter("transmitPort", this.transmitPort);
        addParameter("transmitActive", this.transmitActive);
    }

    /**
     * Gets the OSC address pattern for a parameter
     *
     * @param p parameter
     * @return OSC address
     */
    public static String getOscAddress(LXParameter p) {
        LXComponent component = p.getComponent();
        if (component instanceof LXOscComponent) {
            String componentAddress = ((LXOscComponent) component).getOscAddress();
            if (componentAddress == null) {
                System.err.println("Component has no OSC address: " + component + " (parameter: " + p + ")");
                new Exception().printStackTrace();
            } else {
                return componentAddress + "/" + p.getPath();
            }
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
                    } else if (parts[2].equals(ROUTE_MIDI)) {
                        oscMidi(message, parts, 3);
                    } else if (parts[2].equals(ROUTE_OUTPUT)) {
                        oscComponent(message, lx.engine.output, parts, 3);
                    } else if (parts[2].equals(ROUTE_AUDIO)) {
                        oscAudio(message, parts, 3);
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
                System.err.println("[OSC] No route for message: " + message.getAddressPattern().getValue());
            }
        }

        private void oscAudio(OscMessage message, String[] parts, int index) {
            if (parts[index].equals(ROUTE_METER)) {
                oscComponent(message, lx.engine.audio.meter, parts, index+1);
            } else {
                oscComponent(message, lx.engine.audio, parts, index);
            }
        }

        private void oscMidi(OscMessage message, String[] parts, int index) {
            try {
                if (parts[index].equals(ROUTE_NOTE)) {
                    int pitch = message.getInt();
                    int velocity = message.getInt();
                    int channel = message.getInt();
                    lx.engine.midi.dispatch(new MidiNoteOn(channel, pitch, velocity), false);
                } else if (parts[index].equals(ROUTE_CC)) {
                    int value = message.getInt();
                    int cc = message.getInt();
                    int channel = message.getInt();
                    lx.engine.midi.dispatch(new MidiControlChange(channel, cc, value), false);
                } else if (parts[index].equals(ROUTE_PITCHBEND)) {
                    int msb = message.getInt();
                    int channel = message.getInt();
                    lx.engine.midi.dispatch(new MidiPitchBend(channel, msb), false);
                } else {
                    System.err.println("[OSC] Unrecognized MIDI message: " + message.getAddressPattern().getValue());
                }
            } catch (InvalidMidiDataException imdx) {
                System.err.println("[OSC] Invalid MIDI message: " + imdx.getLocalizedMessage());
            }
        }

        private void oscChannel(OscMessage message, LXBus channel, String[] parts, int index) {
            if (channel instanceof LXChannel) {
                if (parts[index].equals(ROUTE_PATTERN)) {
                    if (parts[index+1].equals(ROUTE_ACTIVE)) {
                        oscPattern(message, ((LXChannel) channel).getActivePattern(), parts, index+2);
                    } else if (parts[index+1].matches("\\d+")) {
                        oscPattern(message, ((LXChannel) channel).getPattern(Integer.parseInt(parts[index+1])), parts, index+2);
                    } else {
                        oscPattern(message, ((LXChannel) channel).getPattern(parts[index+1]), parts, index+2);
                    }
                    return;
                } else if (parts[index].equals(ROUTE_ACTIVE_PATTERN) || parts[index].equals(ROUTE_NEXT_PATTERN)) {
                    ((LXChannel) channel).goIndex(message.getInt());
                    return;
                }
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
            } else if (parameter instanceof StringParameter) {
                ((StringParameter) parameter).setValue(message.getString());
            }  else if (parameter instanceof ColorParameter) {
                ((ColorParameter) parameter).setColor(message.getInt());
            } else if (parameter instanceof DiscreteParameter) {
                OscArgument arg = message.get();
                if (arg instanceof OscInt) {
                    parameter.setValue(arg.toInt());
                } else {
                    ((DiscreteParameter)parameter).setNormalized(arg.toFloat());
                }
            } else if (parameter instanceof LXNormalizedParameter) {
                ((LXNormalizedParameter)parameter).setNormalized(message.getFloat());
            } else {
                parameter.setValue(message.getFloat());
            }
        }
    }

    public class Transmitter {

        private final byte[] bytes;
        private final ByteBuffer buffer;
        private final DatagramSocket socket;
        protected final DatagramPacket packet;

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

        public void setPort(int port) {
            this.packet.setPort(port);
        }

        public void setHost(String host) throws UnknownHostException {
            this.packet.setAddress(InetAddress.getByName(host));
        }
    }

    private class EngineTransmitter extends Transmitter implements LXParameterListener, LXChannel.Listener, LXEngine.Listener, LXModulationEngine.Listener {
        private EngineTransmitter(String host, int port, int bufferSize) throws SocketException, UnknownHostException {
            super(InetAddress.getByName(host), port, bufferSize);
            registerComponent(lx.engine);
            registerComponent(lx.palette);
            registerComponent(lx.tempo);
            registerComponent(lx.engine.audio);
            registerComponent(lx.engine.audio.meter);
            registerComponent(lx.engine.output);
            registerComponent(lx.engine.modulation);
            for (LXModulator modulator : lx.engine.modulation.modulators) {
                registerComponent(modulator);
            }
            lx.engine.modulation.addListener(this);
            registerComponent(lx.engine.masterChannel);
            for (LXChannel channel : lx.engine.getChannels()) {
                registerChannel(channel);
            }
            lx.engine.addListener(this);
        }

        private void registerChannel(LXChannel channel) {
            registerComponent(channel);
            for (LXPattern p : channel.patterns) {
                registerComponent(p);
            }
            channel.addListener(this);
        }

        private void unregisterChannel(LXChannel channel) {
            unregisterComponent(channel);
            for (LXPattern p : channel.patterns) {
                unregisterComponent(p);
            }
            channel.removeListener(this);
        }

        private void registerComponent(LXComponent component) {
            for (LXParameter p : component.getParameters()) {
                if (p instanceof LXListenableParameter) {
                    ((LXListenableParameter) p).addListener(this);
                }
            }
        }

        private void unregisterComponent(LXComponent component) {
            for (LXParameter p : component.getParameters()) {
                if (p instanceof LXListenableParameter) {
                    ((LXListenableParameter) p).removeListener(this);
                }
            }
        }

        private final OscMessage oscMessage = new OscMessage("");
        private final OscFloat oscFloat = new OscFloat(0);
        private final OscInt oscInt = new OscInt(0);
        private final OscString oscString = new OscString("");

        @Override
        public void onParameterChanged(LXParameter parameter) {
            if (transmitActive.isOn()) {
                // TODO(mcslee): contemplate accumulating OscMessages into OscBundle
                // and sending once per engine loop?? Probably a bad tradeoff since
                // it would require dynamic memory allocations that we can skip here...
                String address = getOscAddress(parameter);
                if (address != null) {
                    oscMessage.clearArguments();
                    oscMessage.setAddressPattern(address);
                    if (parameter instanceof BooleanParameter) {
                        oscInt.setValue(((BooleanParameter) parameter).isOn() ? 1 : 0);
                        oscMessage.add(oscInt);
                    } else if (parameter instanceof StringParameter) {
                        oscString.setValue(((StringParameter) parameter).getString());
                        oscMessage.add(oscString);
                    } else if (parameter instanceof ColorParameter) {
                        oscInt.setValue(((ColorParameter) parameter).getColor());
                        oscMessage.add(oscInt);
                    } else if (parameter instanceof DiscreteParameter) {
                        oscInt.setValue(((DiscreteParameter) parameter).getValuei());
                        oscMessage.add(oscInt);
                    } else if (parameter instanceof LXNormalizedParameter) {
                        oscFloat.setValue(((LXNormalizedParameter) parameter).getNormalizedf());
                        oscMessage.add(oscFloat);
                    } else {
                        oscFloat.setValue(parameter.getValuef());
                        oscMessage.add(oscFloat);
                    }
                    sendMessage(oscMessage);
                }
            }
        }

        private void sendMessage(String address, int value) {
            oscMessage.clearArguments();
            oscMessage.setAddressPattern(address);
            oscInt.setValue(value);
            oscMessage.add(oscInt);
            sendMessage(oscMessage);
        }

        private void sendMessage(OscMessage message) {
            try {
                send(oscMessage);
            } catch (IOException iox) {
                System.err.println("[OSC] Failed to transmit: " + iox.getLocalizedMessage());
            }
        }

        @Override
        public void effectAdded(LXBus channel, LXEffect effect) {
            registerComponent(effect);
        }

        @Override
        public void effectRemoved(LXBus channel, LXEffect effect) {
            unregisterComponent(effect);
        }

        @Override
        public void effectMoved(LXBus channel, LXEffect effect) {}

        @Override
        public void indexChanged(LXChannel channel) {}

        @Override
        public void patternAdded(LXChannel channel, LXPattern pattern) {
            registerComponent(pattern);
        }

        @Override
        public void patternRemoved(LXChannel channel, LXPattern pattern) {
            unregisterComponent(pattern);
        }

        @Override
        public void patternMoved(LXChannel channel, LXPattern pattern) {

        }

        @Override
        public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
            sendMessage(channel.getOscAddress() + "/nextPattern", nextPattern.getIndex());
        }

        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
            sendMessage(channel.getOscAddress() + "/activePattern", pattern.getIndex());
            sendMessage(channel.getOscAddress() + "/nextPattern", -1);
        }

        @Override
        public void channelAdded(LXEngine engine, LXChannel channel) {
            registerChannel(channel);
        }

        @Override
        public void channelRemoved(LXEngine engine, LXChannel channel) {
            unregisterChannel(channel);
        }

        @Override
        public void channelMoved(LXEngine engine, LXChannel channel) {}

        @Override
        public void modulatorAdded(LXModulationEngine engine, LXModulator modulator) {
            registerComponent(modulator);
        }

        @Override
        public void modulatorRemoved(LXModulationEngine engine, LXModulator modulator) {
            unregisterComponent(modulator);
        }

        @Override
        public void modulationAdded(LXModulationEngine engine, LXCompoundModulation modulation) {
            // TODO(mcslee): should probably OSC-map these...
        }

        @Override
        public void modulationRemoved(LXModulationEngine engine, LXCompoundModulation modulation) {
            // TODO(mcslee): should probably OSC-map these...
        }

        @Override
        public void triggerAdded(LXModulationEngine engine, LXTriggerModulation trigger) {
            // TODO(mcslee): should probably OSC-map these...
        }

        @Override
        public void triggerRemoved(LXModulationEngine engine, LXTriggerModulation trigger) {
            // TODO(mcslee): should probably OSC-map these...
        }


    }

    public class Receiver {

        public final int port;
        private final DatagramSocket socket;
        public final SocketAddress address;
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
            this.address = socket.getLocalSocketAddress();
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
                System.out.println("Stopped OSC listener " + address);
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
        if (p == this.receivePort || p == this.receiveHost) {
            if (this.engineReceiver != null) {
                startReceiver();
            }
        } else if (p == this.receiveActive) {
            if (this.receiveActive.isOn()) {
                startReceiver();
            } else {
                stopReceiver();
            }
        } else if (p == this.transmitPort) {
            if (this.engineTransmitter != null) {
                this.engineTransmitter.setPort(this.transmitPort.getValuei());
            }
        } else if (p == this.transmitHost) {
            if (this.engineTransmitter != null) {
                try {
                    this.engineTransmitter.setHost(this.transmitHost.getString());
                } catch (UnknownHostException uhx) {
                    System.err.println("[OSC] Invalid host: " + uhx.getLocalizedMessage());
                    this.transmitActive.setValue(false);
                }
            }
        } else if (p == this.transmitActive) {
            startTransmitter();
        }
    }

    private void startReceiver() {
        if (this.engineReceiver != null) {
            stopReceiver();
        }
        try {
            this.engineReceiver = receiver(this.receivePort.getValuei(), this.receiveHost.getString());
            this.engineReceiver.addListener(this.engineListener);
            System.out.println("Started OSC listener " + this.engineReceiver.address);
        } catch (SocketException sx) {
            System.err.println("Failed to start OSC receiver: " + sx.getLocalizedMessage());
        } catch (UnknownHostException uhx) {
            System.err.println("Bad OSC receive host: " + uhx.getLocalizedMessage());
        }
    }

    private void stopReceiver() {
        if (this.engineReceiver != null) {
            this.engineReceiver.stop();
            this.engineReceiver = null;
        }
    }

    private void startTransmitter() {
        if (this.engineTransmitter == null) {
            try {
                this.engineTransmitter = new EngineTransmitter(
                    this.transmitHost.getString(),
                    this.transmitPort.getValuei(),
                    DEFAULT_MAX_PACKET_SIZE
                );
            } catch (UnknownHostException uhx) {
                System.err.println("[OSC] Invalid host: " + uhx.getLocalizedMessage());
            } catch (SocketException sx) {
                System.err.println("[OSC] Could not start transmitter: " + sx.getLocalizedMessage());
            }
        }
    }

    public Receiver receiver(int port, String host) throws SocketException, UnknownHostException {
        return receiver(port, InetAddress.getByName(host));
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

    public Transmitter transmitter(String host, int port) throws SocketException, UnknownHostException {
        return transmitter(InetAddress.getByName(host), port);
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
