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
            try {
                String[] parts = message.getAddressPattern().getValue().split("/");
                LXChannel channel = LXLook.allChannels.get("LXChannel[Look-1 | Channel-9]");
                // LXChannel channel = LXLook.allChannels.get("LXChannel[Channel-9]");


                // if (parts[1].equals("AutoCycle")) {
                //     switch (parts[2]) {
                //         case ("AskewPlanes"):
                //             if (LXChannel.allPatterns.get("AskewPlanes[Channel-9 | AskewPlanes]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("AskewPlanes[Channel-9 | AskewPlanes]").autoCycleEligible.toggle();
                //             } else {
                //                 channel.goPattern(LXChannel.allPatterns.get("AskewPlanes[Channel-9 | AskewPlanes]"));
                //                 LXChannel.allPatterns.get("AskewPlanes[Channel-9 | AskewPlanes]").toggleAutoCycleEligible();
                //             }
                //             break;
                //         case ("Awaken"):
                //             if (LXChannel.allPatterns.get("Awaken[Channel-9 | Awaken]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Awaken[Channel-9 | Awaken]").autoCycleEligible.toggle();
                //             } else {
                //                 channel.goPattern(LXChannel.allPatterns.get("Awaken[Channel-9 | Awaken]"));
                //                 LXChannel.allPatterns.get("Awaken[Channel-9 | Awaken]").toggleAutoCycleEligible();
                //             }
                //             break;
                //         case ("Balance"):
                //             if (LXChannel.allPatterns.get("Balance[Channel-9 | Balance]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Balance[Channel-9 | Balance]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Balance[Channel-9 | Balance]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Balance[Channel-9 | Balance]"));
                //             }
                //             break;
                //         case ("Blinders"):
                //             if (LXChannel.allPatterns.get("Blinders[Channel-9 | Blinders]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Blinders[Channel-9 | Blinders]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Blinders[Channel-9 | Blinders]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Blinders[Channel-9 | Blinders]"));
                //             }
                //             break;
                //         case ("Bubbles"):
                //             if (LXChannel.allPatterns.get("Bubbles[Channel-9 | Bubbles]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Bubbles[Channel-9 | Bubbles]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Bubbles[Channel-9 | Bubbles]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Bubbles[Channel-9 | Bubbles]"));
                //             }
                //             break;
                //         case ("BouncyBalls"):
                //             if (LXChannel.allPatterns.get("BouncyBalls[Channel-9 | BouncyBalls]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("BouncyBalls[Channel-9 | BouncyBalls]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("BouncyBalls[Channel-9 | BouncyBalls]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("BouncyBalls[Channel-9 | BouncyBalls]"));
                //             }
                //             break;
                //         case ("CrossSections"):
                //             if (LXChannel.allPatterns.get("CrossSections[Channel-9 | CrossSections]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("CrossSections[Channel-9 | CrossSections]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("CrossSections[Channel-9 | CrossSections]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("CrossSections[Channel-9 | CrossSections]"));
                //             }
                //             break;
                //         case ("Crystalline"):
                //             if (LXChannel.allPatterns.get("Crystalline[Channel-9 | Crystalline]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Crystalline[Channel-9 | Crystalline]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Crystalline[Channel-9 | Crystalline]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Crystalline[Channel-9 | Crystalline]"));
                //             }
                //             break;
                //         case ("Diamonds"):
                //             if (LXChannel.allPatterns.get("Diamonds[Channel-9 | Diamonds]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Diamonds[Channel-9 | Diamonds]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Diamonds[Channel-9 | Diamonds]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Diamonds[Channel-9 | Diamonds]"));
                //             }
                //             break;
                //         case ("Explosions"):
                //             if (LXChannel.allPatterns.get("Explosions[Channel-9 | Explosions]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Explosions[Channel-9 | Explosions]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Explosions[Channel-9 | Explosions]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Explosions[Channel-9 | Explosions]"));
                //             }
                //             break;
                //         case ("FlockWave"):
                //             if (LXChannel.allPatterns.get("FlockWave[Channel-9 | FlockWave]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWave[Channel-9 | FlockWave]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWave[Channel-9 | FlockWave]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWave[Channel-9 | FlockWave]"));
                //             }
                //             break;
                //         case ("FlockWaveBlues"):
                //             if (LXChannel.allPatterns.get("FlockWaveBlues[Channel-9 | FlockWaveBlues]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWaveBlues[Channel-9 | FlockWaveBlues]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWaveBlues[Channel-9 | FlockWaveBlues]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWaveBlues[Channel-9 | FlockWaveBlues]"));
                //             }
                //             break;
                //         case ("FlockWaveFiery"):
                //             if (LXChannel.allPatterns.get("FlockWaveFiery[Channel-9 | FlockWaveFiery]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWaveFiery[Channel-9 | FlockWaveFiery]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWaveFiery[Channel-9 | FlockWaveFiery]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWaveFiery[Channel-9 | FlockWaveFiery]"));
                //             }
                //             break;
                //         case ("FlockWaveGalaxies"):
                //             if (LXChannel.allPatterns.get("FlockWaveGalaxies[Channel-9 | FlockWaveGalaxies]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWaveGalaxies[Channel-9 | FlockWaveGalaxies]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWaveGalaxies[Channel-9 | FlockWaveGalaxies]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWaveGalaxies[Channel-9 | FlockWaveGalaxies]"));

                //             }
                //             break;
                //         case ("FlockWaveMercury"):
                //             if (LXChannel.allPatterns.get("FlockWaveMercury[Channel-9 | FlockWaveMercury]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWaveMercury[Channel-9 | FlockWaveMercury]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWaveMercury[Channel-9 | FlockWaveMercury]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWaveMercury[Channel-9 | FlockWaveMercury]"));
                //             }
                //             break;
                //         case ("FlockWaveOoze"):
                //             if (LXChannel.allPatterns.get("FlockWaveOoze[Channel-9 | FlockWaveOoze]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWaveOoze[Channel-9 | FlockWaveOoze]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWaveOoze[Channel-9 | FlockWaveOoze]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWaveOoze[Channel-9 | FlockWaveOoze]"));
                //             }
                //             break;
                //         case ("FlockWavePlanets"):
                //             if (LXChannel.allPatterns.get("FlockWavePlanets[Channel-9 | FlockWavePlanets]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWavePlanets[Channel-9 | FlockWavePlanets]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWavePlanets[Channel-9 | FlockWavePlanets]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWavePlanets[Channel-9 | FlockWavePlanets]"));
                //             }
                //             break;
                //         case ("FlockWaveTimewarp"):
                //             if (LXChannel.allPatterns.get("FlockWaveTimewarp[Channel-9 | FlockWaveTimewarp]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("FlockWaveTimewarp[Channel-9 | FlockWaveTimewarp]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("FlockWaveTimewarp[Channel-9 | FlockWaveTimewarp]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("FlockWaveTimewarp[Channel-9 | FlockWaveTimewarp]"));
                //             }
                //             break;
                //         case ("Metaballs"):
                //             if (LXChannel.allPatterns.get("Metaballs[Channel-9 | Metaballs]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Metaballs[Channel-9 | Metaballs]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Metaballs[Channel-9 | Metaballs]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Metaballs[Channel-9 | Metaballs]"));
                //             }
                //             break;
                //         case ("Wasps"):
                //             if (LXChannel.allPatterns.get("Wasps[Channel-9 | Wasps]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Wasps[Channel-9 | Wasps]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Wasps[Channel-9 | Wasps]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Wasps[Channel-9 | Wasps]"));
                //             }
                //             break;
                //         case ("Noise1"):
                //             if (LXChannel.allPatterns.get("Noise[Channel-9 | Noise]1").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Noise[Channel-9 | Noise]1").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Noise[Channel-9 | Noise]1").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Noise[Channel-9 | Noise]1"));
                //             }
                //             break;
                //         case ("Noise2"):
                //             if (LXChannel.allPatterns.get("Noise[Channel-9 | Noise]2").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Noise[Channel-9 | Noise]2").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Noise[Channel-9 | Noise]2").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Noise[Channel-9 | Noise]2"));
                //             }
                //             break;
                //         case ("Pong"):
                //             if (LXChannel.allPatterns.get("Pong[Channel-9 | Pong]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Pong[Channel-9 | Pong]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Pong[Channel-9 | Pong]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Pong[Channel-9 | Pong]"));
                //             }
                //             break;
                //         case ("Psy"):
                //             if (LXChannel.allPatterns.get("Psychedelia[Channel-9 | Psychedelia]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Psychedelia[Channel-9 | Psychedelia]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Psychedelia[Channel-9 | Psychedelia]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Psychedelia[Channel-9 | Psychedelia]"));

                //             }
                //             break;
                //         case ("Raindrops"):
                //             if (LXChannel.allPatterns.get("Raindrops[Channel-9 | Raindrops]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Raindrops[Channel-9 | Raindrops]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Raindrops[Channel-9 | Raindrops]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Raindrops[Channel-9 | Raindrops]"));

                //             }
                //             break;
                //         case ("Rings"):
                //             if (LXChannel.allPatterns.get("Rings[Channel-9 | Rings]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Rings[Channel-9 | Rings]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Rings[Channel-9 | Rings]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Rings[Channel-9 | Rings]"));

                //             }
                //             break;
                //         case ("Ripple"):
                //             if (LXChannel.allPatterns.get("Ripple[Channel-9 | Ripple]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Ripple[Channel-9 | Ripple]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Ripple[Channel-9 | Ripple]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Ripple[Channel-9 | Ripple]"));

                //             }
                //             break;
                //         case ("Raven"):
                //             if (LXChannel.allPatterns.get("RKPattern01[Channel-9 | RKPattern01]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("RKPattern01[Channel-9 | RKPattern01]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("RKPattern01[Channel-9 | RKPattern01]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("RKPattern01[Channel-9 | RKPattern01]"));
                //             }
                //             break;
                //         case ("ShiftingPlane"):
                //             if (LXChannel.allPatterns.get("ShiftingPlane[Channel-9 | ShiftingPlane]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("ShiftingPlane[Channel-9 | ShiftingPlane]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("ShiftingPlane[Channel-9 | ShiftingPlane]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("ShiftingPlane[Channel-9 | ShiftingPlane]"));

                //             }
                //             break;
                //         case ("SimplexNoise"):
                //             if (LXChannel.allPatterns.get("SimplexNoisePattern[Channel-9 | SimplexNoise]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("SimplexNoisePattern[Channel-9 | SimplexNoise]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("SimplexNoisePattern[Channel-9 | SimplexNoise]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("SimplexNoisePattern[Channel-9 | SimplexNoise]"));

                //             }
                //             break;
                //         case ("SineSphere"):
                //             if (LXChannel.allPatterns.get("SineSphere[Channel-9 | SineSphere]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("SineSphere[Channel-9 | SineSphere]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("SineSphere[Channel-9 | SineSphere]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("SineSphere[Channel-9 | SineSphere]"));

                //             }
                //             break;
                //         case ("solid"):
                //             if (LXChannel.allPatterns.get("SolidColorPattern[Channel-9 | SolidColor]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("SolidColorPattern[Channel-9 | SolidColor]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("SolidColorPattern[Channel-9 | SolidColor]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("SolidColorPattern[Channel-9 | SolidColor]"));
                //             }
                //             break;
                //         case ("SpaceTime"):
                //             if (LXChannel.allPatterns.get("SpaceTime[Channel-9 | SpaceTime]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("SpaceTime[Channel-9 | SpaceTime]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("SpaceTime[Channel-9 | SpaceTime]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("SpaceTime[Channel-9 | SpaceTime]"));
                //             }
                //             break;
                //         case ("Sparkle"):
                //             if (LXChannel.allPatterns.get("Sparkle[Channel-9 | Sparkle]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Sparkle[Channel-9 | Sparkle]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Sparkle[Channel-9 | Sparkle]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Sparkle[Channel-9 | Sparkle]"));
                //             }
                //             break;
                //         case ("Swarm"):
                //             if (LXChannel.allPatterns.get("Swarm[Channel-9 | Swarm]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Swarm[Channel-9 | Swarm]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Swarm[Channel-9 | Swarm]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Swarm[Channel-9 | Swarm]"));
                //             }
                //             break;
                //         case ("TimPinwheels"):
                //             if (LXChannel.allPatterns.get("TimPinwheels[Channel-9 | TimPinwheels]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("TimPinwheels[Channel-9 | TimPinwheels]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("TimPinwheels[Channel-9 | TimPinwheels]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("TimPinwheels[Channel-9 | TimPinwheels]"));
                //             }
                //             break;
                //         case ("Swim"):
                //             if (LXChannel.allPatterns.get("Swim[Channel-9 | Swim]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Swim[Channel-9 | Swim]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Swim[Channel-9 | Swim]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Swim[Channel-9 | Swim]"));

                //             }
                //             break;
                //         case ("ViolinWave"):
                //             if (LXChannel.allPatterns.get("ViolinWave[Channel-9 | ViolinWave]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("ViolinWave[Channel-9 | ViolinWave]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("ViolinWave[Channel-9 | ViolinWave]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("ViolinWave[Channel-9 | ViolinWave]"));
                //             }
                //             break;
                //         case ("Voronoi"):
                //             if (LXChannel.allPatterns.get("Voronoi[Channel-9 | Voronoi]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("Voronoi[Channel-9 | Voronoi]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("Voronoi[Channel-9 | Voronoi]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("Voronoi[Channel-9 | Voronoi]"));

                //             }
                //             break;
                //         case ("CubeFlash"):
                //             if (LXChannel.allPatterns.get("CubeFlash[Channel-9 | CubeFlash]").autoCycleEligible.isOn()) {
                //                 LXChannel.allPatterns.get("CubeFlash[Channel-9 | CubeFlash]").autoCycleEligible.toggle();
                //             } else {
                //                 LXChannel.allPatterns.get("CubeFlash[Channel-9 | CubeFlash]").toggleAutoCycleEligible();
                //                 channel.goPattern(LXChannel.allPatterns.get("CubeFlash[Channel-9 | CubeFlash]"));

                //             }
                //             break;
                //     }
                // }

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
                        case ("Bubbles"):
                            channel.goPattern(LXChannel.allPatterns.get("Bubbles[Look-1 | Channel-9 | Bubbles]"));
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
                            oscChannel(message, look.getChannel(Integer.parseInt(parts[3]) - 1), parts, 4);
                        } else {
                            oscChannel(message, look.getChannel(parts[3]), parts, 4);
                        }
                    } else if (parts[2].equals(ROUTE_LOOK)) {
                        if (parts[3].equals(ROUTE_FOCUSED)) {
                            oscLook(message, lx.engine.getFocusedLook(), parts, 4);
                        } else if (parts[3].matches("\\d+")) {
                            oscLook(message, lx.engine.getLook(Integer.parseInt(parts[3]) - 1), parts, 4);
                        } else {
                            oscLook(message, lx.engine.getLook(parts[3]), parts, 4);
                        }
                    }
                }
            } catch (Exception x) {
                System.err.println("[OSC] No route for message: " + message.getAddressPattern().getValue());
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

        private void oscLook(OscMessage message, LXLook look, String[] parts, int index) {
            if (parts[index].equals(ROUTE_CHANNEL)) {
                if (parts[index+1].equals(ROUTE_FOCUSED)) {
                    oscChannel(message, look.getFocusedChannel(), parts, index+2);
                } else if (parts[index+1].matches("\\d+")) {
                    oscChannel(message, look.getChannel(Integer.parseInt(parts[index+1]) - 1), parts, index+2);
                } else {
                    oscChannel(message, look.getChannel(parts[index+1]), parts, index+2);
                }
                return;
            }
            oscComponent(message, look, parts, index);
        }

        private void oscChannel(OscMessage message, LXBus channel, String[] parts, int index) {
            if (channel instanceof LXChannel) {
                if (parts[index].equals(ROUTE_PATTERN)) {
                    if (parts[index+1].equals(ROUTE_ACTIVE)) {
                        oscPattern(message, ((LXChannel) channel).getActivePattern(), parts, index+2);
                    } else if (parts[index+1].matches("\\d+")) {
                        oscPattern(message, ((LXChannel) channel).getPattern(Integer.parseInt(parts[index+1]) - 1), parts, index+2);
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
                    oscEffect(message, channel.getEffect(Integer.parseInt(parts[index+1]) - 1), parts, index+2);
                } else {
                    oscEffect(message, channel.getEffect(parts[index+1]), parts, index+2);
                }
                return;
            }
            if (parts[index].equals(ROUTE_WARP)) {
                if (parts[index+1].matches("\\d+")) {
                    oscWarp(message, channel.getWarp(Integer.parseInt(parts[index+1]) - 1), parts, index+2);
                } else {
                    oscWarp(message, channel.getWarp(parts[index+1]), parts, index+2);
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

        private void oscWarp(OscMessage message, LXWarp warp, String[] parts, int index) {
            oscComponent(message, warp, parts, index);
        }

        private void oscComponent(OscMessage message, LXComponent component, String[] parts, int index) {
            if (component instanceof LXModulationComponent && parts[index].equals(ROUTE_MODULATION)) {
                oscComponent(message, ((LXModulationComponent) component).getModulation().getModulator(parts[index+1]), parts, index+2);
                return;
            }

            LXParameter parameter = component.getParameter(parts[index]);
            if (parameter == null) {
                System.err.println("[OSC] Component " + component + " does not have parameter: " + parts[index]);
                return;
            }
            if (parameter instanceof BooleanParameter) {
                ((BooleanParameter)parameter).setValue(message.getBoolean());
            } else if (parameter instanceof StringParameter) {
                ((StringParameter) parameter).setValue(message.getString());
            } else if (parameter instanceof ColorParameter) {
                if (parts.length >= index+1) {
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

}
