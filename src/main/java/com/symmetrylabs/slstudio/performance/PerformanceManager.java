package com.symmetrylabs.slstudio.performance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
                    if (pat != null) {
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
    }

    public class PerformanceDeck extends LXComponent {
        public PerformanceChannel[] channels;
        public CompoundParameter crossfade;
        public ObjectParameter<LXBlend> blendMode;
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

            crossfade = new CompoundParameter("crossfade", 0);
            crossfade.setPolarity(LXParameter.Polarity.BIPOLAR);
            blendMode = channels[CHANNELS_PER_DECK - 1].channel.blendMode;
            lastCrossfade = crossfade.getValuef();

            lx.engine.addLoopTask(new LXLoopTask() {
                @Override
                public void loop(double v) {
                    if (crossfade.getValuef() != lastCrossfade) {
                        setChannelFaders();
                        lastCrossfade = crossfade.getValuef();
                    }
                }
            });
            setChannelFaders();
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
                    blur = ((BlurEffect)e).amount;
//                    addLink(((BlurEffect)e).amount, blur);
                }
                if (e instanceof DesaturationEffect) {
                    desaturation = (CompoundParameter)((DesaturationEffect)e).getParameter("amount");
//                    addLink((CompoundParameter)((DesaturationEffect)e).getParameter("amount"), desaturation);
                }
                if (e instanceof ColorShiftEffect) {
                    hueShift = ((ColorShiftEffect)e).shift;
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

    public BooleanParameter performanceModeInitialized;
    public DiscreteParameter cueState;
    public PerformanceGlobalParams globalParams;
    public PerformanceDeck[] decks;
    public PerformanceGUIController gui;
    private final SLStudioLX lx;
    public HashSet<String> hiddenPatterns;

    private void setupDecks() {
        decks = new PerformanceDeck[N_DECKS];
        for (int i = 0; i < N_DECKS; i++) {
            decks[i] = new PerformanceDeck(lx, this, i);
        }

        globalParams = new PerformanceGlobalParams(lx);


    }

    private void addEffects(LXBus c) {
        for (int i = 0; i < c.effects.size(); i++) {
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


                LXChannel.CrossfadeGroup group = deckIndex == 0 ? LXChannel.CrossfadeGroup.A : LXChannel.CrossfadeGroup.B;
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

    private void saveAndRestart() {
        File proj = lx.getProject();
        lx.saveProject(proj);
        lx.applet.saveStrings(SLStudio.RESTART_FILE_NAME, new String[0]);
        lx.applet.exit();
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

        gui.createFaderWindow(decks[0].crossfade, 0);
        gui.createFaderWindow(lx.engine.crossfader, 1);
        gui.createFaderWindow(decks[1].crossfade, 2);

        cueState.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                setCue();
            }
        });
        setCue();

        gui.createGlobalWindow(this);

    }

    static private String HIDDEN_PATTERNS_KEY = "hiddenPatterns";

    @Override
    public void save(LX lx, JsonObject obj) {
        super.save(lx, obj);
        JsonArray hidden = new JsonArray();
        for (String p : hiddenPatterns) {
            hidden.add(p);
        }

        obj.add(HIDDEN_PATTERNS_KEY, hidden);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);

        if (obj.has(HIDDEN_PATTERNS_KEY)) {
            for (JsonElement e : obj.getAsJsonArray(HIDDEN_PATTERNS_KEY)) {
                hiddenPatterns.add(e.getAsString());
            }
        }
    }

    private void setCue() {
        int cueI = cueState.getValuei();
        BooleanParameter[] cues = new BooleanParameter[]{
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

    public PerformanceManager(SLStudioLX lx_) {
        super(lx_);
        lx = lx_;

        performanceModeInitialized = new BooleanParameter("performanceModeInitialized", false);
        cueState = new DiscreteParameter("cueState", 3, 0, 7);

        addParameter(performanceModeInitialized);
        addParameter(cueState);

        hiddenPatterns = new HashSet<String>();

    }

}
