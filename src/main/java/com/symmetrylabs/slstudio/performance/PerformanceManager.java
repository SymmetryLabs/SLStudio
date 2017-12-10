package com.symmetrylabs.slstudio.performance;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEffect;
import heronarts.lx.LXEngine;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.p3lx.ui.UIWindow;

import java.util.List;

import static com.symmetrylabs.slstudio.SLStudio.*;
import static heronarts.lx.color.LXColor.WHITE;
import static heronarts.lx.color.LXColor.RED;
import static processing.core.PApplet.println;


public class PerformanceManager extends LXComponent {
    final CompoundParameter aFader = new CompoundParameter("aFader");
    final CompoundParameter bFader = new CompoundParameter("bFader");
    public LXListenableNormalizedParameter upL = null;
    public LXListenableNormalizedParameter downL = null;
    public LXListenableNormalizedParameter upR = null;
    public LXListenableNormalizedParameter downR = null;


    final CompoundParameter aBlur = new CompoundParameter("aBlur");
    final CompoundParameter bBlur = new CompoundParameter("bBlur");
    public LXParameter[] blurs = new LXParameter[4];

    final BoundedParameter aColor = new BoundedParameter("aColor", 0, 360);
    final BoundedParameter bColor = new BoundedParameter("bColor", 0, 360);
    public LXParameter[] colors = new LXParameter[4];


    final CompoundParameter lDummy = new CompoundParameter("lDummy");
    final CompoundParameter rDummy = new CompoundParameter("rDummy");


    DiscreteParameter deckOneChannel;
    DiscreteParameter deckTwoChannel;
    DiscreteParameter deckThreeChannel;
    DiscreteParameter deckFourChannel;

    DiscreteParameter channelSelections[] = new DiscreteParameter[4];
    public DeckWindow[] deckWindows = new DeckWindow[4];
    public UIWindow[] crossfaders = new UIWindow[3];

    int oldDeckL = -1;
    int oldDeckR = -1;

    SLStudioLX.UI ui;
    private LXChannel.CrossfadeGroup oldAB = null;

    class Listener implements LXEngine.Listener {
        @Override
        public void channelAdded(LXEngine engine, LXChannel channel) {
            println("CHANNEL ADDED");
            setChannelOptions();
        }

        @Override
        public void channelMoved(LXEngine engine, LXChannel channel) {
            setChannelOptions();
        }

        @Override
        public void channelRemoved(LXEngine engine, LXChannel channel) {
            setChannelOptions();
        }
    }

    void setChannelOptions() {
        List<LXChannel> channels = SLStudio.applet.lx.engine.getChannels();
        if (channels.size() == 0) return;
        String[] options = new String[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            options[i] = channels.get(i).getLabel();
        }


        for (int i = 0; i < 4; i++) {
            DiscreteParameter param = channelSelections[i];
            DeckWindow w = deckWindows[i];
            param.setOptions(options);
            w.dropMenu.setParameter(param);
        }
    }


