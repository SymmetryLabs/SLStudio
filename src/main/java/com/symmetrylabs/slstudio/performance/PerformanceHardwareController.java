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

    final LXMidiRemote[] twisters;
    final LXMidiRemote[] fighters;

    final TwisterListener[] twisterListeners;
    final FighterListener[] fighterListeners;

    static class FighterListener implements LXMidiListener {
        int i;

        FighterListener(int i) {
            this.i = i;
        }

        @Override
        public void noteOnReceived(MidiNoteOn midiNoteOn) {

        }

        @Override
        public void noteOffReceived(MidiNote midiNote) {

        }

        @Override
        public void controlChangeReceived(MidiControlChange midiControlChange) {

        }

        @Override
        public void programChangeReceived(MidiProgramChange midiProgramChange) {

        }

        @Override
        public void pitchBendReceived(MidiPitchBend midiPitchBend) {

        }

        @Override
        public void aftertouchReceived(MidiAftertouch midiAftertouch) {

        }
    }

    class TwisterListener implements LXMidiListener {
        int deckI;

        LXParameterListener[] writeListeners;

        ArrayList<LXListenableNormalizedParameter> params = null;

        TwisterListener(int deckI) {
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


            getAndWriteParams();
            pm.decks[deckI].activeChannel.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    getAndWriteParams();
                }
            });

            for (int j = 0; j < 2; j++) {
                final int k = j;
                int wI = (deckI * 2) + j;
                DiscreteParameter active = pm.gui.channelWindows[wI].activePatternIndex;
                active.addListener(new LXParameterListener() {
                    @Override
                    public void onParameterChanged(LXParameter lxParameter) {
                        if (pm.decks[deckI].activeChannel.getValuei() != k) {
                            return;
                        }
                        getAndWriteParams();
                    }
                });
            }



        }


        void getAndWriteParams() {
            getParams();
            writeParamStates();
        }

        void getParams() {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i) != null) {
                        params.get(i).removeListener(writeListeners[i]);
                    }
                }
            }
            int maxParamKnobs = 12;
            int active = pm.decks[deckI].activeChannel.getValuei();
            PerformanceManager.PerformanceChannel activeChannel = pm.decks[deckI].channels[active];
            params = activeChannel.getKnobParameters();
            while (params.size() < maxParamKnobs) {
                params.add(null);
            }
            params.addAll(activeChannel.getEffectParameters());
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) != null) {
                    params.get(i).addListener(writeListeners[i]);
                }
            }
        }

        void writeParamState(int knobI) {
            int RGB_OFF = 17;
            int RGB_MED = 32;
            int RGB_MAX = 47;
            LXMidiOutput out = twisters[deckI].getOutput();

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

        }

        @Override
        public void noteOffReceived(MidiNote midiNote) {

        }

        @Override
        public void controlChangeReceived(MidiControlChange midiControlChange) {
            int channel = midiControlChange.getChannel();
            int cc = midiControlChange.getCC();
            double v = midiControlChange.getNormalized();
            if (cc >= params.size() | params.get(cc) == null) {
                return;
            }
            if (channel == 0) {
                params.get(cc).setNormalized(v);
                float yo = params.get(cc).getValuef();
                System.out.printf("%s: %.2f %.2f", params.get(cc).getLabel(), v, yo);
            }
        }

        @Override
        public void programChangeReceived(MidiProgramChange midiProgramChange) {

        }

        @Override
        public void pitchBendReceived(MidiPitchBend midiPitchBend) {

        }

        @Override
        public void aftertouchReceived(MidiAftertouch midiAftertouch) {

        }
    }



    void bindMidi() {
        ArrayList<LXMidiInput> fighterInputs = new ArrayList<LXMidiInput>();
        ArrayList<LXMidiInput> twisterInputs = new ArrayList<LXMidiInput>();
        ArrayList<LXMidiOutput> fighterOutputs = new ArrayList<LXMidiOutput>();
        ArrayList<LXMidiOutput> twisterOutputs = new ArrayList<LXMidiOutput>();

        List<LXMidiSurface> surfaces = lx.engine.midi.surfaces;

        for (LXMidiInput in : lx.engine.midi.inputs) {
            String name = in.getName();
            if (name.contains("Twister")) {
                twisterInputs.add(in);
            } else if (name.contains("Fighter")) {
                fighterInputs.add(in);
            }
        }

        for (LXMidiOutput out : lx.engine.midi.outputs) {
            String name = out.getName();
            if (name.contains("Twister")) {
                twisterOutputs.add(out);
            } else if (name.contains("Fighter")) {
                twisterOutputs.add(out);
            }
        }

        for (int i = 0; i < Math.min(2, twisterInputs.size()); i++) {
            LXMidiInput in = twisterInputs.get(i);
            LXMidiOutput out = twisterOutputs.get(i);
            in.open();
            out.open();
            twisters[i] = new LXMidiRemote(in, out);
            twisterListeners[i] = new TwisterListener(i);
            in.addListener(twisterListeners[i]);
        }

        for (int i = 0; i < Math.min(2, fighterInputs.size()); i++) {
            LXMidiInput in = fighterInputs.get(i);
            LXMidiOutput out = fighterOutputs.get(i);
            in.open();
            out.open();
            fighters[i] = new LXMidiRemote(in, out);
            fighterListeners[i] = new FighterListener(i);
            in.addListener(fighterListeners[i]);
        }

//        for (LXMidiRemote twister : twisters) {
//            for (int deckI = 0; deckI < 16; deckI++) {
//                LXParameter p = pm.decks[0].channels[0].getKnobParameters().get(deckI);
//                twister.bindController(p, 0, deckI);
//            }
//        }
//        System.out.println("AYE");
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

        twisters = new LXMidiRemote[2];
        fighters = new LXMidiRemote[2];
        twisterListeners = new TwisterListener[2];
        fighterListeners = new FighterListener[2];



        lx.engine.midi.whenReady(new Runnable() {
            public void run() {
                bindMidi();
            }
        });


//        startPaletteThread();


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

        brightnessColor = thresholdColor(pm.globalParams.brightness, 1.0f, 181, 130, 130);
        speedColor = thresholdColor(pm.globalParams.brightness, 0.5f, 181, 130, 316);
        blurColor = thresholdColor(pm.globalParams.effectParams.blur, 0.0f, 130, 130, 316);
        desaturationColor = thresholdColor(pm.globalParams.effectParams.desaturation, 0.0f, 130, 316, 316);

        patternColors = new ColorParameter[4];
        presetColors = new ColorParameter[4];
        blendModeColors = new ColorParameter[2];
        cueColors = new ColorParameter[4];

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
                            0.0f, 25.0f, 60.0f, 115.0f, 180.0f, 225.0f, 280.0f,
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

                    cueColors[k].setColor(on ? LXColor.RED : LXColor.BLACK);
                }
            };
            addImmediateListener(cueState, listener);
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
                    return null;
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

    void startPaletteThread() {
//        Runnable run =
//            new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            };
//        Thread paletteThread = new Thread(run);
//        paletteThread.setName("HOWDY");
//        paletteThread.start();
    }
}
