package com.symmetrylabs.slstudio.performance;

import com.google.gson.*;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.effect.ColorShiftEffect;
import heronarts.lx.*;

import heronarts.lx.blend.LXBlend;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.DesaturationEffect;
import heronarts.lx.parameter.*;

import java.util.*;
import java.io.*;

public class PerformanceManager extends LXComponent {

    public static int CHANNELS_PER_DECK = 2;
    public static int N_DECKS = 2;
    public static int N_CHANNELS = CHANNELS_PER_DECK * N_DECKS;

    public BooleanParameter performanceModeInitialized;
    public DiscreteParameter cueState;
    public PerformanceGlobalParams globalParams;
    public PerformanceDeck[] decks;
    public PerformanceGUIController gui;
    public PerformanceHardwareController hardware;
    private final SLStudioLX lx;
    public HashSet<String> hiddenPatterns;
    public ArrayList<Preset> presets;
    public BooleanParameter presetsLoaded;

    public CompoundParameter[] deckCrossfaders;

    public class PerformanceChannel extends LXComponent {
        public PerformanceEffectParams effectParams;
        public int globalIndex;
        public int indexInDeck;

        public LXChannel channel;

        public PerformanceDeck deck;
        public PerformanceManager manager;

        PerformanceChannel(LX lx, PerformanceDeck deck_, int indexInDeck_) {
            super(lx);

            deck = deck_;
            manager = deck.manager;
            globalIndex = (deck.globalIndex * CHANNELS_PER_DECK) + indexInDeck_;
            indexInDeck = indexInDeck_;

            int nPatterns = 1;

            channel = lx.engine.channels.get(globalIndex);
            effectParams = new PerformanceEffectParams(lx, channel);


            addNewPatterns();
        }

        private void addNewPatterns() {
            HashSet<String> inUse = new HashSet<String>();
            for (LXPattern p : channel.patterns) {
                inUse.add(p.getClass().getCanonicalName());
            }

            List<Class<? extends LXPattern>> available = lx.getRegisteredPatterns();
            for (Class<? extends LXPattern> c : available) {
                if (!inUse.contains(c.getCanonicalName())) {
                    LXPattern pat = lx.instantiatePattern(c);
                    // stupid hack below
                    if (pat != null && !pat.getLabel().equals("SolidColor")) {
                        try {
                            pat.onInactive();

                            channel.addPattern(pat);
                        } catch (Exception e) {
                            System.err.printf("Pattern %s was not safe to add\n", pat.getLabel());
                        }
                    }
                }
            }
        }

        public ArrayList<LXListenableNormalizedParameter> getKnobParameters() {
            int maxKnobs = 12;
            ArrayList<LXListenableNormalizedParameter> params =
                    new ArrayList<LXListenableNormalizedParameter>();

            Collection<LXParameter> parameters = channel.getActivePattern().getParameters();
            for (LXParameter param : parameters) {
                if (!(param instanceof LXListenableNormalizedParameter)) continue;

                if (!(param instanceof BooleanParameter)) {
                    params.add((LXListenableNormalizedParameter) param);
                }

                if (params.size() >= maxKnobs) {
                    break;
                }


            }
            for (LXParameter p : params) {
//          System.out.println(p.getPath());
            }
            return params;
        }

        public ArrayList<BooleanParameter> getButtonParameters() {
            int maxButtons = 8;
            ArrayList<BooleanParameter> params = new ArrayList<BooleanParameter>();

            Collection<LXParameter> parameters = channel.getActivePattern().getParameters();
            for (LXParameter param : parameters) {
                if (!(param instanceof LXListenableNormalizedParameter)) continue;

                if ((param instanceof BooleanParameter)) {
                    params.add((BooleanParameter) param);
                }

                if (params.size() >= maxButtons) {
                    break;
                }
            }
            return params;
        }

        public ArrayList<LXListenableNormalizedParameter> getEffectParameters() {
            ArrayList<LXListenableNormalizedParameter> params = new ArrayList<LXListenableNormalizedParameter>();


            params.add(effectParams.blur);

            params.add(effectParams.desaturation);

            params.add(effectParams.hueShift);

            return params;
        }

        public ArrayList<String> getEffectLabels() {
            ArrayList<String> labels = new ArrayList<String>();
            labels.add("Blur");
            labels.add("Desat");
            labels.add("Hue");
            return labels;
        }
    }




        public class PerformanceDeck extends LXComponent {
        public PerformanceChannel[] channels;
        public CompoundParameter crossfade;
        public ObjectParameter<LXBlend> blendMode;
        public DiscreteParameter activeChannel;
        public int globalIndex;
        public PerformanceManager manager;
        private float lastCrossfade;

