/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.midi;

import heronarts.lx.LXDeck;
import heronarts.lx.LXEngine;
import heronarts.lx.pattern.LXPattern;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import rwmidi.MidiInputDevice;
import rwmidi.MidiOutputDevice;

/**
 * Note and CC constants for all the APC40 controls
 */
public class APC40 extends LXMidiDevice {

    // CC numbers

    public final static int VOLUME = 7;
    public final static int MASTER_FADER = 14;
    public final static int CUE_LEVEL = 47;
    public final static int DEVICE_CONTROL = 16;
    public final static int DEVICE_CONTROL_LED_MODE = 24;
    public final static int TRACK_CONTROL = 48;
    public final static int TRACK_CONTROL_LED_MODE = 56;
    public final static int CROSSFADER = 15;

    // Note numbers

    public final static int CLIP_LAUNCH = 53;
    public final static int SCENE_LAUNCH = 82;

    public final static int CLIP_STOP = 52;
    public final static int STOP_ALL_CLIPS = 81;

    public final static int TRACK_SELECTION = 51;
    public final static int MASTER_TRACK = 80;

    public final static int ACTIVATOR = 50;
    public final static int SOLO_CUE = 49;
    public final static int RECORD_ARM = 48;

    public final static int PAN = 87;
    public final static int SEND_A = 88;
    public final static int SEND_B = 89;
    public final static int SEND_C = 90;

    public final static int SHIFT = 98;

    public final static int BANK_UP = 94;
    public final static int BANK_DOWN = 95;
    public final static int BANK_RIGHT = 96;
    public final static int BANK_LEFT = 97;

    public final static int TAP_TEMPO = 99;
    public final static int NUDGE_PLUS = 100;
    public final static int NUDGE_MINUS = 101;

    public final static int CLIP_TRACK = 58;
    public final static int DEVICE_ON_OFF = 59;
    public final static int LEFT_ARROW = 60;
    public final static int RIGHT_ARROW = 61;

    public final static int DETAIL_VIEW = 62;
    public final static int REC_QUANTIZATION = 63;
    public final static int MIDI_OVERDUB = 64;
    public final static int METRONOME = 65;

    public final static int PLAY = 91;
    public final static int STOP = 92;
    public final static int REC = 93;

    // LED color values

    public static final int OFF = 0;
    public static final int GREEN = 1;
    public static final int GREEN_BLINK = 2;
    public static final int RED = 3;
    public static final int RED_BLINK = 4;
    public static final int YELLOW = 5;
    public static final int YELLOW_BLINK = 6;

    // Encoder ring modes

    public final static int LED_MODE_OFF = 0;
    public final static int LED_MODE_SINGLE = 1;
    public final static int LED_MODE_VOLUME = 2;
    public final static int LED_MODE_PAN = 3;

    // APC Modes

    public final static byte GENERIC = 0x40;
    public final static byte MODE_ABLETON = 0x41;
    public final static byte MODE_ALTERNATE_ABLETON = 0x42;

    private final static int NUM_TRACK_CONTROL_KNOBS = 8;
    private final static int NUM_DEVICE_CONTROL_KNOBS = 8;

    public final static String DEVICE_NAME = "APC40";

    public static MidiInputDevice getInputDevice() {
        return LXMidiDevice.getInputDevice(DEVICE_NAME);
    }

    public static MidiOutputDevice getOutputDevice() {
        return LXMidiDevice.getOutputDevice(DEVICE_NAME);
    }

    public static APC40 getAPC40() {
        MidiInputDevice inputDevice = getInputDevice();
        MidiOutputDevice outputDevice = getOutputDevice();
        if (inputDevice != null) {
            return new APC40(inputDevice, outputDevice);
        }
        return null;
    }

    public APC40(MidiInputDevice input) {
        this(input, null);
    }

    public APC40(MidiInputDevice input, MidiOutputDevice output) {
        super(input, output);
    }

    public APC40 setMode(byte mode) {
        byte[] apcModeSysex = new byte[] { (byte) 0xf0, // sysex start
                (byte) 0x47, // manufacturers id
                (byte) 0x00, // device id
                (byte) 0x73, // product model id
                (byte) 0x60, // message
                (byte) 0x00, // bytes MSB
                (byte) 0x04, // bytes LSB
                mode,
                (byte) 0x08, // version maj
                (byte) 0x01, // version min
                (byte) 0x01, // version bugfix
                (byte) 0xf7, // sysex end
        };
        sendSysex(apcModeSysex);
        return this;
    }

    private LXDeck deviceControlDeck = null;

    private final LXDeck.AbstractListener deviceControlListener = new LXDeck.AbstractListener() {
        @Override
        public void patternDidChange(LXDeck deck, LXPattern pattern) {
            bindDeviceControlKnobs(pattern);
        }
    };

    public APC40 bindDeviceControlKnobs(final LXEngine engine) {
        engine.focusedDeck.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                bindDeviceControlKnobs(engine.getFocusedDeck());
            }
        });
        bindDeviceControlKnobs(engine.getFocusedDeck());
        return this;
    }

    public APC40 bindDeviceControlKnobs(LXDeck deck) {
        if (this.deviceControlDeck != deck) {
            if (this.deviceControlDeck != null) {
                this.deviceControlDeck.removeListener(this.deviceControlListener);
            }
            this.deviceControlDeck = deck;
            this.deviceControlDeck.addListener(this.deviceControlListener);
        }
        bindDeviceControlKnobs(deck.getActivePattern());
        return this;
    }

    public APC40 bindDeviceControlKnobs(LXPattern pattern) {
        int parameterIndex = 0;
        for (LXParameter parameter : pattern.getParameters()) {
            if (parameter instanceof LXListenableNormalizedParameter) {
                bindController(parameter, 0, DEVICE_CONTROL + parameterIndex);
                if (++parameterIndex >= NUM_DEVICE_CONTROL_KNOBS) {
                    break;
                }
            }
        }
        while (parameterIndex < NUM_DEVICE_CONTROL_KNOBS) {
            unbindController(0, DEVICE_CONTROL + parameterIndex);
            sendController(0, DEVICE_CONTROL + parameterIndex, 0);
            ++parameterIndex;
        }
        return this;
    }

}