    public PerformanceManager(LX lx) {
        super(lx);

        aFader.setPolarity(LXParameter.Polarity.BIPOLAR);
        aFader.addListener(parameter -> propagateLeftFader());

        bFader.setPolarity(LXParameter.Polarity.BIPOLAR);
        bFader.addListener(parameter -> propagateRightFader());

        aBlur.addListener(parameter -> {
            if (blurs[0] != null) blurs[0].setValue(aBlur.getValuef());
            if (blurs[1] != null) blurs[1].setValue(aBlur.getValuef());
        });

        bBlur.addListener(parameter -> {
            if (blurs[2] != null) blurs[2].setValue(bBlur.getValuef());
            if (blurs[3] != null) blurs[3].setValue(bBlur.getValuef());
        });

        aColor.addListener(parameter -> {
            println("CHANGED", aColor.getValuef());
            if (colors[0] != null) colors[0].setValue(aColor.getValuef());
            if (colors[1] != null) colors[1].setValue(aColor.getValuef());
        });

        bColor.addListener(parameter -> {
            if (colors[2] != null) colors[2].setValue(bColor.getValuef());
            if (colors[3] != null) colors[3].setValue(bColor.getValuef());
        });

        //  lDummy.addListener(new LXParameterListener() {
        //      public void onParameterChanged(LXParameter parameter) {
        //          int mult = 10;
        //          float v = lDummy.getValuef();
        //          if (v < 0.1) {
        //              v *= mult;
        //              aColor.setValue((aColor.getValuef() + v) % 360);
        //          }
        //          if (v > 0.9) {
        //              v = mult * (1.0 - v);
        //              float raw = (aColor.getValuef() - v);
        //              float mod = raw < 0 ? 360 - raw : raw % 360;
        //              aColor.setValue(raw);
        //          }
        //      }
        //  });

        // rDummy.addListener(new LXParameterListener() {
        //      public void onParameterChanged(LXParameter parameter) {
        //          int mult = 10;
        //          float v = rDummy.getValuef();
        //          if (v < 0.1) {
        //              v *= mult;
        //              bColor.setValue((bColor.getValuef() + v) % 360);
        //          }
        //          if (v > 0.9) {
        //              v = mult * (1.0 - v);
        //              float raw = (bColor.getValuef() - v);
        //              float mod = raw < 0 ? 360 - raw : raw % 360;
        //              bColor.setValue(raw);
        //          }
        //      }
        //  });


        channelSelections[0] = deckOneChannel = new DiscreteParameter("deckOneChannel", 100000);
        channelSelections[1] = deckTwoChannel = new DiscreteParameter("deckTwoChannel", 100000);
        channelSelections[2] = deckThreeChannel = new DiscreteParameter("deckThreeChannel", 100000);
        channelSelections[3] = deckFourChannel = new DiscreteParameter("deckFourChannel", 100000);

        for (DiscreteParameter param : channelSelections) {
            addParameter(param);
        }

    }

    public void propagateLeftFader() {
        float v = aFader.getValuef();
        upL.setValue(1.0 - v);
        downL.setValue(v);
    }

    public void propagateRightFader() {
        float v = bFader.getValuef();
        upR.setValue(1.0 - v);
        downR.setValue(v);
    }

    float getWindowX(int i) {
        switch (i) {
            case 0:
                return 0;
            case 1:
                return CHAN_WIDTH + PAD;
            case 2:
                return ui.getWidth() - (2 * (CHAN_WIDTH + PAD));
            case 3:
                return ui.getWidth() - (CHAN_WIDTH + PAD);
            default:
                return 0;
        }
    }


    void addUI() {

        for (int i = 0; i < 4; i++) {
            float x = getWindowX(i);
            DiscreteParameter selection = channelSelections[i];
            DeckWindow w = new DeckWindow(selection, i, this, ui, "NEVER SEE THIS", x, CHAN_Y, CHAN_WIDTH, CHAN_HEIGHT);
            ui.addLayer(w);
            w.setVisible(false);
            deckWindows[i] = w;
        }

        float w = (2 * CHAN_WIDTH + PAD) / 2;
        float h = 50.0f;
        float y = CHAN_Y + CHAN_HEIGHT;

        FaderWindow fL = new FaderWindow(aFader, ui, "", CHAN_WIDTH / 2f, y, w, h);
        ui.addLayer(fL);
        fL.setVisible(false);

        FaderWindow fR = new FaderWindow(bFader, ui, "", ui.getWidth() - CHAN_WIDTH - (w / 2), y, w, h);
        ui.addLayer(fR);
        fR.setVisible(false);

        FaderWindow fC = new FaderWindow(SLStudio.applet.lx.engine.crossfader, ui, "", ui.getWidth() / 2 - (w / 2), y, w, h);
        ui.addLayer(fC);
        fC.setVisible(false);

        crossfaders[0] = fL;
        crossfaders[1] = fR;
        crossfaders[2] = fC;
    }

