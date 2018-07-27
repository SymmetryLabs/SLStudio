package com.symmetrylabs.slstudio.performance;

import com.google.gson.*;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.*;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.midi.surface.LXMidiSurface;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.component.UIItemList;

import java.util.*;



public class PerformanceHardwareController extends LXComponent {
    final LX lx;
    public final PerformanceManager pm;
    PaletteListener palette = null;
    HashMap<String, ArrayList<LXParameter>> uuidToParameter;
    HashMap<String, ArrayList<DeckDependentMapping>> uuidToDeckDependentMapping;
    HashMap<String, ArrayList<Integer>> uuidToDeckIndex;
    HashMap<String, Boolean> uuidToUpsideDown;
    HashMap<String, ColorParameter> uuidToColorParameter;

    final BooleanParameter[] cuesPressed;

    final static String MAPPING_FILENAME = "palette_mapping.json";
    final static String MAPPINGS_KEY = "mappings";
    final static String UPSIDE_DOWN_KEY = "upside-down";

    final static String CENTER_CROSSFADER = "center-crossfader";
    final static String BRIGHTNESS = "brightness";
    final static String SPEED = "speed";
    final static String BLUR = "blur";
    final static String DESATURATION = "desaturation";
    final static String DECK_CROSSFADER = "deck-crossfader";
    final static String PATTERN_SCROLL = "pattern-scroll";
    final static String PRESET_SCROLL = "preset-scroll";
    final static String PRESET_SAVE = "preset-save";
    final static String PRESET_FIRE = "preset-fire";
    final static String BLENDMODE_SCROLL = "blendmode-scroll";
    final static String CUE_BUTTONS = "cue-buttons";


    final ArrayList<TwisterListener> twisterListeners;
    final ArrayList<FighterListener> fighterListeners;

    abstract class MFListener {
        int deckI;
        LXMidiRemote midi;

        ArrayList<LXListenableNormalizedParameter> params = null;
        LXParameterListener[] writeListeners;
        LXParameterListener activeChannelListener;

        abstract void writeParamState(int i);
        abstract void getParams();
        abstract void flashSide(int i);
        abstract void writeParamStates();


        void getAndWriteParams() {
            removeWriteListeners();
            getParams();
            addWriteListeners();
            writeParamStates();
        }

        void removeListeners(int i) {
            pm.decks[i].activeChannel.removeListener(activeChannelListener);
            for (int j = 0; j < 2; j++) {
                int wI = (i * 2) + j;
                BooleanParameter active = pm.gui.channelWindows[wI].patternChanged;
                active.removeListener(activeChannelListener);
            }

        }

        void addListeners(int i) {
            pm.decks[i].activeChannel.addListener(activeChannelListener);

            for (int j = 0; j < 2; j++) {
                int wI = (i * 2) + j;
                BooleanParameter active = pm.gui.channelWindows[wI].patternChanged;
                active.addListener(activeChannelListener);
            }
        }

        void removeWriteListeners() {
            if (params != null) {
                for (int i = 0; i < Math.min(params.size(), writeListeners.length); i++) {
                    if (params.get(i) != null) {
                        params.get(i).removeListener(writeListeners[i]);
                    }
                }
            } else {
                params = new ArrayList<>();
            }
        }

        void addWriteListeners() {
            for (int i = 0; i < Math.min(params.size(), writeListeners.length); i++) {
                if (params.get(i) != null) {
                    params.get(i).addListener(writeListeners[i]);
                }
            }
        }



        MFListener(LXMidiRemote midi, int deckI) {
            this.midi = midi;
            this.deckI = deckI;



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

            activeChannelListener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    getAndWriteParams();
                }
            };