        PerformanceDeck(LX lx, PerformanceManager manager_, int globalIndex_) {
            manager = manager_;
            globalIndex = globalIndex_;

            channels = new PerformanceChannel[CHANNELS_PER_DECK];
            for (int i = 0; i < CHANNELS_PER_DECK; i++) {
                channels[i] = new PerformanceChannel(lx, this, i);
            }

            crossfade = manager.deckCrossfaders[globalIndex];
            blendMode = channels[CHANNELS_PER_DECK - 1].channel.blendMode;
            lastCrossfade = crossfade.getValuef();

            activeChannel = new DiscreteParameter("activeChannel", 0, 0, 2);

            lx.engine.addLoopTask(
                    new LXLoopTask() {
                        @Override
                        public void loop(double v) {
                            if (crossfade.getValuef() != lastCrossfade) {
                                setChannelFaders();
                                lastCrossfade = crossfade.getValuef();
                                setActiveChannel();
                            }
                        }
                    });
            setChannelFaders();

            setActiveChannel();
        }

        void setActiveChannel() {
            float val = crossfade.getValuef();
            activeChannel.setValue(val <= 0.5 ? 0 : 1);
        }

        public void setChannelFaders() {
            for (int i = 0; i < CHANNELS_PER_DECK; i++) {
                float cf = crossfade.getValuef();
                float val = i == 0 ? (1.0f - cf) : cf;
                channels[i].channel.fader.setValue(val);
            }
        }
    }

    public class PerformanceEffectParams extends LXComponent {
        public CompoundParameter blur;
        public CompoundParameter desaturation;
        public BoundedParameter hueShift;

        PerformanceEffectParams(LX lx, LXBus b) {
            super(lx);

            blur = new CompoundParameter("Blur", 0);
            desaturation = new CompoundParameter("Desat", 0);
            //            hueShift = new CompoundParameter("Hue", 0, 0, 360);

            List<LXEffect> effects = b.getEffects();
            for (LXEffect e : effects) {
                if (e instanceof BlurEffect) {
                    blur = ((BlurEffect) e).amount;
                    //                    addLink(((BlurEffect)e).amount, blur);
                }
                if (e instanceof DesaturationEffect) {
                    desaturation = (CompoundParameter) ((DesaturationEffect) e).getParameter("amount");
                    //                    addLink((CompoundParameter)((DesaturationEffect)e).getParameter("amount"),
                    // desaturation);
                }
                if (e instanceof ColorShiftEffect) {
                    hueShift = ((ColorShiftEffect) e).shift;
                    //                    addLink(((ColorShiftEffect)e).shift, hueShift);
                }
            }
        }
    }

    public class PerformanceGlobalParams extends LXComponent {
        public CompoundParameter crossfade;
        public ObjectParameter<LXBlend> blendMode;
        public BoundedParameter brightness;
        public BoundedParameter speed;
        public PerformanceEffectParams effectParams;

        PerformanceGlobalParams(LX lx) {
            super(lx);
            crossfade = lx.engine.crossfader;
            blendMode = lx.engine.crossfaderBlendMode;
            brightness = lx.engine.output.brightness;
            speed = lx.engine.speed;
            effectParams = new PerformanceEffectParams(lx, lx.engine.masterChannel);
        }
    }

    private void setupDecks() {
        decks = new PerformanceDeck[N_DECKS];
        for (int i = 0; i < N_DECKS; i++) {
            decks[i] = new PerformanceDeck(lx, this, i);
        }

        globalParams = new PerformanceGlobalParams(lx);
    }

    private void addEffects(LXBus c) {
        int nExisting = c.effects.size();
        for (int i = 0; i < nExisting; i++) {
            c.removeEffect(c.effects.get(0));
        }

        ColorShiftEffect colorShift = new ColorShiftEffect(lx);
        DesaturationEffect desaturation = new DesaturationEffect(lx);
        BlurEffect blur = new BlurEffect(lx);

        colorShift.shift.setValue(0);
        desaturation.getParameter("amount").setValue(0);
        blur.amount.setValue(0);

        colorShift.enabled.setValue(true);
        desaturation.enabled.setValue(true);
        blur.enabled.setValue(true);

        c.addEffect(colorShift);
        c.addEffect(desaturation);
        c.addEffect(blur);
    }

    public void teardownPerformanceMode() {
        performanceModeInitialized.setValue(false);
        saveAndRestart();
    }

