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

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

import rwmidi.Controller;
import rwmidi.MidiInput;
import rwmidi.MidiInputDevice;
import rwmidi.MidiOutput;
import rwmidi.MidiOutputDevice;
import rwmidi.Note;

public class LXMidiDevice {

    private final static int MIDI_RANGE = 128;
    private final static int MIDI_CHANNELS = 16;
    private final static int NUM_BINDINGS = MIDI_RANGE * MIDI_CHANNELS;

    private final static int MIDI_MAX = MIDI_RANGE - 1;

    public final static int ANY_CHANNEL = -1;
    public final static int NOTE_VELOCITY = -1;
    public final static int CC_VALUE = -1;

    private final static int OFF = -1;
    public final static int DIRECT = 1;
    public final static int TOGGLE = 2;

    // TODO(mcslee): implement LXModulator controls
    // public final static int START = 3;
    // public final static int STOP = 4;
    // public final static int START_STOP = 5;
    // public final static int TRIGGER = 6;

    private abstract class Binding implements LXParameterListener {

        protected final LXParameter parameter;
        protected final boolean isListening;

        private Binding(LXParameter parameter) {
            if (parameter == null) {
                throw new IllegalArgumentException("Cannot bind to null parameter");
            }
            this.parameter = parameter;
            if ((output != null) && (parameter instanceof LXListenableParameter)) {
                ((LXListenableParameter) parameter).addListener(this);
                this.isListening = true;
            } else {
                this.isListening = false;
            }
        }

        protected void unbind() {
            if ((output != null) && (parameter instanceof LXListenableParameter)) {
                ((LXListenableParameter) parameter).removeListener(this);
            }
        }

        protected void assertChannel(int channel) {
            if (channel < 0 || channel >= MIDI_CHANNELS) {
                throw new IllegalArgumentException("Invalid MIDI channel: " + channel);
            }
        }

        protected void assertValue(int value) {
            if (value < 0 || value >= MIDI_RANGE) {
                throw new IllegalArgumentException("Invalid MIDI value: " + value);
            }
        }
    }

    private class NoteBinding extends Binding {

        private final int channel;
        private final int number;
        private final int mode;
        private final int value;

        private NoteBinding(LXParameter parameter, int channel, int number,
                int mode, int value) {
            super(parameter);
            this.channel = channel;
            this.number = number;
            this.mode = mode;
            this.value = value;

            assertChannel(channel);
            assertValue(number);

            switch (this.mode) {
            case OFF:
            case DIRECT:
            case TOGGLE:
                if (!(parameter instanceof LXNormalizedParameter)) {
                    throw new IllegalArgumentException(
                            "TOGGLE mode requires LXNormalizedParameter");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid NoteBinding mode: " + mode);
            }

            if (this.isListening) {
                onParameterChanged(this.parameter);
            }
        }

        private void noteOnReceived(Note note) {
            switch (this.mode) {
            case DIRECT:
                switch (this.value) {
                case NOTE_VELOCITY:
                    if (this.parameter instanceof LXNormalizedParameter) {
                        double normalized = note.getVelocity() / (double) MIDI_MAX;
                        ((LXNormalizedParameter) this.parameter).setNormalized(normalized);
                    } else {
                        this.parameter.setValue(note.getVelocity());
                    }
                    break;
                default:
                    this.parameter.setValue(this.value);
                    break;
                }
                break;

            // Toggle mode for BooleanParameter
            case TOGGLE:
                LXNormalizedParameter normalized = (LXNormalizedParameter) this.parameter;
                if (this.parameter instanceof BooleanParameter) {
                    ((BooleanParameter) this.parameter).toggle();
                } else {
                    if (normalized.getNormalized() > 0) {
                        normalized.setNormalized(0);
                    } else {
                        normalized.setNormalized(1);
                    }
                }
                break;
            }
        }

        private void noteOffReceived(Note note) {
            switch (this.mode) {
            case OFF:
                if (this.parameter instanceof BooleanParameter) {
                    ((BooleanParameter) this.parameter).setValue(false);
                } else if (this.parameter instanceof LXNormalizedParameter) {
                    ((LXNormalizedParameter) this.parameter).setNormalized(0);
                } else {
                    this.parameter.setValue(0);
                }
                break;
            }
        }

        public void onParameterChanged(LXParameter parameter) {
            if (output != null) {
                double parameterValue = this.parameter.getValue();
                if (this.parameter instanceof LXNormalizedParameter) {
                    parameterValue = MIDI_MAX
                            * ((LXNormalizedParameter) this.parameter).getNormalized();
                }
                if (parameterValue == 0) {
                    if (this.mode == TOGGLE || this.mode == OFF) {
                        output.sendNoteOff(this.channel, this.number, 0);
                    }
                } else {
                    int velocity;
                    switch (this.value) {
                    case NOTE_VELOCITY:
                        velocity = (int) LXUtils.constrain(parameterValue, 0, MIDI_MAX);
                        break;
                    default:
                        velocity = LXUtils.constrain(this.value, 0, MIDI_MAX);
                        break;
                    }
                    output.sendNoteOn(this.channel, this.number, velocity);
                }
            }
        }
    }

