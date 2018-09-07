package com.symmetrylabs.slstudio.performance;

import heronarts.lx.*;
import heronarts.lx.midi.*;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import java.util.ArrayList;

public class MidiFighterListener extends LXComponent implements LXMidiListener {
    final int RGB_OFF = 17;
    final int RGB_MED = 32;
    final int RGB_MAX = 47;

    LXMidiRemote midi;

    ArrayList<LXListenableNormalizedParameter> params;
    LXParameterListener[] writeListeners;
    final LX lx;
    int lastActivePatternIndex = -1;


    void getAndWriteParams() {
        removeWriteListeners();
        getParams();
        addWriteListeners();
        writeParamStates();
    }


    void removeWriteListeners() {
        for (int i = 0; i < Math.min(params.size(), writeListeners.length); i++) {
            if (params.get(i) != null) {
                params.get(i).removeListener(writeListeners[i]);
            }
        }

    }

    void addWriteListeners() {
        for (int i = 0; i < Math.min(params.size(), writeListeners.length); i++) {
            if (params.get(i) != null) {
                params.get(i).addListener(writeListeners[i]);
            }
        }
    }

    LXChannel getActiveChannel() {
        LXBus b = lx.engine.getFocusedChannel();
        if (!(b instanceof LXChannel)) {
            return null;
        }
        LXChannel c = (LXChannel)b;
        return c;
    }

    void getParams() {
        params.clear();
        LXChannel c = getActiveChannel();
        if (c == null) {
            return;
        }
        ArrayList<LXListenableNormalizedParameter> knobParams = new ArrayList<>();
        ArrayList<BooleanParameter> buttonParams = new ArrayList<>();

        for (LXParameter p : c.getActivePattern().getParameters()) {
            if (!(p instanceof LXListenableNormalizedParameter)) {
                continue;
            }

            if (p instanceof BooleanParameter) {
                buttonParams.add((BooleanParameter)p);
            } else {
                knobParams.add((LXListenableNormalizedParameter)p);
            }
        }
        params.clear();
        int buttonStart = knobParams.size();
        for (int i = 0; i < 16; i++) {
            int bI = i - buttonStart;
            if (i < knobParams.size()) {
                params.add(knobParams.get(i));
            } else if (bI >= 0 && bI < buttonParams.size()) {
                params.add(buttonParams.get(bI));
            } else {
                params.add(null);
            }
        }
    }

    void writeParamState(int knobI) {
        LXMidiOutput out = midi.getOutput();

        int VALUE_CHANNEL = 0;
        int COLOR_CHANNEL = 1;
        int BRIGHTNESS_CHANNEL = 2;

//        int c = (p instanceof BooleanParameter) ? 70 : 114;
//        out.sendControlChange(1, knobI, c);


        if (knobI < params.size() && params.get(knobI) != null) {
            LXListenableNormalizedParameter p = params.get(knobI);

            if (p instanceof BooleanParameter) {
                BooleanParameter b = (BooleanParameter)p;
                out.sendControlChange(VALUE_CHANNEL, knobI, 0);
                out.sendControlChange(COLOR_CHANNEL, knobI, b.isOn() ? 90 : 70);
                out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_MAX);

            } else {
                float raw = p.getNormalizedf();
                int v = (int)(raw * 127);

                out.sendControlChange(VALUE_CHANNEL, knobI, v);
                out.sendControlChange(COLOR_CHANNEL, knobI, 114);
                out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_MAX);
            }

        } else {
            out.sendControlChange(VALUE_CHANNEL, knobI, 0);
            out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_OFF);
        }
    }

    void writeParamStates() {
        for (int i = 0; i < 16; i++) {
            writeParamState(i);
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn midiNoteOn) {
        System.out.println(midiNoteOn.toString());
    }

    @Override
    public void noteOffReceived(MidiNote midiNote) {
        System.out.println(midiNote.toString());
    }

    @Override
    public void controlChangeReceived(MidiControlChange midiControlChange) {

        int channel = midiControlChange.getChannel();
        int cc = midiControlChange.getCC();
        double v = midiControlChange.getNormalized();

        if (channel == 0 || channel == 1) {
            if (cc >= params.size() || params.get(cc) == null) {
                return;
            }
            LXListenableNormalizedParameter param = params.get(cc);

            if (channel == 1 && (param instanceof BooleanParameter)) {
                BooleanParameter b = (BooleanParameter)param;
                boolean on = v == 1.0;
                if (b.getMode() == BooleanParameter.Mode.MOMENTARY) {
                    b.setValue(on);
                } else if (on) {
                    b.toggle();
                }
            } else if (!(param instanceof BooleanParameter)) {
                param.setNormalized(v);
            }

        }

        if (channel == 3) {
            if (v == 1.0f && cc >= 8 && cc <= 13) {
                boolean left = cc <= 10;
                if (left) {
                    lx.engine.focusedChannel.decrement();
                } else {
                    lx.engine.focusedChannel.increment();
                }
            }
        }
    }

    @Override
    public void programChangeReceived(MidiProgramChange midiProgramChange) {
        System.out.println(midiProgramChange.toString());
    }

    @Override
    public void pitchBendReceived(MidiPitchBend midiPitchBend) {
        System.out.println(midiPitchBend.toString());

    }

    @Override
    public void aftertouchReceived(MidiAftertouch midiAftertouch) {
        System.out.println(midiAftertouch.toString());
    }

    MidiFighterListener(LX lx, LXMidiRemote midi) {
        super(lx);
        this.lx = lx;

        this.midi = midi;

        params = new ArrayList<>();

        writeListeners = new LXParameterListener[16];
        for (int i = 0; i < writeListeners.length; i++) {
            final int k = i;
            writeListeners[i] = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    writeParamState(k);
                }
            };
        }

        lx.engine.focusedChannel.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                getAndWriteParams();
            }
        });

        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double v) {
                LXChannel c = getActiveChannel();
                if (c == null) {
                    return;
                }
                int i = c.getActivePatternIndex();
                if (i != lastActivePatternIndex) {
                    getAndWriteParams();
                    lastActivePatternIndex = i;
                }
            }
        });

        getAndWriteParams();

    }

    public static void bindMidi(LX lx) {

        ArrayList<LXMidiInput> twisterInputs = new ArrayList<LXMidiInput>();
        ArrayList<LXMidiOutput> twisterOutputs = new ArrayList<LXMidiOutput>();


        for (LXMidiInput in : lx.engine.midi.inputs) {
            String name = in.getName();
            String desc = in.getDescription();
            if (name.contains("Twister")) {
                twisterInputs.add(in);
            }
        }

        for (LXMidiOutput out : lx.engine.midi.outputs) {
            String name = out.getName();
            if (name.contains("Twister")) {
                twisterOutputs.add(out);
            }
        }

        for (int i = 0; i < Math.min(2, twisterInputs.size()); i++) {
            LXMidiInput in = twisterInputs.get(i);
            LXMidiOutput out = twisterOutputs.get(i);
            in.open();
            out.open();
            LXMidiRemote twister = new LXMidiRemote(in, out);
            MidiFighterListener listener = new MidiFighterListener(lx, twister);
            in.addListener(listener);
        }
    }

}