    public void initializePerformanceMode() {

        // we need to remove all the existing channels. however, LX won't let us remove the last channel
        // so, we keep track of how many channels existed before initialization, and remove them after
        int nExistingChannels = lx.engine.channels.size();

        for (int deckIndex = 0; deckIndex < N_DECKS; deckIndex++) {
            for (int chanIndex = 0; chanIndex < CHANNELS_PER_DECK; chanIndex++) {
                LXChannel c = lx.engine.addChannel();
                addEffects(c);

                c.label.setValue(String.format("D %d C %d", deckIndex + 1, chanIndex + 1));

                LXChannel.CrossfadeGroup group =
                        deckIndex == 0 ? LXChannel.CrossfadeGroup.A : LXChannel.CrossfadeGroup.B;
                c.crossfadeGroup.setValue(group);
            }
        }

        for (int i = 0; i < nExistingChannels; i++) {
            lx.engine.removeChannel(lx.engine.channels.get(0));
        }

        addEffects(lx.engine.masterChannel);

        performanceModeInitialized.setValue(true);

        saveAndRestart();
    }

    public void restart() {
        lx.applet.saveStrings(SLStudio.RESTART_FILE_NAME, new String[0]);
        lx.applet.exit();
    }

    public void saveToFileAndRestart(final File file) {
        lx.saveProject(file);
        restart();
    }

    private void saveAndRestart() {
        File proj = lx.getProject();
        if (proj == null) {
            lx.applet.selectOutput(
                    "Select a file to save to:",
                    "saveToFileAndRestart",
                    lx.applet.saveFile("project.lxp"),
                    PerformanceManager.this);
            return;
        }
        saveToFileAndRestart(proj);
    }

    public void start() {
        if (!performanceModeInitialized.getValueb()) {
            return;
        }

        setupDecks();
        lx.ui.toggleSidebars();
        lx.ui.bottomTray.setVisible(false);
        lx.ui.reflow();

        gui = new PerformanceGUIController(lx, lx.ui, this);

        for (PerformanceDeck deck : decks) {
            for (PerformanceChannel chan : deck.channels) {
                gui.createChannelWindow(chan);
            }
        }

        gui.createFaderWindow(decks[0].crossfade, decks[0].blendMode, 0);
        gui.createFaderWindow(lx.engine.crossfader, globalParams.blendMode, 1);
        gui.createFaderWindow(decks[1].crossfade, decks[1].blendMode, 2);

        cueState.addListener(
                new LXParameterListener() {
                    @Override
                    public void onParameterChanged(LXParameter lxParameter) {
                        setCue();
                    }
                });
        setCue();

        gui.createGlobalWindow(this);

        hardware = new PerformanceHardwareController(lx, this);
    }

    private static String HIDDEN_PATTERNS_KEY = "hiddenPatterns";
    private static String PRESETS_KEY = "presets";

    public boolean isHidden(LXPattern pattern) {
        return hiddenPatterns.contains(pattern.getLabel());
    }

    public void toggleHidden(LXPattern pattern) {
        String k = pattern.getLabel();
        if (hiddenPatterns.contains(k)) {
            hiddenPatterns.remove(k);
        } else {
            hiddenPatterns.add(k);
        }

        gui.updateAllPatternLists();


    }

    public void updateAllTwisters() {
        for (PerformanceHardwareController.TwisterListener t : hardware.twisterListeners) {
            if (t != null) {
                t.getAndWriteParams();
            }
        }
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);

        JsonArray hidden = new JsonArray();
        for (String p : hiddenPatterns) {
            hidden.add(p);
        }
        obj.add(HIDDEN_PATTERNS_KEY, hidden);

