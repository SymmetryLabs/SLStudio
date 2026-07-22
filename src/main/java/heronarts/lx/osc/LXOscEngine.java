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
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.sound.midi.InvalidMidiDataException;

import heronarts.lx.*;
import heronarts.lx.LXChannel;
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
import heronarts.lx.warp.LXWarp;

public class LXOscEngine extends LXComponent {

    private static final String ROUTE_LX = "lx";
    private static final String ROUTE_ENGINE = "engine";
    private static final String ROUTE_INPUT = "input";
    private static final String ROUTE_OUTPUT = "output";
    private static final String ROUTE_PALETTE = "palette";
    private static final String ROUTE_MODULATION = "modulation";
    private static final String ROUTE_AUDIO = "audio";
    private static final String ROUTE_TEMPO = "tempo";
    private static final String ROUTE_BEAT = "beat";
    private static final String ROUTE_METER = "meter";
    private static final String ROUTE_MIDI = "midi";
    private static final String ROUTE_NOTE = "note";
    private static final String ROUTE_CC = "cc";
    private static final String ROUTE_PITCHBEND = "pitchbend";
    private static final String ROUTE_MASTER = "master";
    private static final String ROUTE_CHANNEL = "channel";
    private static final String ROUTE_LOOK = "look";
    private static final String ROUTE_ACTIVE_PATTERN = "activePattern";
    private static final String ROUTE_NEXT_PATTERN = "nextPattern";
    private static final String ROUTE_PATTERN = "pattern";
    private static final String ROUTE_EFFECT = "effect";
    private static final String ROUTE_FOCUSED = "focused";
    private static final String ROUTE_ACTIVE = "active";
    private static final String ROUTE_HUE = "hue";
    private static final String ROUTE_SATURATION = "saturation";
    private static final String ROUTE_BRIGHTNESS = "brightness";
    private static final String ROUTE_WARP = "warp";

    public final static int DEFAULT_RECEIVE_PORT = 3030;
    public final static int DEFAULT_TRANSMIT_PORT = 3131;

    public final static String DEFAULT_RECEIVE_HOST = "0.0.0.0";
    public final static String DEFAULT_TRANSMIT_HOST = "localhost";

    private final static int DEFAULT_MAX_PACKET_SIZE = 8192;

    public final StringParameter receiveHost = (StringParameter)
        new StringParameter("RX Host", DEFAULT_RECEIVE_HOST)
        .setDescription("Hostname to which OSC input socket is bound")
        .setSupportsOscTransmit(false);

    public final DiscreteParameter receivePort = (DiscreteParameter)
        new DiscreteParameter("RX Port", DEFAULT_RECEIVE_PORT, 1, 9999)
        .setDescription("UDP port on which the engine listens for OSC message")
        .setUnits(LXParameter.Units.INTEGER)
        .setSupportsOscTransmit(false);

    public final DiscreteParameter transmitPort = (DiscreteParameter)
        new DiscreteParameter("TX Port", DEFAULT_TRANSMIT_PORT, 1, 9999)
        .setDescription("UDP port on which the engine transmits OSC messages")
        .setUnits(LXParameter.Units.INTEGER)
        .setSupportsOscTransmit(false);

    public final StringParameter transmitHost = (StringParameter)
        new StringParameter("TX Host", DEFAULT_TRANSMIT_HOST)
        .setDescription("Hostname to which OSC messages are sent")
        .setSupportsOscTransmit(false);

    public final BooleanParameter receiveActive = (BooleanParameter)
        new BooleanParameter("RX Active", false)
        .setDescription("Enables or disables OSC engine input")
        .setSupportsOscTransmit(false);

    public final BooleanParameter transmitActive = (BooleanParameter)
        new BooleanParameter("TX Active", false)
        .setDescription("Enables or disables OSC engine output")
        .setSupportsOscTransmit(false);

    private final List<Receiver> receivers = new ArrayList<Receiver>();

    private Receiver engineReceiver;
    private final EngineListener engineListener = new EngineListener();
    private final List<LXOscListener> engineListenerQueue = new ArrayList<LXOscListener>();

    private EngineTransmitter engineTransmitter;

    private volatile boolean suppressEcho = false;

    private final LX lx;

    public interface DestinationListener {
        void destinationAdded(LXOscEngine engine, OscDestination destination);
        void destinationRemoved(LXOscEngine engine, OscDestination destination);
    }

    private final List<DestinationListener> destinationListeners = new ArrayList<DestinationListener>();

    public void addDestinationListener(DestinationListener listener) {
        this.destinationListeners.add(listener);
    }

    public void removeDestinationListener(DestinationListener listener) {
        this.destinationListeners.remove(listener);
    }

    private final List<OscDestination> extraDestinations = new CopyOnWriteArrayList<OscDestination>();

    public List<OscDestination> getExtraDestinations() {
        return Collections.unmodifiableList(this.extraDestinations);
    }

    public OscDestination addDestination() {
        OscDestination dest = new OscDestination(this.lx, this.extraDestinations.size() + 1);
        this.extraDestinations.add(dest);
        for (DestinationListener l : this.destinationListeners) {
            l.destinationAdded(this, dest);
        }
        return dest;
    }

    public void removeDestination(OscDestination dest) {
        if (this.extraDestinations.remove(dest)) {
            dest.dispose();
            for (DestinationListener l : this.destinationListeners) {
                l.destinationRemoved(this, dest);
            }
        }
    }