    private class ControllerBinding extends Binding {
        private final int channel;
        private final int cc;
        private final int value;

        private ControllerBinding(LXParameter parameter, int channel, int cc,
                int value) {
            super(parameter);
            this.channel = channel;
            this.cc = cc;
            this.value = value;

            assertChannel(channel);
            assertValue(cc);

            if (this.isListening) {
                onParameterChanged(this.parameter);
            }
        }

        private void controllerChangeReceived(Controller controller) {
            int controllerValue = controller.getValue();
            if (controllerValue == 0) {
                if (this.parameter instanceof LXNormalizedParameter) {
                    ((LXNormalizedParameter) this.parameter).setNormalized(0);
                } else {
                    this.parameter.setValue(0);
                }
            } else {
                switch (this.value) {
                case CC_VALUE:
                    if (this.parameter instanceof LXNormalizedParameter) {
                        ((LXNormalizedParameter) this.parameter)
                                .setNormalized(controllerValue / (double) MIDI_MAX);
                    } else {
                        this.parameter.setValue(controllerValue);
                    }
                    break;
                default:
                    this.parameter.setValue(this.value);
                    break;
                }
            }
        }

        public void onParameterChanged(LXParameter parameter) {
            if (output != null) {
                double parameterValue = this.parameter.getValue();
                if (this.parameter instanceof LXNormalizedParameter) {
                    double normalized = ((LXNormalizedParameter) this.parameter).getNormalized();
                    parameterValue = MIDI_MAX * normalized;
                }
                if (parameterValue == 0) {
                    output.sendController(this.channel, this.cc, 0);
                } else {
                    int sendValue;
                    switch (this.value) {
                    case CC_VALUE:
                        sendValue = (int) LXUtils.constrain(parameterValue, 0, MIDI_MAX);
                        break;
                    default:
                        sendValue = LXUtils.constrain(this.value, 0, MIDI_MAX);
                        break;
                    }
                    output.sendController(this.channel, this.cc, sendValue);
                }
            }
        }
    }

    private final NoteBinding[] noteOnBindings;
    private final NoteBinding[] noteOffBindings;
    private final ControllerBinding[] controllerBindings;

    private final MidiInput input;
    private final MidiOutput output;

    public LXMidiDevice(MidiInputDevice input) {
        this(input, null);
    }

    public LXMidiDevice(MidiOutputDevice output) {
        this(null, output);
    }

    public LXMidiDevice(MidiInputDevice input, MidiOutputDevice output) {
        this.input = (input != null) ? input.createInput(this) : null;
        this.output = (output != null) ? output.createOutput() : null;
        this.noteOnBindings = new NoteBinding[NUM_BINDINGS];
        this.noteOffBindings = new NoteBinding[NUM_BINDINGS];
        this.controllerBindings = new ControllerBinding[NUM_BINDINGS];
        for (int i = 0; i < NUM_BINDINGS; ++i) {
            this.noteOnBindings[i] = null;
            this.noteOffBindings[i] = null;
            this.controllerBindings[i] = null;
        }
    }

    public MidiInput getInput() {
        return this.input;
    }

    public MidiOutput getOutput() {
        return this.output;
    }

    public LXMidiDevice bindNote(LXParameter parameter, int number) {
        return bindNote(parameter, number, NOTE_VELOCITY);
    }

    public LXMidiDevice bindNote(LXParameter parameter, int channel, int number) {
        return bindNote(parameter, channel, number, DIRECT);
    }

    public LXMidiDevice bindNote(LXParameter parameter, int channel, int number,
            int mode) {
        return bindNote(parameter, channel, number, mode, NOTE_VELOCITY);
    }

    public LXMidiDevice bindNote(LXParameter parameter, int channel, int number,
            int mode, int value) {
        bindNoteOn(parameter, channel, number, mode, value);
        if (mode == DIRECT) {
            bindNoteOff(parameter, channel, number);
        }
        return this;
    }

    public LXMidiDevice bindNoteOn(LXParameter parameter, int number) {
        return bindNoteOn(parameter, number, NOTE_VELOCITY);
    }

    public LXMidiDevice bindNoteOn(LXParameter parameter, int channel, int number) {
        return bindNoteOn(parameter, channel, number, DIRECT);
    }

    public LXMidiDevice bindNoteOn(LXParameter parameter, int channel,
            int number, int mode) {
        return bindNoteOn(parameter, channel, number, mode, NOTE_VELOCITY);
    }

    public LXMidiDevice bindNoteOn(LXParameter parameter, int channel,
            int number, int mode, int value) {
        if (channel == ANY_CHANNEL) {
            for (int i = 0; i < MIDI_CHANNELS; ++i) {
                bindNoteOn(parameter, i, number, mode, value);
            }
        } else {
            unbindNoteOn(channel, number);
            int i = index(channel, number);
            this.noteOnBindings[i] = new NoteBinding(parameter, channel, number,
                    mode, value);
        }
        return this;
    }

    public LXMidiDevice bindNoteOff(LXParameter parameter, int number) {
        return bindNoteOff(parameter, ANY_CHANNEL, number);
    }