        JsonArray presetArr = new JsonArray();
        for (Preset p : presets) {
            presetArr.add(p.toJSON());
        }
        obj.add(PRESETS_KEY, presetArr);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);

        if (obj.has(HIDDEN_PATTERNS_KEY)) {
            for (JsonElement e : obj.get(HIDDEN_PATTERNS_KEY).getAsJsonArray()) {
                hiddenPatterns.add(e.getAsString());
            }
        }

        if (obj.has(PRESETS_KEY) && obj.getAsJsonArray(PRESETS_KEY).size() > 0)  {
             for (JsonElement e : obj.getAsJsonArray(PRESETS_KEY)) {
                presets.add(new Preset(e.getAsJsonObject()));
            }
        } else {
            for (int i = 0; i < Preset.MAX_PRESETS; i++) {
                presets.add(new Preset(i));
            }
        }
        presetsLoaded.setValue(true);
    }

    private void setCue() {
        int cueI = cueState.getValuei();
        BooleanParameter[] cues =
                new BooleanParameter[] {
                    decks[0].channels[0].channel.cueActive,
                    lx.engine.cueA,
                    decks[0].channels[1].channel.cueActive,
                    null,
                    decks[1].channels[0].channel.cueActive,
                    lx.engine.cueB,
                    decks[1].channels[1].channel.cueActive,
                };
        for (BooleanParameter cue : cues) {
            if (cue != null) {
                cue.setValue(false);
            }
        }
        if (cues[cueI] != null) {
            cues[cueI].setValue(true);
        }
    }

    static class Preset {
        int index;
        String name;
        float hue;
        String patternName;
        HashMap<String, Float> parameterValues;

        static int MAX_PRESETS = 7;

        public Preset(int index) {
            this.index = index;

            float[] hues =
                    new float[] {
                        0.0f, 25.0f, 55.0f, 115.0f, 180.0f, 225.0f, 280.0f,
                    };

            String names[] =
                    new String[] {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"};

            hue = hues[index];
            name = names[index];

            patternName = null;
            parameterValues = new HashMap<String, Float>();
        }

        public Preset(JsonObject obj) {
            index = obj.get("index").getAsInt();
            name = obj.get("name").getAsString();
            hue = obj.get("hue").getAsFloat();
            parameterValues = new HashMap<String, Float>();

            // LX doesn't serialize nulls, hence this ridiculous boolean hack
            JsonPrimitive patName = obj.get("patternName").getAsJsonPrimitive();
            if (patName.isBoolean()) {
                patternName = null;
            } else {
                patternName = patName.getAsString();
            }



            JsonObject values = obj.get("parameterValues").getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : values.entrySet()) {
                parameterValues.put(e.getKey(), e.getValue().getAsFloat());
            }
        }

        public JsonObject toJSON() {
            JsonObject obj = new JsonObject();
            obj.addProperty("index", index);
            obj.addProperty("name", name);
            obj.addProperty("hue", hue);

            if (patternName == null) {
                obj.addProperty("patternName", false);
            } else {
                obj.addProperty("patternName", patternName);

            }


            JsonObject values = new JsonObject();
            for (Map.Entry<String, Float> e : parameterValues.entrySet()) {
                values.addProperty(e.getKey(), e.getValue());
            }
            obj.add("parameterValues", values);

            return obj;
        }

        void loadFrom(LXChannel channel) {
            LXPattern pat = channel.getActivePattern();
            patternName = pat.getLabel();
            parameterValues.clear();
            for (LXParameter param : pat.getParameters()) {
                parameterValues.put(param.getPath(), param.getValuef());
            }
            List<LXEffect> effects = channel.getEffects();
            for (int i = 0; i < effects.size(); i++) {
                LXEffect e = effects.get(i);
                for (LXParameter param : e.getParameters()) {
                    String path = String.format("effect-%d-%s", i, param.getPath());
                    parameterValues.put(path, param.getValuef());
                }
            }
        }

        LXPattern applyTo(LXChannel channel) {
            LXPattern pat = channel.getPattern(patternName);
            for (Map.Entry<String, Float> e : parameterValues.entrySet()) {
                String path = e.getKey();
                if (path.contains("effect-")) {
                    String[] parts = path.split("-");
                    int eI = Integer.parseInt(parts[1]);
                    String eP = parts[2];
                    channel.getEffect(eI).getParameter(eP).setValue(e.getValue());
                } else {
                    pat.getParameter(path).setValue(e.getValue());
                }
            }
            return pat;
        }
    }

    public PerformanceManager(SLStudioLX lx_) {
        super(lx_);
        lx = lx_;

        performanceModeInitialized = new BooleanParameter("performanceModeInitialized", false);
        cueState = new DiscreteParameter("cueState", 3, 0, 7);
        String[] cueLabels = new String[] {"1", "L", "2", "All", "3", "R", "4"};
        cueState.setOptions(cueLabels);


        addParameter(performanceModeInitialized);
        addParameter(cueState);

        deckCrossfaders = new CompoundParameter[N_DECKS];
        for (int i = 0; i < N_DECKS; i++) {
            String name = String.format("crossfader-%d", i);
            deckCrossfaders[i] = new CompoundParameter(name, 0);
            deckCrossfaders[i].setPolarity(LXParameter.Polarity.BIPOLAR);
            addParameter(deckCrossfaders[i]);
        }

        hiddenPatterns = new HashSet<String>();
        presets = new ArrayList<Preset>();
        presetsLoaded = new BooleanParameter("presetsLoaded", false);

    }
}