    public class OscDestination {
        public final StringParameter receiveHost;
        public final DiscreteParameter receivePort;
        public final BooleanParameter receiveActive;
        public final StringParameter transmitHost;
        public final DiscreteParameter transmitPort;
        public final BooleanParameter transmitActive;

        private Receiver destReceiver;
        private Transmitter destTransmitter;
        private final LXParameterListener paramListener;

        OscDestination(LX lx, int index) {
            this.receiveHost = (StringParameter)
                new StringParameter("RX Host " + index, DEFAULT_RECEIVE_HOST)
                .setDescription("Hostname for extra OSC input #" + index)
                .setSupportsOscTransmit(false);

            this.receivePort = (DiscreteParameter)
                new DiscreteParameter("RX Port " + index, DEFAULT_RECEIVE_PORT, 1, 9999)
                .setDescription("Port for extra OSC input #" + index)
                .setUnits(LXParameter.Units.INTEGER)
                .setSupportsOscTransmit(false);

            this.receiveActive = (BooleanParameter)
                new BooleanParameter("RX Active " + index, false)
                .setDescription("Enable extra OSC input #" + index)
                .setSupportsOscTransmit(false);

            this.transmitHost = (StringParameter)
                new StringParameter("TX Host " + index, DEFAULT_TRANSMIT_HOST)
                .setDescription("Hostname for extra OSC output #" + index)
                .setSupportsOscTransmit(false);

            this.transmitPort = (DiscreteParameter)
                new DiscreteParameter("TX Port " + index, DEFAULT_TRANSMIT_PORT, 1, 9999)
                .setDescription("Port for extra OSC output #" + index)
                .setUnits(LXParameter.Units.INTEGER)
                .setSupportsOscTransmit(false);

            this.transmitActive = (BooleanParameter)
                new BooleanParameter("TX Active " + index, false)
                .setDescription("Enable extra OSC output #" + index)
                .setSupportsOscTransmit(false);

            this.paramListener = (p) -> onDestParamChanged(p);
            this.receiveHost.addListener(this.paramListener);
            this.receivePort.addListener(this.paramListener);
            this.receiveActive.addListener(this.paramListener);
            this.transmitHost.addListener(this.paramListener);
            this.transmitPort.addListener(this.paramListener);
            this.transmitActive.addListener(this.paramListener);
        }

        private void onDestParamChanged(LXParameter p) {
            if (p == this.receivePort || p == this.receiveHost) {
                if (this.destReceiver != null) {
                    startDestReceiver();
                }
            } else if (p == this.receiveActive) {
                if (this.receiveActive.isOn()) {
                    startDestReceiver();
                } else {
                    stopDestReceiver();
                }
            } else if (p == this.transmitPort) {
                if (this.destTransmitter != null) {
                    this.destTransmitter.setPort(this.transmitPort.getValuei());
                }
            } else if (p == this.transmitHost) {
                if (this.destTransmitter != null) {
                    try {
                        this.destTransmitter.setHost(this.transmitHost.getString());
                    } catch (UnknownHostException uhx) {
                        System.err.println("[OSC] Invalid host: " + uhx.getLocalizedMessage());
                        this.transmitActive.setValue(false);
                    }
                }
            } else if (p == this.transmitActive) {
                if (this.transmitActive.isOn()) {
                    startDestTransmitter();
                } else {
                    stopDestTransmitter();
                }
            }
        }

        private void startDestReceiver() {
            stopDestReceiver();
            try {
                this.destReceiver = receiver(this.receivePort.getValuei(), this.receiveHost.getString());
                this.destReceiver.addListener(engineListener);
                System.out.println("[OSC] Started extra receiver " + this.destReceiver.address);
            } catch (SocketException sx) {
                System.err.println("[OSC] Failed to start extra receiver: " + sx.getLocalizedMessage());
            } catch (UnknownHostException uhx) {
                System.err.println("[OSC] Bad extra receive host: " + uhx.getLocalizedMessage());
            }
        }

        private void stopDestReceiver() {
            if (this.destReceiver != null) {
                this.destReceiver.stop();
                this.destReceiver = null;
            }
        }

        private void startDestTransmitter() {
            if (this.destTransmitter == null) {
                try {
                    this.destTransmitter = new Transmitter(
                        InetAddress.getByName(this.transmitHost.getString()),
                        this.transmitPort.getValuei(),
                        DEFAULT_MAX_PACKET_SIZE
                    );
                    System.out.println("[OSC] Started extra transmitter to " + this.transmitHost.getString() + ":" + this.transmitPort.getValuei());
                } catch (UnknownHostException uhx) {
                    System.err.println("[OSC] Invalid extra TX host: " + uhx.getLocalizedMessage());
                } catch (SocketException sx) {
                    System.err.println("[OSC] Could not start extra transmitter: " + sx.getLocalizedMessage());
                }
            }
        }

        private void stopDestTransmitter() {
            this.destTransmitter = null;
        }

        public void sendMessage(OscMessage message) {
            if (this.transmitActive.isOn() && this.destTransmitter != null) {
                try {
                    this.destTransmitter.send(message);
                } catch (IOException iox) {
                    System.err.println("[OSC] Failed to transmit to extra dest: " + iox.getLocalizedMessage());
                }
            }
        }