    public LXMidiDevice bindNoteOff(LXParameter parameter, int channel, int number) {
        if (channel == ANY_CHANNEL) {
            for (int i = 0; i < MIDI_CHANNELS; ++i) {
                bindNoteOff(parameter, i, number);
            }
        } else {
            unbindNoteOff(channel, number);
            int i = index(channel, number);
            this.noteOffBindings[i] = new NoteBinding(parameter, channel, number,
                    OFF, 0);
        }
        return this;
    }

    public LXMidiDevice bindController(LXParameter parameter, int cc) {
        return bindController(parameter, ANY_CHANNEL, cc);
    }

    public LXMidiDevice bindController(LXParameter parameter, int channel, int cc) {
        return bindController(parameter, channel, cc, CC_VALUE);
    }

    public LXMidiDevice bindController(LXParameter parameter, int channel,
            int cc, int value) {
        if (channel == ANY_CHANNEL) {
            for (int i = 0; i < MIDI_CHANNELS; ++i) {
                bindController(parameter, i, cc, value);
            }
        } else {
            unbindController(channel, cc);
            int i = index(channel, cc);
            this.controllerBindings[i] = new ControllerBinding(parameter, channel,
                    cc, value);
        }
        return this;
    }

    public LXMidiDevice unbindNote(int number) {
        return unbindNote(ANY_CHANNEL, number);
    }

    public LXMidiDevice unbindNote(int channel, int number) {
        unbindNoteOn(channel, number);
        unbindNoteOff(channel, number);
        return this;
    }

    public LXMidiDevice unbindNoteOn(int number) {
        return unbindNoteOn(ANY_CHANNEL, number);
    }

    public LXMidiDevice unbindNoteOn(int channel, int number) {
        if (channel == ANY_CHANNEL) {
            for (int i = 0; i < MIDI_CHANNELS; ++i) {
                unbindNoteOn(channel, number);
            }
        } else {
            int i = index(channel, number);
            if (noteOnBindings[i] != null) {
                noteOnBindings[i].unbind();
                noteOnBindings[i] = null;
            }
        }
        return this;
    }

    public LXMidiDevice unbindNoteOff(int number) {
        return unbindNoteOff(ANY_CHANNEL, number);
    }

    public LXMidiDevice unbindNoteOff(int channel, int number) {
        if (channel == ANY_CHANNEL) {
            for (int i = 0; i < MIDI_CHANNELS; ++i) {
                unbindNoteOff(channel, number);
            }
        } else {
            int i = index(channel, number);
            if (noteOffBindings[i] != null) {
                noteOffBindings[i].unbind();
                noteOffBindings[i] = null;
            }
        }
        return this;
    }

    public LXMidiDevice unbindController(int cc) {
        return unbindController(ANY_CHANNEL, cc);
    }

    public LXMidiDevice unbindController(int channel, int cc) {
        if (channel == ANY_CHANNEL) {
            for (int i = 0; i < MIDI_CHANNELS; ++i) {
                unbindNoteOff(channel, cc);
            }
        } else {
            int i = index(channel, cc);
            if (controllerBindings[i] != null) {
                controllerBindings[i].unbind();
                controllerBindings[i] = null;
            }
        }
        return this;
    }

    private int index(int channel, int number) {
        return channel * MIDI_RANGE + number;
    }

    public LXMidiDevice sendNoteOn(int channel, int number, int velocity) {
        if (this.output != null) {
            this.output.sendNoteOn(channel, number, velocity);
        }
        return this;
    }

    public LXMidiDevice sendNoteOff(int channel, int number) {
        return sendNoteOff(channel, number, 0);
    }

    public LXMidiDevice sendNoteOff(int channel, int number, int velocity) {
        if (this.output != null) {
            this.output.sendNoteOff(channel, number, velocity);
        }
        return this;
    }

    public LXMidiDevice sendController(int channel, int cc, int value) {
        if (this.output != null) {
            this.output.sendController(channel, cc, value);
        }
        return this;
    }

    public LXMidiDevice sendSysex(byte[] sysex) {
        if (this.output != null) {
            this.output.sendSysex(sysex);
        }
        return this;
    }

    public final void noteOnReceived(Note note) {
        int index = index(note.getChannel(), note.getPitch());
        if (this.noteOnBindings[index] != null) {
            this.noteOnBindings[index].noteOnReceived(note);
        }
        noteOn(note);
    }

    public final void noteOffReceived(Note note) {
        int index = index(note.getChannel(), note.getPitch());
        if (this.noteOffBindings[index] != null) {
            this.noteOffBindings[index].noteOffReceived(note);
        }
        noteOff(note);
    }

    public final void controllerChangeReceived(Controller controller) {
        int index = index(controller.getChannel(), controller.getCC());
        if (this.controllerBindings[index] != null) {
            this.controllerBindings[index].controllerChangeReceived(controller);
        }
        controllerChange(controller);
    }

    protected void noteOn(Note note) {
    }

    protected void noteOff(Note note) {
    }

    protected void controllerChange(Controller controller) {
    }

}
