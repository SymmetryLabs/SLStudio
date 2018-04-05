package com.symmetrylabs.slstudio.performance;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.effect.BlurEffect;
import heronarts.lx.midi.remote.LXMidiRemote;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIDropMenu;
import heronarts.p3lx.ui.component.UIItemList;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UISwitch;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.effect.ColorShiftEffect;

import static processing.core.PApplet.println;


public class DeckWindow extends UIWindow {
    final ArrayList<UIKnob> knobs = new ArrayList();
    final ArrayList<UISwitch> switches = new ArrayList();
    UIItemList.ScrollList patternList = null;
    UIDropMenu dropMenu;

    LXPattern activePattern = null;
    LXChannel channel = null;

    final UI ui;
    final PerformanceManager pm;

    public int slot = -1;

    LXParameterListener labelListener;
    final Listener listener = new Listener();

    final DiscreteParameter selection;

    int[] boundCCs = new int[0];
    int[] boundNotes = new int[0];

    private final LX lx;

    DeckWindow(LX lx, DiscreteParameter sel, int slot, PerformanceManager pm, UI ui,
            String title, float x, float y, float w, float h) {

        super(ui, title, x, y, w, h);

        this.lx = lx;
        this.ui = ui;
        this.pm = pm;
        this.slot = slot;
        this.selection = sel;

        labelListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                setTitle(((StringParameter) parameter).getString());
            }
        };


        selection.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                println("PARAMETER CHANGED", parameter.getLabel(), ((DiscreteParameter) parameter).getValuei());
                setSelectedChannel();
            }
        });


        buildKnobs();
        buildSwitches();
        buildPatternList();
        buildChannelSelector();

    }

    public void start() {
        setSelectedChannel();
        setupChannel(channel);
    }

    void setSelectedChannel() {
        int i = selection.getValuei();
        List<LXChannel> channels = lx.engine.getChannels();
        if (i >= channels.size()) {
            setupChannel(null);
        } else {
            setupChannel(channels.get(i));
        }
    }

    void setupChannel(LXChannel newChannel) {
        if (channel != null) {
            channel.label.removeListener(labelListener);
            channel.removeListener(listener);
        }
        if (newChannel == null) {
            return;
        }
        channel = newChannel;
        channel.label.addListener(labelListener);
        channel.addListener(listener);
        setTitle(channel.getLabel());

        LXPattern pattern = channel.getActivePattern();

        updateChannel(pattern);
        updatePatternList();

        switch (slot) {
            case 0:
                pm.upL = channel.fader;
                break;
            case 1:
                pm.downL = channel.fader;
                break;
            case 2:
                pm.upR = channel.fader;
                break;
            case 3:
                pm.downR = channel.fader;
                break;
            default:
                break;
        }

        // LXEffect b = channel.getEffect("Blur");
        // if (b == null) {
        //     b = new BlurEffect(lx);
        //     channel.addEffect(b);
        // }
        // b.enabled.setValue(true);


        // LXEffect cs = channel.getEffect("ColorShift");
        // if (cs == null) {
        //     cs = new ColorShiftEffect(lx);
        //     channel.addEffect(cs);
        // }
        // cs.enabled.setValue(true);

        // pm.blurs[slot] = b.getParameter("amount");
        // pm.colors[slot] = cs.getParameter("shift");

        // for (int i = 0; i < 4; i++) {
        //     println(i, pm.blurs[i], pm.colors[i]);
        // }


    }

    class PatternItem extends UIItemList.AbstractItem {
        final LXPattern pattern;

        PatternItem(LXPattern pattern) {
            this.pattern = pattern;
        }

        public String getLabel() {
            return pattern.getLabel();
        }

        boolean isSelected() {
            return false;
        }

        @Override
        public boolean isActive() {
            return pattern == activePattern;
        }

        @Override
        public int getActiveColor(UI ui) {
            return ui.theme.getPrimaryColor();
        }

        @Override
        public void onActivate() {
            channel.goPattern(pattern);
        }
    }

    void buildKnobs() {
        for (UIKnob knob : knobs) {
            knob.removeFromContainer();
        }
        knobs.clear();

        float xOffset = ((getWidth() - (UIKnob.WIDTH * 4)) / 5);
        float yOffset = xOffset + 20;
        float yStart = 30;

        for (int i = 0; i < 16; i++) {
            int xi = i % 4;
            int yi = i / 4;
            UIKnob knob = new UIKnob(
                xOffset + xi * (xOffset + UIKnob.WIDTH),
                yStart + yi * (yOffset + UIKnob.HEIGHT),
                UIKnob.WIDTH,
                UIKnob.HEIGHT
            );
            knob.addToContainer(this);
            knobs.add(knob);
        }
    }

    void buildSwitches() {
        for (UISwitch sw : switches) {
            sw.removeFromContainer();
        }
        switches.clear();

        float xOffset = ((getWidth() - (UISwitch.WIDTH * 4)) / 5);
        float yOffset = xOffset + 20;
        float yStart = 330;

        for (int i = 0; i < 8; i++) {
            int xi = i % 4;
            int yi = i / 4;
            UISwitch sw = new UISwitch(
                xOffset + xi * (xOffset + UISwitch.WIDTH),
                yStart + yi * (yOffset + UISwitch.WIDTH)
            );
            sw.addToContainer(this);
            switches.add(sw);
        }
    }

    void buildPatternList() {
        float w = getWidth();
        patternList = new UIItemList.ScrollList(ui, 4, 500, w - 8, 150 - 8);
        patternList.addToContainer(this);
        patternList.setSingleClickActivate(true);
    }

    void buildChannelSelector() {
        float w = getWidth();
        dropMenu = new UIDropMenu(4, 460, w - 8, 20, selection);
        dropMenu.addToContainer(this);
    }

    class Listener extends LXChannel.AbstractListener {
        public Listener() {
            super();
        }

        @Override
        public void patternDidChange(LXChannel channel, LXPattern pattern) {
            updateChannel(pattern);
        }

        @Override
        public void patternAdded(LXChannel channel, LXPattern pattern) {
            updatePatternList();
        }

        @Override
        public void patternMoved(LXChannel channel, LXPattern pattern) {
            updatePatternList();
        }

        @Override
        public void patternRemoved(LXChannel channel, LXPattern pattern) {
            updatePatternList();
        }
    }

    void updatePatternList() {
        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (LXPattern pattern : channel.getPatterns()) {
            items.add(new PatternItem(pattern));
        }
        patternList.setItems(items);
    }

    void updateChannel(LXPattern pattern) {
        activePattern = pattern;

        buildKnobs();
        buildSwitches();

        int ki = 0;
        int si = 0;

        for (LXParameter param : pattern.getParameters()) {
            if (!(param instanceof LXListenableNormalizedParameter)) continue;

            LXListenableNormalizedParameter p = (LXListenableNormalizedParameter) param;

            if (param instanceof BooleanParameter) {
                if (si >= switches.size()) continue;
                switches.get(si).setParameter((BooleanParameter) p);
                si++;

            } else {
                if (ki >= knobs.size()) continue;
                knobs.get(ki).setParameter(p);
                ki++;
            }

        }
        rebindDeck();
        redraw();
    }

    public void bindDeck() {
        if (getRemote() == null) {
            return;
        }

        final int[] knobCCs = new int[16];
        int j = 0;
        for (int i = 16; i < 24; i++) {
            knobCCs[j++] = i;
        }
        for (int i = 48; i < 56; i++) {
            knobCCs[j++] = i;
        }
        final int[] buttonNotes = {58, 59, 60, 61, 62, 63, 64, 65};

        boundCCs = knobCCs;
        boundNotes = buttonNotes;

        for (int i = 0; i < knobs.size(); i++) {
            UIKnob knob = knobs.get(i);
            LXParameter p = knob.getParameter();
            if (p != null) {
                getRemote().sendController(0, knobCCs[i] + 8, 2);
                getRemote().bindController(p, 0, knobCCs[i]);
                // getRemote().sendController(3, knobCCs[i] + 8, 3);
            }
        }

        for (int i = 0; i < switches.size(); i++) {
            UISwitch sw = switches.get(i);
            LXParameter p = sw.getParameter();
            if (p != null) {
                getRemote().bindNote(p, 0, buttonNotes[i]);
            }
        }

        // for (int i = 0; i < 8; i++) {
        //     getRemote().sendController(1, 24 + i, 2);
        // }
    }

    void unbindDeck() {
        for (int cc : boundCCs) {
            getRemote().unbindController(0, cc);
            getRemote().sendController(0, cc + 8, 0);
        }
        for (int note : boundNotes) {
            getRemote().unbindNote(0, note);
            getRemote().sendNoteOff(0, note);
        }

    }

    public void rebindDeck() {
        unbindDeck();
        bindDeck();
    }

    int getSide() {
        return slot < 2 ? 0 : 1;
    }

    LXMidiRemote getRemote() {
        return SLStudio.applet.apc40Listener.remotes[getSide()];
    }
}