        void dispose() {
            stopDestReceiver();
            stopDestTransmitter();
            this.receiveHost.removeListener(this.paramListener);
            this.receivePort.removeListener(this.paramListener);
            this.receiveActive.removeListener(this.paramListener);
            this.transmitHost.removeListener(this.paramListener);
            this.transmitPort.removeListener(this.paramListener);
            this.transmitActive.removeListener(this.paramListener);
        }

        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("receiveHost", this.receiveHost.getString());
            obj.addProperty("receivePort", this.receivePort.getValuei());
            obj.addProperty("receiveActive", this.receiveActive.isOn());
            obj.addProperty("transmitHost", this.transmitHost.getString());
            obj.addProperty("transmitPort", this.transmitPort.getValuei());
            obj.addProperty("transmitActive", this.transmitActive.isOn());
            return obj;
        }

        public void loadJson(JsonObject obj) {
            if (obj.has("receiveHost")) this.receiveHost.setValue(obj.get("receiveHost").getAsString());
            if (obj.has("receivePort")) this.receivePort.setValue(obj.get("receivePort").getAsInt());
            if (obj.has("transmitHost")) this.transmitHost.setValue(obj.get("transmitHost").getAsString());
            if (obj.has("transmitPort")) this.transmitPort.setValue(obj.get("transmitPort").getAsInt());
            if (obj.has("receiveActive")) this.receiveActive.setValue(obj.get("receiveActive").getAsBoolean());
            if (obj.has("transmitActive")) this.transmitActive.setValue(obj.get("transmitActive").getAsBoolean());
        }
    }

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

    public void enqueueMessage(OscMessage toSend){
        engineTransmitter.sendMessage(toSend);
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
            if (componentAddress != null) {
                return componentAddress + "/" + p.getPath();
            }
        }
        return null;
    }

    private class EngineListener implements LXOscListener {

        @Override
        public void oscMessage(OscMessage message) {
            suppressEcho = true;
            try {
                String[] parts = message.getAddressPattern().getValue().split("/");
                LXChannel channel = LXLook.allChannels.get("LXChannel[Look-1 | Channel-9]");
                // LXChannel channel = LXLook.allChannels.get("LXChannel[Channel-9]");


                if (parts[1].equals("AutoCycle")) {
                    boolean cycle = message.getInt(0) == 0;
                    switch (parts[2]) {
                        case ("AskewPlanes"):
                            setAutoCycle(channel, "AskewPlanes[Look-1 | Channel-9 | AskewPlanes]", cycle);
                            break;
                        case ("Awaken"):
                            setAutoCycle(channel, "Awaken[Look-1 | Channel-9 | Awaken]", cycle);
                            break;
                        case ("Balance"):
                            setAutoCycle(channel, "Balance[Look-1 | Channel-9 | Balance]", cycle);
                            break;
                        case ("Blinders"):
                            setAutoCycle(channel, "Blinders[Look-1 | Channel-9 | Blinders]", cycle);
                            break;
                        case ("BassPod"):
                            setAutoCycle(channel, "BassPod[Look-1 | Channel-9 | BassPod]", cycle);
                            break;
                        case ("BouncyBalls"):
                            setAutoCycle(channel, "BouncyBalls[Look-1 | Channel-9 | BouncyBalls]", cycle);
                            break;
                        case ("CrossSections"):
                            setAutoCycle(channel, "CrossSections[Look-1 | Channel-9 | CrossSections]", cycle);
                            break;
                        case ("Crystalline"):
                            setAutoCycle(channel, "Crystalline[Look-1 | Channel-9 | Crystalline]", cycle);
                            break;
                        case ("Diamonds"):
                            setAutoCycle(channel, "Diamonds[Look-1 | Channel-9 | Diamonds]", cycle);
                            break;
                        case ("Explosions"):
                            setAutoCycle(channel, "Explosions[Look-1 | Channel-9 | Explosions]", cycle);
                            break;
                        case ("FlockWave"):
                            setAutoCycle(channel, "FlockWave[Look-1 | Channel-9 | FlockWave]", cycle);
                            break;
                        case ("FlockWaveBlues"):
                            setAutoCycle(channel, "FlockWaveBlues[Look-1 | Channel-9 | FlockWaveBlues]", cycle);
                            break;
                        case ("FlockWaveFiery"):
                            setAutoCycle(channel, "FlockWaveFiery[Look-1 | Channel-9 | FlockWaveFiery]", cycle);
                            break;
                        case ("FlockWaveGalaxies"):
                            setAutoCycle(channel, "FlockWaveGalaxies[Look-1 | Channel-9 | FlockWaveGalaxies]", cycle);
                            break;
                        case ("FlockWaveMercury"):
                            setAutoCycle(channel, "FlockWaveMercury[Look-1 | Channel-9 | FlockWaveMercury]", cycle);
                            break;
                        case ("FlockWaveOoze"):
                            setAutoCycle(channel, "FlockWaveOoze[Look-1 | Channel-9 | FlockWaveOoze]", cycle);
                            break;
                        case ("FlockWavePlanets"):
                            setAutoCycle(channel, "FlockWavePlanets[Look-1 | Channel-9 | FlockWavePlanets]", cycle);
                            break;
                        case ("FlockWaveTimewarp"):
                            setAutoCycle(channel, "FlockWaveTimewarp[Look-1 | Channel-9 | FlockWaveTimewarp]", cycle);
                            break;
                        case ("Metaballs"):
                            setAutoCycle(channel, "Metaballs[Look-1 | Channel-9 | Metaballs]", cycle);
                            break;
                        case ("Wasps"):
                            setAutoCycle(channel, "Wasps[Look-1 | Channel-9 | Wasps]", cycle);
                            break;
                        case ("Noise1"):
                            setAutoCycle(channel, "Noise[Look-1 | Channel-9 | Noise]1", cycle);
                            break;
                        case ("Noise2"):
                            setAutoCycle(channel, "Noise[Look-1 | Channel-9 | Noise]2", cycle);
                            break;
                        case ("Pong"):
                            setAutoCycle(channel, "Pong[Look-1 | Channel-9 | Pong]", cycle);
                            break;
                        case ("Psy"):
                            setAutoCycle(channel, "Psychedelia[Look-1 | Channel-9 | Psychedelia]", cycle);
                            break;
                        case ("Raindrops"):
                            setAutoCycle(channel, "Raindrops[Look-1 | Channel-9 | Raindrops]", cycle);
                            break;
                        case ("Rings"):
                            setAutoCycle(channel, "Rings[Look-1 | Channel-9 | Rings]", cycle);
                            break;
                        case ("Ripple"):
                            setAutoCycle(channel, "Ripple[Look-1 | Channel-9 | Ripple]", cycle);
                            break;
                        case ("Raven"):
                            setAutoCycle(channel, "RKPattern01[Look-1 | Channel-9 | RKPattern01]", cycle);
                            break;
                        case ("ShiftingPlane"):
                            setAutoCycle(channel, "ShiftingPlane[Look-1 | Channel-9 | ShiftingPlane]", cycle);
                            break;
                        case ("SimplexNoise"):
                            setAutoCycle(channel, "SimplexNoisePattern[Look-1 | Channel-9 | SimplexNoise]", cycle);
                            break;
                        case ("SineSphere"):
                            setAutoCycle(channel, "SineSphere[Look-1 | Channel-9 | SineSphere]", cycle);
                            break;
                        case ("solid"):
                            setAutoCycle(channel, "SolidColorPattern[Look-1 | Channel-9 | SolidColor]", cycle);
                            break;
                        case ("SpaceTime"):
                            setAutoCycle(channel, "SpaceTime[Look-1 | Channel-9 | SpaceTime]", cycle);
                            break;
                        case ("Sparkle"):
                            setAutoCycle(channel, "Sparkle[Look-1 | Channel-9 | Sparkle]", cycle);
                            break;
                        case ("Swarm"):
                            setAutoCycle(channel, "Swarm[Look-1 | Channel-9 | Swarm]", cycle);
                            break;
                        case ("TimPinwheels"):
                            setAutoCycle(channel, "TimPinwheels[Look-1 | Channel-9 | TimPinwheels]", cycle);
                            break;
                        case ("Swim"):
                            setAutoCycle(channel, "Swim[Look-1 | Channel-9 | Swim]", cycle);
                            break;
                        case ("ViolinWave"):
                            setAutoCycle(channel, "ViolinWave[Look-1 | Channel-9 | ViolinWave]", cycle);
                            break;
                        case ("Voronoi"):
                            setAutoCycle(channel, "Voronoi[Look-1 | Channel-9 | Voronoi]", cycle);
                            break;
                        case ("CubeFlash"):
                            setAutoCycle(channel, "CubeFlash[Look-1 | Channel-9 | CubeFlash]", cycle);
                            break;
                    }
                }

                if (parts[1].equals("GoPattern")) {
                // LXChannel channel = LXEngine.allChannels.get("LXChannel[Channel-9]");

                    switch (parts[2]) {
                        case ("AskewPlanes"):

                            System.out.println("TEST ASKEW PLANES");
                            channel.goPattern(LXChannel.allPatterns.get("AskewPlanes[Look-1 | Channel-9 | AskewPlanes]"));


                            System.out.println(LXChannel.allPatterns.get("AskewPlanes[Look-1 | Channel-9 | AskewPlanes]"));
                            
                            
    



                            break;
                        case ("Awaken"):
                            channel.goPattern(LXChannel.allPatterns.get("Awaken[Look-1 | Channel-9 | Awaken]"));
                            break;
                        case ("Balance"):
                            channel.goPattern(LXChannel.allPatterns.get("Balance[Look-1 | Channel-9 | Balance]"));
                            break;
                        case ("Blinders"):
                            channel.goPattern(LXChannel.allPatterns.get("Blinders[Look-1 | Channel-9 | Blinders]"));
                            break;
                        case ("BassPod"):
                            channel.goPattern(LXChannel.allPatterns.get("BassPod[Look-1 | Channel-9 | BassPod]"));
                            break;
                        case ("BouncyBalls"):
                            channel.goPattern(LXChannel.allPatterns.get("BouncyBalls[Look-1 | Channel-9 | BouncyBalls]"));
                            break;
                        case ("CrossSections"):
                            channel.goPattern(LXChannel.allPatterns.get("CrossSections[Look-1 | Channel-9 | CrossSections]"));
                            break;
                        case ("Crystalline"):
                            channel.goPattern(LXChannel.allPatterns.get("Crystalline[Look-1 | Channel-9 | Crystalline]"));
                            break;
                        case ("Diamonds"):
                            channel.goPattern(LXChannel.allPatterns.get("Diamonds[Look-1 | Channel-9 | Diamonds]"));
                            break;
                        case ("Explosions"):
                            channel.goPattern(LXChannel.allPatterns.get("Explosions[Look-1 | Channel-9 | Explosions]"));
                            break;
                        case ("FlockWave"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWave[Look-1 | Channel-9 | FlockWave]"));
                            break;
                        case ("FlockWaveBlues"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWaveBlues[Look-1 | Channel-9 | FlockWaveBlues]"));
                            break;
                        case ("FlockWaveFiery"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWaveFiery[Look-1 | Channel-9 | FlockWaveFiery]"));
                            break;
                        case ("FlockWaveGalaxies"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWaveGalaxies[Look-1 | Channel-9 | FlockWaveGalaxies]"));
                            break;
                        case ("FlockWaveMercury"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWaveMercury[Look-1 | Channel-9 | FlockWaveMercury]"));
                            break;
                        case ("FlockWaveOoze"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWaveOoze[Look-1 | Channel-9 | FlockWaveOoze]"));
                            break;
                        case ("FlockWavePlanets"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWavePlanets[Look-1 | Channel-9 | FlockWavePlanets]"));
                            break;
                        case ("FlockWaveTimewarp"):
                            channel.goPattern(LXChannel.allPatterns.get("FlockWaveTimewarp[Look-1 | Channel-9 | FlockWaveTimewarp]"));
                            break;
                        case ("Metaballs"):
                            channel.goPattern(LXChannel.allPatterns.get("Metaballs[Look-1 | Channel-9 | Metaballs]"));
                            break;
                        case ("Wasps"):
                            channel.goPattern(LXChannel.allPatterns.get("Wasps[Look-1 | Channel-9 | Wasps]"));
                            break;
                        case ("Noise1"):
                            channel.goPattern(LXChannel.allPatterns.get("Noise[Look-1 | Channel-9 | Noise]1"));
                            break;
                        case ("Noise2"):
                            channel.goPattern(LXChannel.allPatterns.get("Noise[Look-1 | Channel-9 | Noise]2"));
                            break;
                        case ("Pong"):
                            channel.goPattern(LXChannel.allPatterns.get("Pong[Look-1 | Channel-9 | Pong]"));
                            break;
                        case ("Pyschedelia"):
                            channel.goPattern(LXChannel.allPatterns.get("Psychedelia[Look-1 | Channel-9 | Psychedelia]"));
                            break;
                        case ("Raindrops"):
                            channel.goPattern(LXChannel.allPatterns.get("Raindrops[Look-1 | Channel-9 | Raindrops]"));
                            break;
                        case ("Rings"):
                            channel.goPattern(LXChannel.allPatterns.get("Rings[Look-1 | Channel-9 | Rings]"));
                            break;
                        case ("Ripple"):
                            channel.goPattern(LXChannel.allPatterns.get("Ripple[Look-1 | Channel-9 | Ripple]"));
                            break;
                        case ("Raven"):
                            channel.goPattern(LXChannel.allPatterns.get("RKPattern01[Look-1 | Channel-9 | RKPattern01]"));
                            break;
                        case ("ShiftingPlane"):
                            channel.goPattern(LXChannel.allPatterns.get("ShiftingPlane[Look-1 | Channel-9 | ShiftingPlane]"));
                            break;
                        case ("SimplexNoise"):
                            channel.goPattern(LXChannel.allPatterns.get("SimplexNoisePattern[Look-1 | Channel-9 | SimplexNoise]"));
                            break;
                        case ("SineSphere"):
                            channel.goPattern(LXChannel.allPatterns.get("SineSphere[Look-1 | Channel-9 | SineSphere]"));
                            break;
                        case ("SolidColor"):
                            channel.goPattern(LXChannel.allPatterns.get("SolidColorPattern[Look-1 | Channel-9 | SolidColor]"));
                            break;
                        case ("SpaceTime"):
                            channel.goPattern(LXChannel.allPatterns.get("SpaceTime[Look-1 | Channel-9 | SpaceTime]"));
                            break;
                        case ("Sparkle"):
                            channel.goPattern(LXChannel.allPatterns.get("Sparkle[Look-1 | Channel-9 | Sparkle]"));
                            break;
                        case ("Swim"):
                            channel.goPattern(LXChannel.allPatterns.get("Swim[Look-1 | Channel-9 | Swim]"));
                            break;
                        case ("Swarm"):
                            channel.goPattern(LXChannel.allPatterns.get("Swarm[Look-1 | Channel-9 | Swarm]"));
                            break;
                        case ("TimPinwheels"):
                            channel.goPattern(LXChannel.allPatterns.get("TimPinwheels[Look-1 | Channel-9 | TimPinwheels]"));
                            break;
                        case ("ViolinWave"):
                            channel.goPattern(LXChannel.allPatterns.get("ViolinWave[Look-1 | Channel-9 | ViolinWave]"));
                            break;
                        case ("Voronoi"):
                            channel.goPattern(LXChannel.allPatterns.get("Voronoi[Look-1 | Channel-9 | Voronoi]"));
                            break;
                        case ("CubeFlash"):
                            channel.goPattern(LXChannel.allPatterns.get("CubeFlash[Look-1 | Channel-9 | CubeFlash]"));
                            break;
                    }
                }


                if (parts[1].equals(ROUTE_LX)) {
                    if (parts[2].equals(ROUTE_ENGINE)) {
                        oscComponent(message, lx.engine, parts, 3);
                    } else if (parts[2].equals(ROUTE_MIDI)) {
                        oscMidi(message, parts, 3);
                    } else if (parts[2].equals(ROUTE_TEMPO)) {
                        oscTempo(message, parts, 3);
                    } else if (parts[2].equals(ROUTE_OUTPUT)) {
                        oscComponent(message, lx.engine.output, parts, 3);
                    } else if (parts[2].equals(ROUTE_AUDIO)) {
                        oscAudio(message, parts, 3);
                    } else if (parts[2].equals(ROUTE_PALETTE)) {
                        oscComponent(message, lx.palette, parts, 3);
                    } else if (parts[2].equals(ROUTE_MASTER)) {
                        oscChannel(message, lx.engine.masterChannel, parts, 3);
                    } else if (parts[2].equals(ROUTE_CHANNEL)) {
                        LXLook look = lx.engine.getFocusedLook();
                        if (parts[3].equals(ROUTE_FOCUSED)) {
                            oscChannel(message, look.getFocusedChannel(), parts, 4);
                        } else if (parts[3].matches("\\d+")) {
                            int channelIndex = getOscIndex(parts[3], look.channels.size(), "Channel");
                            if (channelIndex >= 0) {
                                oscChannel(message, look.getChannel(channelIndex), parts, 4);
                            }
                        } else {
                            oscChannel(message, look.getChannel(parts[3]), parts, 4);
                        }
                    } else if (parts[2].equals(ROUTE_LOOK)) {
                        if (parts[3].equals(ROUTE_FOCUSED)) {
                            oscLook(message, lx.engine.getFocusedLook(), parts, 4);
                        } else if (parts[3].matches("\\d+")) {
                            int lookIndex = getOscIndex(parts[3], lx.engine.getLooks().size(), "Look");
                            if (lookIndex >= 0) {
                                oscLook(message, lx.engine.getLook(lookIndex), parts, 4);
                            }
                        } else {
                            oscLook(message, lx.engine.getLook(parts[3]), parts, 4);
                        }
                    }
                }
            } catch (Exception x) {
                System.err.println("[OSC] Invalid message " + message.getAddressPattern().getValue() + ": " + x.getMessage());
            } finally {
                suppressEcho = false;
            }
        }

        private void oscTempo(OscMessage message, String[] parts, int index) {
            if (parts[index].equals(ROUTE_BEAT)) {
                lx.tempo.trigger(message.getInt()-1);
            } else {
                oscComponent(message, lx.tempo, parts, index);
            }
        }

        private void oscAudio(OscMessage message, String[] parts, int index) {
            if (parts[index].equals(ROUTE_INPUT)) {
                oscComponent(message, lx.engine.audio.input, parts, index+1);
            } else if (parts[index].equals(ROUTE_OUTPUT)) {
                oscComponent(message, lx.engine.audio.output, parts, index+1);
            } else if (parts[index].equals(ROUTE_METER)) {
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
                    lx.engine.midi.dispatch(new MidiNoteOn(channel, pitch, velocity));
                } else if (parts[index].equals(ROUTE_CC)) {
                    int value = message.getInt();
                    int cc = message.getInt();
                    int channel = message.getInt();
                    lx.engine.midi.dispatch(new MidiControlChange(channel, cc, value));
                } else if (parts[index].equals(ROUTE_PITCHBEND)) {
                    int msb = message.getInt();
                    int channel = message.getInt();
                    lx.engine.midi.dispatch(new MidiPitchBend(channel, msb));
                } else {
                    System.err.println("[OSC] Unrecognized MIDI message: " + message.getAddressPattern().getValue());
                }
            } catch (InvalidMidiDataException imdx) {
                System.err.println("[OSC] Invalid MIDI message: " + imdx.getLocalizedMessage());
            }
        }

        private int getOscIndex(String value, int size, String type) {
            int index = Integer.parseInt(value) - 1;
            if (index < 0 || index >= size) {
                System.err.println("[OSC] " + type + " index out of bounds: " + value + " (available: " + size + ")");
                return -1;
            }
            return index;
        }

        private void oscLook(OscMessage message, LXLook look, String[] parts, int index) {
            if (look == null || index >= parts.length) {
                System.err.println("[OSC] Look not found or route is incomplete");
                return;
            }
            if (parts[index].equals(ROUTE_CHANNEL)) {
                if (parts[index+1].equals(ROUTE_FOCUSED)) {
                    oscChannel(message, look.getFocusedChannel(), parts, index+2);
                } else if (parts[index+1].matches("\\d+")) {
                    int channelIndex = getOscIndex(parts[index+1], look.channels.size(), "Channel");
                    if (channelIndex >= 0) {
                        oscChannel(message, look.getChannel(channelIndex), parts, index+2);
                    }
                } else {
                    oscChannel(message, look.getChannel(parts[index+1]), parts, index+2);
                }
                return;
            }
            oscComponent(message, look, parts, index);
        }

        private void oscChannel(OscMessage message, LXBus channel, String[] parts, int index) {
            if (channel == null || index >= parts.length) {
                System.err.println("[OSC] Channel not found or route is incomplete");
                return;
            }
            if (channel instanceof LXChannel) {
                if (parts[index].equals(ROUTE_PATTERN)) {
                    if (parts[index+1].equals(ROUTE_ACTIVE)) {
                        oscPattern(message, ((LXChannel) channel).getActivePattern(), parts, index+2);
                    } else if (parts[index+1].matches("\\d+")) {
                        LXChannel lxChannel = (LXChannel) channel;
                        int patternIndex = getOscIndex(parts[index+1], lxChannel.getPatterns().size(), "Pattern");
                        if (patternIndex >= 0) {
                            oscPattern(message, lxChannel.getPattern(patternIndex), parts, index+2);
                        }
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
                    int effectIndex = getOscIndex(parts[index+1], channel.getEffects().size(), "Effect");
                    if (effectIndex >= 0) {
                        oscEffect(message, channel.getEffect(effectIndex), parts, index+2);
                    }
                } else {
                    oscEffect(message, channel.getEffect(parts[index+1]), parts, index+2);
                }
                return;
            }
            if (parts[index].equals(ROUTE_WARP)) {
                if (parts[index+1].matches("\\d+")) {
                    int warpIndex = getOscIndex(parts[index+1], channel.getWarps().size(), "Warp");
                    if (warpIndex >= 0) {
                        oscWarp(message, channel.getWarp(warpIndex), parts, index+2);
                    }
                } else {
                    oscWarp(message, channel.getWarp(parts[index+1]), parts, index+2);
                }
                return;
            }
            oscComponent(message, channel, parts, index);
        }

        private void oscEffect(OscMessage message, LXEffect effect, String[] parts, int index) {
            if (effect == null) {
                System.err.println("[OSC] Effect not found");
                return;
            }
            oscComponent(message, effect, parts, index);
        }

        private void oscPattern(OscMessage message, LXPattern pattern, String[] parts, int index) {
            if (pattern == null) {
                System.err.println("[OSC] Pattern not found");
                return;
            }
            if (parts[index].equals(ROUTE_EFFECT)) {
                LXChannel channel = pattern.getChannel();
                if (channel != null) {
                    List<LXEffect> effects = channel.getPatternEffects(pattern);
                    LXEffect effect = null;
                    if (parts[index+1].matches("\\d+")) {
                        int effectIndex = Integer.parseInt(parts[index+1]) - 1;
                        if (effectIndex >= 0 && effectIndex < effects.size()) {
                            effect = effects.get(effectIndex);
                        } else {
                            System.err.println("[OSC] Pattern effect index out of bounds: " + (effectIndex + 1));
                        }
                    } else {
                        String label = parts[index+1];
                        for (LXEffect e : effects) {
                            if (e.getLabel().equals(label)) {
                                effect = e;
                                break;
                            }
                        }
                        if (effect == null) {
                            System.err.println("[OSC] Pattern effect not found: " + label);
                        }
                    }
                    if (effect != null) {
                        oscEffect(message, effect, parts, index+2);
                    }
                }
                return;
            }
            if (parts[index].equals(ROUTE_WARP)) {
                LXChannel channel = pattern.getChannel();
                if (channel != null) {
                    List<LXWarp> warps = channel.getPatternWarps(pattern);
                    LXWarp warp = null;
                    if (parts[index+1].matches("\\d+")) {
                        int warpIndex = Integer.parseInt(parts[index+1]) - 1;
                        if (warpIndex >= 0 && warpIndex < warps.size()) {
                            warp = warps.get(warpIndex);
                        } else {
                            System.err.println("[OSC] Pattern warp index out of bounds: " + (warpIndex + 1));
                        }
                    } else {
                        String label = parts[index+1];
                        for (LXWarp w : warps) {
                            if (w.getLabel().equals(label)) {
                                warp = w;
                                break;
                            }
                        }
                        if (warp == null) {
                            System.err.println("[OSC] Pattern warp not found: " + label);
                        }
                    }
                    if (warp != null) {
                        oscWarp(message, warp, parts, index+2);
                    }
                }
                return;
            }
            oscComponent(message, pattern, parts, index);
        }

        private void oscWarp(OscMessage message, LXWarp warp, String[] parts, int index) {
            if (warp == null) {
                System.err.println("[OSC] Warp not found");
                return;
            }
            oscComponent(message, warp, parts, index);
        }

        private void oscComponent(OscMessage message, LXComponent component, String[] parts, int index) {
            if (component == null || index >= parts.length) {
                System.err.println("[OSC] Component not found or route is incomplete");
                return;
            }
            if (component instanceof LXModulationComponent && parts[index].equals(ROUTE_MODULATION)) {
                LXModulationEngine modulation = ((LXModulationComponent) component).getModulation();
                LXModulator modulator = modulation.getModulator(parts[index+1]);
                if (modulator == null) {
                    System.err.println("[OSC] Modulator '" + parts[index+1] + "' not found on " + component);
                    System.err.println("[OSC] Available modulators: " + modulation.getModulators());
                    return;
                }
                oscComponent(message, modulator, parts, index+2);
                return;
            }

            LXParameter parameter = component.getParameter(parts[index]);
            if (parameter == null) {
                System.err.println("[OSC] Component " + component + " does not have parameter: " + parts[index]);
                return;
            }
            if (parameter instanceof BooleanParameter) {
                boolean value = message.getBoolean();
                if (component instanceof LXPattern && parameter == ((LXPattern) component).autoCycleEligible) {
                    value = !value;
                }
                ((BooleanParameter)parameter).setValue(value);
            } else if (parameter instanceof StringParameter) {
                ((StringParameter) parameter).setValue(message.getString());
            } else if (parameter instanceof ColorParameter) {
                if (parts.length > index+1) {
                    if (parts[index+1].equals(ROUTE_HUE)) {
                        ((ColorParameter) parameter).hue.setNormalized(message.getFloat());
                    } else if (parts[index+1].equals(ROUTE_SATURATION)) {
                        ((ColorParameter) parameter).saturation.setNormalized(message.getFloat());
                    } else if (parts[index+1].equals(ROUTE_BRIGHTNESS)) {
                        ((ColorParameter) parameter).brightness.setNormalized(message.getFloat());
                    }
                } else {
                    ((ColorParameter) parameter).setColor(message.getInt());
                }
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

    private void setAutoCycle(LXChannel channel, String patternKey, boolean cycle) {
        LXPattern pattern = LXChannel.allPatterns.get(patternKey);
        if (pattern == null) {
            System.err.println("[OSC] Pattern not found: " + patternKey);
            return;
        }
        pattern.autoCycleEligible.setValue(cycle);
        if (cycle && channel != null) {
            channel.goPattern(pattern);
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
            this.socket.setBroadcast(true);
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

    private class EngineTransmitter extends Transmitter implements LXParameterListener, LXChannel.Listener, LXLook.Listener, LXModulationEngine.Listener {
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
            for (LXLook look : lx.engine.getLooks()) {
                registerComponent(look);
                for (LXComponent comp : look.allComponents()) {
                    registerComponent(comp);
                }
            }
            lx.engine.getFocusedLook().addListener(this);
        }

        private void registerChannel(LXChannel channel) {
            registerComponent(channel);
            for (LXComponent p : channel.allComponents()) {
                registerComponent(p);
            }
            channel.addListener(this);
        }

        private void unregisterChannel(LXChannel channel) {
            unregisterComponent(channel);
            for (LXComponent p : channel.allComponents()) {
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
            if (suppressEcho) {
                return;
            }
            if (transmitActive.isOn() && parameter.supportsOscTransmit()) {
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

        public void sendMessage(OscMessage message) {
            try {
                send(oscMessage);
            } catch (IOException iox) {
                System.err.println("[OSC] Failed to transmit: " + iox.getLocalizedMessage());
            }
            for (OscDestination dest : extraDestinations) {
                dest.sendMessage(message);
            }
        }

        @Override
        public void warpAdded(LXBus channel, LXWarp warp) {
            registerComponent(warp);
        }

        @Override
        public void warpRemoved(LXBus channel, LXWarp warp) {
            unregisterComponent(warp);
        }

        @Override
        public void warpMoved(LXBus channel, LXWarp warp) {}

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
        public void patternEffectAdded(LXChannel channel, LXPattern pattern, LXEffect effect) {
            registerComponent(effect);
        }

        @Override
        public void patternEffectRemoved(LXChannel channel, LXPattern pattern, LXEffect effect) {
            unregisterComponent(effect);
        }

        @Override
        public void patternEffectMoved(LXChannel channel, LXPattern pattern, LXEffect effect) {}

        @Override
        public void patternWarpAdded(LXChannel channel, LXPattern pattern, LXWarp warp) {
            registerComponent(warp);
        }

        @Override
        public void patternWarpRemoved(LXChannel channel, LXPattern pattern, LXWarp warp) {
            unregisterComponent(warp);
        }

        @Override
        public void patternWarpMoved(LXChannel channel, LXPattern pattern, LXWarp warp) {}

        @Override
        public void patternWillChange(LXChannel channel, LXPattern pattern, LXPattern nextPattern) {
            sendMessage(channel.getOscAddress() + "/" + ROUTE_NEXT_PATTERN, nextPattern.getIndex());
        }

        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
            sendMessage(channel.getOscAddress() + "/" + ROUTE_ACTIVE_PATTERN, pattern.getIndex());
            sendMessage(channel.getOscAddress() + "/" + ROUTE_NEXT_PATTERN, -1);
        }

        @Override
        public void channelAdded(LXLook look, LXChannel channel) {
            registerChannel(channel);
        }

        @Override
        public void channelRemoved(LXLook look, LXChannel channel) {
            unregisterChannel(channel);
        }

        @Override
        public void channelMoved(LXLook look, LXChannel channel) {}

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
            this.thread.setDaemon(true);
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

    public void addEngineListener(LXOscListener listener) {
        if (this.engineReceiver != null) {
            this.engineReceiver.addListener(listener);
        } else {
            this.engineListenerQueue.add(listener);
        }
    }

    private void startReceiver() {
        if (this.engineReceiver != null) {
            stopReceiver();
        }
        try {
            this.engineReceiver = receiver(this.receivePort.getValuei(), this.receiveHost.getString());
            this.engineReceiver.addListener(this.engineListener);
            for (LXOscListener listener : this.engineListenerQueue) {
                this.engineReceiver.addListener(listener);
            }
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
        synchronized (this.receivers) {
            this.receivers.add(receiver);
        }
        return receiver;
    }

    public Receiver receiver(int port) throws SocketException {
        return receiver(port, DEFAULT_MAX_PACKET_SIZE);
    }

    public Receiver receiver(int port, int bufferSize) throws SocketException {
        Receiver receiver = new Receiver(port, bufferSize);
        synchronized (this.receivers) {
            this.receivers.add(receiver);
        }
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
        synchronized (this.receivers) {
            for (Receiver receiver : this.receivers) {
                receiver.dispatch();
            }
        }
    }

    private static final String KEY_EXTRA_DESTINATIONS = "extraDestinations";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        JsonArray arr = new JsonArray();
        for (OscDestination dest : this.extraDestinations) {
            arr.add(dest.toJson());
        }
        obj.add(KEY_EXTRA_DESTINATIONS, arr);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);
        // Clear existing extra destinations
        for (OscDestination dest : new ArrayList<OscDestination>(this.extraDestinations)) {
            removeDestination(dest);
        }
        if (obj.has(KEY_EXTRA_DESTINATIONS)) {
            JsonArray arr = obj.getAsJsonArray(KEY_EXTRA_DESTINATIONS);
            for (JsonElement el : arr) {
                OscDestination dest = addDestination();
                dest.loadJson(el.getAsJsonObject());
            }
        }
    }

}