            getAndWriteParams();
            addListeners(deckI);
        }

        void setDirection(boolean left, boolean propagate) {
            int desired = left ? 0 : 1;

            if (deckI != desired) {
                removeListeners(deckI);
                deckI = desired;
                addListeners(deckI);
                getAndWriteParams();
            }

            flashSide(deckI);


            if (propagate) {
                boolean isTwister = this instanceof TwisterListener;
                for (MFListener l : isTwister ? twisterListeners : fighterListeners) {
                    if (l == this) continue;
                    l.setDirection(!left, false);
                }
            }


        }
    }

    class FighterListener extends MFListener implements LXMidiListener {



        FighterListener(LXMidiRemote midi, int i) {
            super(midi, i);

            flashSide(deckI);


        }

        void onNote(int channel, int pitch, boolean on) {
            if (!on) {
                return;
            }
            if (channel == 2) {
                int i = fromKey(pitch);
                if (i < params.size()) {
                    BooleanParameter p = (BooleanParameter)params.get(i);
                    p.toggle();
                }
            }
            if (channel == 3) {
                if (pitch >= 20) {
                    int section = (pitch - 20) / 3;
                    setDirection(section % 2 == 0, true);
                }
                if (pitch < 4) {
                    int d = deckI * 2;
                    int clamped = Math.max(d, Math.min(d + 1, pitch));
                    int cI = clamped - d;
                    pm.decks[deckI].activeChannel.setValue(cI);
                    writeDeck();
                }
            }
        }

        @Override
        public void noteOnReceived(MidiNoteOn midiNoteOn) {
            int channel = midiNoteOn.getChannel();
            int pitch = midiNoteOn.getPitch();
            onNote(channel, pitch, true);
        }

        @Override
        public void noteOffReceived(MidiNote midiNote) {
            int channel = midiNote.getChannel();
            int pitch = midiNote.getPitch();
            onNote(channel, pitch, false);
        }

        @Override
        public void controlChangeReceived(MidiControlChange midiControlChange) {
        }

        @Override
        public void programChangeReceived(MidiProgramChange midiProgramChange) {
            System.out.println(midiProgramChange.toString());
        }

        @Override
        public void pitchBendReceived(MidiPitchBend midiPitchBend) {

        }

        @Override
        public void aftertouchReceived(MidiAftertouch midiAftertouch) {

        }

        @Override
        void getParams() {
            int maxParamButtons = 8;
            int active = pm.decks[deckI].activeChannel.getValuei();
            PerformanceManager.PerformanceChannel activeChannel = pm.decks[deckI].channels[active];
            ArrayList<BooleanParameter> buttonParams = activeChannel.getButtonParameters();
            params.clear();
            for (int i = 0; i < maxParamButtons; i++) {
                if (i < buttonParams.size()) {
                    params.add(buttonParams.get(i));
                } else {
                    break;
                }
            }
        }

        int fromKey(int pitch) {
            int k = (pitch - 36) % 16;
            int x = k % 4;
            int y = k / 4;
            return ((3 - y) * 4) + x;
        }

        int toKey(int i) {
            int x = i % 4;
            int y = i / 4;
            int k = ((3 - y) * 4) + x;
            return 36 + k;
        }

        void writeDeck() {
            int c = pm.decks[deckI].activeChannel.getValuei();
            int d = deckI * 2;
            midi.getOutput().sendNoteOn(3, d + c, 127);
        }

        void writeParamStates() {
            for (int i = 0; i < 16; i++) {
                writeParamState(i);
            }
            writeDeck();
        }

        @Override
        void writeParamState(int buttonI) {
            LXMidiOutput out = midi.getOutput();

            int pitch = toKey(buttonI);

            int ON = 65; //63;
            int OFF = 27; //15;

            int v;
            int b;
            if (buttonI < params.size() && params.get(buttonI) != null) {
                v = ((BooleanParameter)params.get(buttonI)).isOn() ? ON : OFF;
                b = 33;
//                out.sendControlChange(2, knobI, RGB_MAX);
            } else {
                v = OFF;
                b = 18;
//                out.sendControlChange(2, knobI, RGB_OFF);

            }

            for (int i = 0; i < 4; i++) {
                int kI = pitch + (16 * i);
                midi.sendNoteOn(2, kI, v);
                midi.sendNoteOn(3, kI, b);
            }
        }

        @Override
        void flashSide(int side) {

            for (int i = 0; i < 16; i++) {
                int pitch = toKey(i);

                int col = i % 4;
                boolean on;
                if (side == 0) {
                    on = col < 2;
                } else {
                    on = col >= 2;
                }
                for (int j = 0; j < 4; j++) {
                    int kI = pitch + (16 * j);

                    midi.sendNoteOn(2, kI, 110);
                    midi.sendNoteOn(3, kI, on ? 33 : 18);
                }
            }

            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        writeParamStates();
                    }
                },
                500
            );