    public void start(SLStudioLX.UI ui) {
        this.ui = ui;

        SLStudio.applet.lx.engine.addListener(new Listener());
        addUI();
        setChannelOptions();
        for (DeckWindow w : deckWindows) {
            w.start();
        }

        aFader.addListener(parameter -> swapDecks(aFader, DeckSide.LEFT));
        bFader.addListener(parameter -> swapDecks(bFader, DeckSide.RIGHT));
        getLX().engine.crossfader.addListener(parameter -> {
            swapDecks(bFader, DeckSide.RIGHT);
            swapDecks(bFader, DeckSide.LEFT);
        });

        if (SLStudio.applet.apc40Listener.hasRemote.isOn()) {
            bindCommon(SLStudio.applet.apc40Listener.remotes[0], aFader, DeckSide.LEFT);
            bindCommon(SLStudio.applet.apc40Listener.remotes[1], bFader, DeckSide.RIGHT);
        }

        SLStudio.applet.apc40Listener.hasRemote.addListener(parameter -> {
            if (SLStudio.applet.apc40Listener.hasRemote.isOn()) {
                bindCommon(SLStudio.applet.apc40Listener.remotes[0], aFader, DeckSide.LEFT);
                bindCommon(SLStudio.applet.apc40Listener.remotes[1], bFader, DeckSide.RIGHT);
            }
        });

        aFader.setValue(0.00001);
        bFader.setValue(0.00001);
        propagateLeftFader();
        propagateRightFader();

    }

    private void updateDeckColors() {
        double crossfade = getLX().engine.crossfader.getValue();

        deckWindows[0].setBackgroundColor(computeDeckColor(aFader.getValue() < .5f, crossfade < .5f));
        deckWindows[1].setBackgroundColor(computeDeckColor(aFader.getValue() >= .5f, crossfade < .5f));

        deckWindows[2].setBackgroundColor(computeDeckColor(bFader.getValue() < .5f, crossfade >= .5f));
        deckWindows[3].setBackgroundColor(computeDeckColor(bFader.getValue() >= .5f, crossfade >= .5f));
    }

    private int computeDeckColor(boolean groupActive, boolean abActive) {
        int color = ui.theme.getDeviceBackgroundColor();
        if (abActive) color = lerpColor(color, WHITE, .2f, RGB);
        if (groupActive) color = lerpColor(color, RED, abActive ? .2f : .1f, RGB);
        return color;
    }

    void swapDecks(CompoundParameter fader, DeckSide side) {
        int newDeck = focusedDeskIndexForSide(side);
        int oldDeck = side == DeckSide.LEFT ? oldDeckL : oldDeckR;
        LXChannel.CrossfadeGroup newAB = getLX().engine.crossfader.getValue() < .5 ? LXChannel.CrossfadeGroup.A : LXChannel.CrossfadeGroup.B;
        if (newDeck != oldDeck || newAB != oldAB)
            updateDeckColors();

        if (newDeck == oldDeck && newAB == oldAB) {
            return;
        }
        oldAB = newAB;


        if (oldDeck != -1) {
            deckWindows[oldDeck].unbindDeck();
        }

        deckWindows[newDeck].bindDeck();

        if (side == DeckSide.LEFT) {
            oldDeckL = newDeck;
        } else {
            oldDeckR = newDeck;
        }
    }

    void bindCommon(LXMidiRemote remote, CompoundParameter fader, DeckSide side) {
        if (remote == null) return;
        remote.bindController(fader, 0, 15);
        remote.bindController(SLStudio.applet.lx.engine.output.brightness, 0, 14);
        LXEffect e = SLStudio.applet.lx.engine.masterChannel.getEffect("Blur");
        println("REMOTE", remote, SLStudio.applet.lx.engine.masterChannel, e);
        if (e != null) {
            remote.bindController(e.getParameter("amount"), 7, 7);
        }
        swapDecks(fader, side);
    }

    public int focusedDeskIndexForSide(DeckSide side) {
        if (side == DeckSide.LEFT) {
            return aFader.getValuef() < 0.5 ? 0 : 1;
        } else {
            return bFader.getValuef() < 0.5 ? 2 : 3;
        }
    }

    public enum DeckSide {
        LEFT {
            @Override
            public boolean isActive(final LX lx) {
                return lx.engine.crossfader.getValue() < .5;
            }
        },

        RIGHT {
            @Override
            public boolean isActive(final LX lx) {
                return lx.engine.crossfader.getValue() >= .5;
            }
        };

        public abstract boolean isActive(final LX lx);
    }
}
