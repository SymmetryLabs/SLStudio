package com.symmetrylabs.slstudio.performance;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.*;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.*;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.*;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UIParameterControl;
import heronarts.p3lx.ui.component.UISwitch;
import heronarts.p3lx.ui.control.UIChannelControl;
import heronarts.p3lx.ui.studio.device.UIPatternDevice;
import heronarts.p3lx.ui.studio.mixer.UIChannelStripControls;

import java.util.ArrayList;
import java.util.List;

public class MidiFighterListener extends LXComponent implements LXMidiListener {
    final int RGB_OFF = 17;
    final int RGB_MED = 32;
    final int RGB_MAX = 47;
    final int COLOR_RED = 80;
    final int COLOR_PINK = 90;
    final int COLOR_ORANGE = 70;
    final int COLOR_PURPLE = 114;


    public enum Type {
        ACTIVE,
        CUE
    };

    LXMidiRemote midi;
    UIWindow cueWindow = null;
    int originalColor;

    final Type type;



    ArrayList<LXListenableNormalizedParameter> params;
    LXParameterListener[] writeListeners;
    final LX lx;
    final UI ui;
    int lastActivePatternIndex = -1;
    int lastActiveChannelIndex = -1;


    void getAndWriteParams() {
        removeWriteListeners();
        getParams();
        addWriteListeners();
        writeParamStates();
        updateCueWindow();
    }

    void updateCueWindow() {
        if (cueWindow == null) {
            return;
        }

        for (UIObject o : cueWindow.getChildren()) {
            if (o instanceof UIControlTarget) {
                ((UI2dComponent)o).removeFromContainer();
            }
        }

        for (int i = 0; i < params.size(); i++) {
            LXListenableNormalizedParameter param = getParam(i);
            if (param == null) {
                continue;
            }
            if (param instanceof BooleanParameter) {
                UISwitch s = new UISwitch(0, 0);
                s.setParameter((BooleanParameter)param);
                s.addToContainer(cueWindow);
            } else {
                UIKnob k = new UIKnob();
                k.setParameter(param);
                k.addToContainer(cueWindow);
            }
        }
        
        setBackgroundColor();
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
        if (type == Type.CUE) {
            int i = getCueIndex();
            if (i == -1) {
                return null;
            }
            return lx.engine.getChannel(i);
        } else {
            LXBus b = lx.engine.getFocusedChannel();
            if (!(b instanceof LXChannel)) {
                return null;
            }
            LXChannel c = (LXChannel)b;
            return c;
        }

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
        for (int i = 0; i < numSlots(); i++) {
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

        if (showCueSelectionMode()) {
            int nChannels = lx.engine.getChannels().size();
            out.sendControlChange(VALUE_CHANNEL, knobI, 0);
            out.sendControlChange(COLOR_CHANNEL, knobI, COLOR_RED);
            out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, knobI < nChannels ? RGB_MAX : RGB_OFF);
            return;
        }


        if (type == Type.CUE && knobI == 15) {
            out.sendControlChange(VALUE_CHANNEL, knobI, 0);
            out.sendControlChange(COLOR_CHANNEL, knobI, COLOR_RED);
            out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_MAX);
            return;
        }