//            for (int i = 0; i < 16; i++) {
//                int col = i % 4;
//                boolean on;
//                if (side == 0) {
//                    on = col < 2;
//                } else {
//                    on = col >= 2;
//                }
//                LXMidiOutput out = midi.getOutput();
//                out.sendControlChange(1, i, 90);
////                out.sendControlChange(2, i, on ? RGB_MAX : RGB_OFF);
////                out.sendControlChange(0, i, on ? 127 : 0);
//            }
//            new java.util.Timer().schedule(
//                new java.util.TimerTask() {
//                    @Override
//                    public void run() {
////                        setButtonColors();
//                        writeParamStates();
//                    }
//                },
//                500
//            );
        }
    }

    class TwisterListener extends MFListener implements LXMidiListener {

        final int RGB_OFF = 17;
        final int RGB_MED = 32;
        final int RGB_MAX = 47;



        TwisterListener(LXMidiRemote midi, int deckI) {
            super(midi, deckI);

            setButtonColors();
            flashSide(deckI);
        }

        void setButtonColors() {
            LXMidiOutput out = midi.getOutput();
            for (int i = 0; i < 16; i++) {
                int c = i < 12 ? 114 : 70;
                out.sendControlChange(1, i, c);
            }
        }

        @Override
        void getParams() {
            int maxParamKnobs = 12;
            int active = pm.decks[deckI].activeChannel.getValuei();
            PerformanceManager.PerformanceChannel activeChannel = pm.decks[deckI].channels[active];
            ArrayList<LXListenableNormalizedParameter> knobParams = activeChannel.getKnobParameters();
            params.clear();
            for (int i = 0; i < maxParamKnobs; i++) {
                if (i < knobParams.size()) {
                    params.add(knobParams.get(i));
                } else {
                    params.add(null);
                }
            }
            params.addAll(activeChannel.getEffectParameters());
        }

        @Override
        void writeParamState(int knobI) {
            LXMidiOutput out = midi.getOutput();

            int v;
            if (knobI < params.size() && params.get(knobI) != null) {
                float raw = params.get(knobI).getNormalizedf();
                v = (int)(raw * 127);
                out.sendControlChange(2, knobI, RGB_MAX);
            } else {
                v = 0;
                out.sendControlChange(2, knobI, RGB_OFF);

            }
            out.sendControlChange(0, knobI, v);
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
        void flashSide(int side) {
            for (int i = 0; i < 16; i++) {
                int col = i % 4;
                boolean on;
                if (side == 0) {
                    on = col < 2;
                } else {
                    on = col >= 2;
                }
                LXMidiOutput out = midi.getOutput();
                out.sendControlChange(1, i, 90);
                out.sendControlChange(2, i, on ? RGB_MAX : RGB_OFF);
            }
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        setButtonColors();
                        writeParamStates();
                    }
                },
                500
            );
        }

        @Override
        public void controlChangeReceived(MidiControlChange midiControlChange) {

            int channel = midiControlChange.getChannel();
            int cc = midiControlChange.getCC();
            double v = midiControlChange.getNormalized();

            if (channel == 0) {
                if (cc >= params.size() || params.get(cc) == null) {
                    return;
                }
                LXListenableNormalizedParameter param = params.get(cc);
                param.setNormalized(v);
            }
            if (channel == 3) {
                if (v == 1.0f && cc >= 8 && cc <= 13) {
                    setDirection(cc <= 10, true);
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
    }



    void bindMidi() {
        ArrayList<LXMidiInput> fighterInputs = new ArrayList<LXMidiInput>();
        ArrayList<LXMidiInput> twisterInputs = new ArrayList<LXMidiInput>();
        ArrayList<LXMidiOutput> fighterOutputs = new ArrayList<LXMidiOutput>();
        ArrayList<LXMidiOutput> twisterOutputs = new ArrayList<LXMidiOutput>();


        for (LXMidiInput in : lx.engine.midi.inputs) {
            String name = in.getName();
            String desc = in.getDescription();
            if (name.contains("Twister")) {
                twisterInputs.add(in);
                // horrible terrible hack
            } else if (name.contains("Fighter") || name.contains("Internal Error")) {
                fighterInputs.add(in);
            }

            System.out.printf("DESC: %s %s\n", in.getDescription(), in.getName());

        }

        for (LXMidiOutput out : lx.engine.midi.outputs) {
            String name = out.getName();
            if (name.contains("Twister")) {
                twisterOutputs.add(out);
            } else if (name.contains("Fighter") || name.contains("Internal Error")) {
                fighterOutputs.add(out);
            }
        }

        for (int i = 0; i < Math.min(2, twisterInputs.size()); i++) {
            LXMidiInput in = twisterInputs.get(i);
            LXMidiOutput out = twisterOutputs.get(i);
            in.open();
            out.open();
            LXMidiRemote twister = new LXMidiRemote(in, out);
            TwisterListener listener = new TwisterListener(twister, i);
            in.addListener(listener);
            twisterListeners.add(listener);
        }

        for (int i = 0; i < Math.min(2, fighterInputs.size()); i++) {
            LXMidiInput in = fighterInputs.get(i);
            LXMidiOutput out = fighterOutputs.get(i);
            in.open();
            out.open();
            LXMidiRemote fighter = new LXMidiRemote(in, out);
            FighterListener listener = new FighterListener(fighter, i);
            in.addListener(listener);
            fighterListeners.add(listener);
        }
    }



    static abstract class DeckDependentMapping {
        abstract     LXParameter getParameter(int deckI, int channelI);
        abstract ColorParameter getColorParameter(int deckI, int channelI);


    }



    public PerformanceHardwareController(LX lx, PerformanceManager pm) {
        super(lx);

        this.lx = lx;
        this.pm = pm;

        LXParameterListener cueListener = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                boolean press = lxParameter.getValue() == 1.0;
                if (!press) {
                    return;
                }

                boolean allPressed = true;
                int anyPressed = -1;
                int nPressed = 0;
                boolean[] p = new boolean[4];
                for (int i = 0; i < cuesPressed.length; i++) {
                    p[i] = cuesPressed[i].isOn();
                    if (!p[i]) {
                        allPressed = false;
                    } else {
                        nPressed++;
                    }

                    if (cuesPressed[i] == lxParameter) {
                        anyPressed = i;
                    }
                }

                int chanVal = anyPressed * 2;


                if (nPressed == 1 && pm.cueState.getValuei()  == chanVal) {
                    pm.cueState.setValue(3);
                    return;
                }


                if (allPressed) {
                    pm.cueState.setValue(3);
                } else if (p[0] && p[1]) {
                    pm.cueState.setValue(1);
                } else if (p[2] && p[3]) {
                    pm.cueState.setValue(5);
                } else if (anyPressed != -1) {
                    pm.cueState.setValue(chanVal);
                } else {
                }
            }
        };

        cuesPressed = new BooleanParameter[4];
        for (int i = 0; i < cuesPressed.length; i++) {
            String name = String.format("cuePressed-%d", i);
            cuesPressed[i] = new BooleanParameter(name, false);
            cuesPressed[i].addListener(cueListener);
        }


        uuidToParameter = new HashMap<String, ArrayList<LXParameter>>();
        uuidToColorParameter = new HashMap<String, ColorParameter>();
        uuidToDeckDependentMapping = new HashMap<String, ArrayList<DeckDependentMapping>>();
        uuidToDeckIndex = new HashMap<String, ArrayList<Integer>>();
        uuidToUpsideDown = new HashMap<String, Boolean>();

        setupColorParameters();
        loadMapping();

        palette = new PaletteListener(lx);
        palette.moduleSet.addListener(new SetListener<PaletteListener.Module>() {
            @Override
            public void onItemAdded(PaletteListener.Module element) {
                mapModule(element);
            }

            @Override
            public void onItemRemoved(PaletteListener.Module element) {
            }
        });

        for (PerformanceManager.PerformanceDeck d : pm.decks) {
            d.activeChannel.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    for (PaletteListener.Module m : palette.moduleSet) {
                        if (uuidToDeckDependentMapping.containsKey(m.uuid)) {
                            mapDeckDepdendentModule(m);
                        }
                    }
                }
            });
        }

        twisterListeners = new ArrayList<>();
        fighterListeners = new ArrayList<>();



        lx.engine.midi.whenReady(new Runnable() {
            public void run() {
                bindMidi();
            }
        });

        for (int i = 0; i < 4; i++) {
            final int j = i;
            pm.gui.channelWindows[i].activePatternIndex.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    int k = ((DiscreteParameter)lxParameter).getValuei();
                    String name = pm.gui.channelWindows[j].patternList.getItems().get(k).getLabel();
                    int deck = j < 2 ? 0 : 1;
                    setPatternScreen(deck, name);
                }
            });
        }