        if (knobI < params.size() && params.get(knobI) != null) {
            LXListenableNormalizedParameter p = params.get(knobI);

            if (p instanceof BooleanParameter) {
                BooleanParameter b = (BooleanParameter)p;
                out.sendControlChange(VALUE_CHANNEL, knobI, 0);
                out.sendControlChange(COLOR_CHANNEL, knobI, b.isOn() ? COLOR_PINK : COLOR_ORANGE);
                out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_MAX);

            } else {
                float raw = p.getNormalizedf();
                int v = (int)(raw * 127);

                out.sendControlChange(VALUE_CHANNEL, knobI, v);
                out.sendControlChange(COLOR_CHANNEL, knobI, COLOR_PURPLE);
                out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_MAX);
            }

        } else {
            out.sendControlChange(VALUE_CHANNEL, knobI, 0);
            out.sendControlChange(BRIGHTNESS_CHANNEL, knobI, RGB_OFF);
        }
    }


    boolean showCueSelectionMode() {
        return type == Type.CUE && getCueIndex() == -1;
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

    LXListenableNormalizedParameter getParam(int cc) {
        if (cc >= params.size()) {
            return null;
        }
        return params.get(cc);
    }

    void onButtonPress(int cc, boolean on) {
        if (showCueSelectionMode()) {
            List<LXChannel> channels = lx.engine.getChannels();
            if (cc < channels.size() && on) {
                channels.get(cc).cueActive.setValue(true);
            }
            return;
        }

        if (type == Type.CUE && cc == 15) {
            getActiveChannel().cueActive.setValue(false);
            return;
        }

        LXListenableNormalizedParameter param = getParam(cc);
        if (param == null || !(param instanceof BooleanParameter)) {
            return;
        }

        BooleanParameter b = (BooleanParameter)param;
        if (b.getMode() == BooleanParameter.Mode.MOMENTARY) {
            b.setValue(on);
        } else if (on) {
            b.toggle();
        }

    }

    void onKnobTwist(int cc, double v) {
        LXListenableNormalizedParameter param = getParam(cc);
        if (param == null || (param instanceof BooleanParameter)) {
            return;
        }

        param.setNormalized(v);
    }

    @Override
    public void controlChangeReceived(MidiControlChange midiControlChange) {

        int channel = midiControlChange.getChannel();
        int cc = midiControlChange.getCC();
        double v = midiControlChange.getNormalized();

        if (channel == 0) {
            onKnobTwist(cc, v);
        }


        if (channel == 1) {
            onButtonPress(cc, v == 1.0);
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

    int getCueIndex() {
        List<LXChannel> channels = lx.engine.getChannels();
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).cueActive.isOn()) {
                return i;
            }
        }
        return -1;
    }

    void pollChanges() {
        LXChannel c = getActiveChannel();
        int patternI = -1;
        int activeI = -1;
        if (c != null) {
            patternI = c.getActivePatternIndex();
            activeI =  c.getIndex();
        }

        if (activeI != lastActiveChannelIndex || patternI != lastActivePatternIndex) {
            getAndWriteParams();

        }
        lastActivePatternIndex = patternI;
        lastActiveChannelIndex = activeI;
    }

    int numSlots() {
        return type == Type.ACTIVE ? 16 : 15;
    }

    MidiFighterListener(LX lx, UI ui, LXMidiRemote midi, Type type) {
        super(lx);
        this.lx = lx;
        this.ui = ui;
        this.midi = midi;
        this.type = type;

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


        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double v) {
                pollChanges();
            }
        });

        getAndWriteParams();

        if (type == Type.CUE) {
            float xOff = ((SLStudioLX.UI)ui).leftPane.getWidth() + 7;
            float yOff = 25;
            cueWindow = new UIWindow(ui, "Cue Controller", xOff, yOff, 175, 200);
            cueWindow.setLayout(UI2dContainer.Layout.HORIZONTAL_GRID);
            cueWindow.setChildMargin(5, 5);
            ui.addLayer(cueWindow);
            originalColor = cueWindow.getBackgroundColor();
            setBackgroundColor();
        }
    }

    void setBackgroundColor() {
        if (cueWindow == null) {
            return;
        }
        int color = getCueIndex() == -1 ? originalColor : LXColor.hsb(0, 100, 30);
        cueWindow.setBackgroundColor(color);
    }

    public static void bindMidi(LX lx, UI ui) {

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
            Type t = i == 0 ? Type.ACTIVE : Type.CUE;
            MidiFighterListener listener = new MidiFighterListener(lx, ui, twister, t);
            in.addListener(listener);
        }
    }

}