//        startPaletteThread();


    }

    void setPatternScreen(int deckIndex, String name) {
        if (palette.modules == null) {
            return;
        }
        for (PaletteListener.Module hub : palette.modules.column(1).values()) {
            if (hub.hubIndex == deckIndex) {
                hub.setString(name);
            }
        }
    }


    void mapDeckDepdendentModule(PaletteListener.Module module) {
        ArrayList<DeckDependentMapping> mappings = uuidToDeckDependentMapping.get(module.uuid);
        for (int i = 0; i < mappings.size(); i++) {
            DeckDependentMapping mapping = mappings.get(i);
            int deckI =  uuidToDeckIndex.get(module.uuid).get(i);
            int channelI = pm.decks[deckI].activeChannel.getValuei();
            LXParameter param = mapping.getParameter(deckI, channelI);
            ColorParameter colorParam = mapping.getColorParameter(deckI, channelI);
            module.mapParameter(param);
            module.mapColor(colorParam);
        }

    }

    void mapDeckIndependentModule(PaletteListener.Module module) {
        for (LXParameter param : uuidToParameter.get(module.uuid)) {
            module.mapParameter(param);
        }
        module.mapColor(uuidToColorParameter.get(module.uuid));
    }

    void mapModule(PaletteListener.Module module) {
        if (uuidToParameter.containsKey(module.uuid)) {
            mapDeckIndependentModule(module);
        } else if (uuidToDeckDependentMapping.containsKey(module.uuid)) {
            mapDeckDepdendentModule(module);
        }
        boolean upsideDown = uuidToUpsideDown.containsKey(module.uuid);
        module.setUpsideDown(upsideDown);

    }

    String[] getUUIDs(JsonObject obj, String key) {
        JsonElement v = obj.get(key);
        String[] uuids = null;
        if (v.isJsonPrimitive()) {
            uuids = new String[] {v.getAsString()};
        } else if (v.isJsonObject()) {
            uuids = new String[2];
            JsonObject lr = v.getAsJsonObject();
            uuids[0] = lr.get("left").getAsString();
            uuids[1] =  lr.get("right").getAsString();
        } else if (v.isJsonArray()) {
            JsonArray els = v.getAsJsonArray();
            uuids = new String[els.size()];
            for (int i = 0; i < els.size(); i++) {
                uuids[i] = els.get(i).getAsString();
            }
        }
        return uuids;
    }

    void setParameterMappings(JsonObject obj, String key, LXParameter[] params, ColorParameter[] colorParams) {
        String[] uuids = getUUIDs(obj, key);
        for (int i = 0; i < uuids.length; i++) {
            if (!uuidToParameter.containsKey(uuids[i])) {
                uuidToParameter.put(uuids[i], new ArrayList<>());
            }
            uuidToParameter.get(uuids[i]).add(params[i]);
            uuidToColorParameter.put(uuids[i], colorParams[i]);
        }
    }

    void setParameterMappings(JsonObject obj, String key, LXParameter param, ColorParameter colorParam) {
        setParameterMappings(obj, key, new LXParameter[] {param}, new ColorParameter[] {colorParam});
    }

    void setParameterMappings(JsonObject obj, String key, DeckDependentMapping mapping) {
        String[] uuids = getUUIDs(obj, key);

        for (int i = 0; i < uuids.length; i++) {
            if (!uuidToDeckDependentMapping.containsKey(uuids[i])) {
                uuidToDeckDependentMapping.put(uuids[i], new ArrayList<>());
                uuidToDeckIndex.put(uuids[i], new ArrayList<>());

            }
            uuidToDeckDependentMapping.get(uuids[i]).add(mapping);
            uuidToDeckIndex.get(uuids[i]).add(i);
        }
    }


    ColorParameter thresholdColor(LXListenableNormalizedParameter p, float t, float below, float equal, float above) {
        String name = String.format("color-%s", p.getLabel());
        ColorParameter cp = new ColorParameter(name);
        LXParameterListener listener = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                float v = p.getNormalizedf();
                float hue;
                if (v < t) {
                    hue = below;
                } else if (v > t) {
                    hue = above;
                } else {
                    hue = equal;
                }
                cp.setColor(LXColor.hsb(hue, 100, 100));
            }
        };
        p.addListener(listener);
        listener.onParameterChanged(p);
        return cp;
    }

    ColorParameter[] crossfaderColors;
    ColorParameter brightnessColor;
    ColorParameter speedColor;
    ColorParameter desaturationColor;
    ColorParameter blurColor;

    ColorParameter[] patternColors;
    ColorParameter[] presetColors;
    ColorParameter[] blendModeColors;
    ColorParameter[] cueColors;
    ColorParameter[] fireColors;




    void addImmediateListener(LXListenableParameter param, LXParameterListener listener) {
        param.addListener(listener);
        listener.onParameterChanged(param);
    }

    void setupColorParameters() {
        crossfaderColors = new ColorParameter[3];
        for (int i = 0; i < 3; i++) {
            String name = String.format("crossfader-%d", i);

            final CompoundParameter fader;
            if (i < 2) {
                fader = pm.deckCrossfaders[i];
            } else {
                fader = lx.engine.crossfader;
            }
            crossfaderColors[i] = thresholdColor(fader, 0.5f, 181, 181, 316);
        }

        brightnessColor = thresholdColor(pm.globalParams.brightness, 0.99f, 181, 130, 130);
        speedColor = thresholdColor(pm.globalParams.speed, 0.5f, 181, 130, 130);
        blurColor = thresholdColor(pm.globalParams.effectParams.blur, 0.01f, 130, 130, 316);
        desaturationColor = thresholdColor(pm.globalParams.effectParams.desaturation, 0.01f, 130, 130, 316);

        patternColors = new ColorParameter[4];
        presetColors = new ColorParameter[4];
        blendModeColors = new ColorParameter[2];
        cueColors = new ColorParameter[4];
        fireColors = new ColorParameter[4];


        for (int i = 0; i < 4; i++) {
            String name = String.format("pattern-%d", i);
            patternColors[i] = new ColorParameter(name);
            final PerformanceGUIController.ChannelWindow window = pm.gui.channelWindows[i];
            final DiscreteParameter active = window.activePatternIndex;
            final int k = i;
            LXParameterListener listener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    UIItemList.Item activeItem = window.patternList.getItems().get(active.getValuei());
                    String label = activeItem.getLabel();
                    int hue = label.hashCode() % 360;
                    patternColors[k].setColor(LXColor.hsb(hue, 100, 100));
                }
            };
            addImmediateListener(active, listener);

        }

        for (int i = 0; i < 4; i++) {
            String name = String.format("preset-%d", i);
            presetColors[i] = new ColorParameter(name);
            final PerformanceGUIController.ChannelWindow window = pm.gui.channelWindows[i];
            final DiscreteParameter active = window.selectedPreset;
            final int k = i;
            LXParameterListener listener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    float hue = pm.presets.get(active.getValuei()).hue;
                    presetColors[k].setColor(LXColor.hsb(hue, 100, 100));
                }
            };
            addImmediateListener(active, listener);
        }

        for (int i = 0; i < 2; i++) {
            String name = String.format("blend-%d", i);
            blendModeColors[i] = new ColorParameter(name);
            final DiscreteParameter blend = pm.decks[i].blendMode;
            final int k = i;
            LXParameterListener listener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    // RAPHTODO
                    float[] hues =
                        new float[] {
                            0.0f, 25.0f, 55.0f, 115.0f, 180.0f, 225.0f, 280.0f,
                        };
                    float hue = hues[blend.getValuei()];
                    blendModeColors[k].setColor(LXColor.hsb(hue, 100, 100));
                }
            };
            addImmediateListener(blend, listener);
        }

        for (int i = 0; i < 4; i++) {
            String name = String.format("cue-%d", i);
            cueColors[i] = new ColorParameter(name);
            final DiscreteParameter cueState = pm.cueState;
            final int k = i;
            LXParameterListener listener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    int s = cueState.getValuei();
                    boolean on = false;
                    if (s == 3) {
                        on = false;
                    } else if (s == 1 && (k == 0 || k == 1)) {
                        on = true;
                    } else if (s == 5 && (k == 2 || k == 3)) {
                        on = true;
                    } else if ((s / 2) == k) {
                        on = true;
                    }

                    int c = LXColor.hsb(316, 100, 100);
                    cueColors[k].setColor(on ? c : LXColor.BLACK);
                }
            };
            addImmediateListener(cueState, listener);

        }

        for (int i = 0; i < 4; i++) {
            String name = String.format("fire-%d", i);
            fireColors[i] = new ColorParameter(name);
            final PerformanceGUIController.ChannelWindow window = pm.gui.channelWindows[i];
            final int k = i;
            LXParameterListener listener = new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    int pI = window.selectedPreset.getValuei();
                    PerformanceManager.Preset preset = pm.presets.get(pI);
                    boolean on = preset.patternName != null;
                    fireColors[k].setColor(LXColor.hsb(preset.hue, 100, on ? 100 : 0));
                }
            };
            addImmediateListener(window.selectedPreset, listener);
            addImmediateListener(window.savePushed, listener);

        }
    }

    void loadMapping() {
        byte[] bytes = SLStudio.applet.loadBytes(MAPPING_FILENAME);
        if (bytes == null) {
            return;
        }
        try {
            JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);

            JsonObject mappingsObj = json.getAsJsonObject(MAPPINGS_KEY);
            JsonArray upsideDown = json.getAsJsonArray(UPSIDE_DOWN_KEY);


            setParameterMappings(mappingsObj, CENTER_CROSSFADER, pm.globalParams.crossfade, crossfaderColors[2]);
            setParameterMappings(mappingsObj, DECK_CROSSFADER, pm.deckCrossfaders, crossfaderColors);

            setParameterMappings(mappingsObj, BRIGHTNESS, pm.globalParams.brightness, brightnessColor);
            setParameterMappings(mappingsObj, SPEED, pm.globalParams.speed, speedColor);
            setParameterMappings(mappingsObj, BLUR, pm.globalParams.effectParams.blur, blurColor);
            setParameterMappings(mappingsObj, DESATURATION, pm.globalParams.effectParams.desaturation, desaturationColor);


            setParameterMappings(mappingsObj, PATTERN_SCROLL, new DeckDependentMapping() {
                @Override
                LXParameter getParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return pm.gui.channelWindows[wI].activePatternIndex;
                }

                @Override
                ColorParameter getColorParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return patternColors[wI];
                }
            });


            setParameterMappings(mappingsObj, PRESET_SCROLL, new DeckDependentMapping() {
                @Override
                LXParameter getParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return pm.gui.channelWindows[wI].selectedPreset;
                }
                @Override
                ColorParameter getColorParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return presetColors[wI];
                }
            });

            setParameterMappings(mappingsObj, PRESET_SAVE, new DeckDependentMapping() {
                @Override
                LXParameter getParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return pm.gui.channelWindows[wI].savePushed;
                }
                @Override
                ColorParameter getColorParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return presetColors[wI];
                }
            });

            setParameterMappings(mappingsObj, PRESET_FIRE, new DeckDependentMapping() {
                @Override
                LXParameter getParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return pm.gui.channelWindows[wI].firePushed;
                }
                @Override
                ColorParameter getColorParameter(int deckI, int channelI) {
                    int wI = deckI * 2 + channelI;
                    return fireColors[wI];
                }
            });

            setParameterMappings(mappingsObj, BLENDMODE_SCROLL, new LXParameter[]{pm.decks[0].blendMode, pm.decks[1].blendMode}, blendModeColors);


            setParameterMappings(mappingsObj, CUE_BUTTONS, cuesPressed, cueColors);

            for (JsonElement el : upsideDown) {
                String uuid = el.getAsString();
                uuidToUpsideDown.put(uuid, true);
            }

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

}
