import heronarts.lx.effect.*;

int CHAN_WIDTH = 200;
int CHAN_HEIGHT = 650;
int CHAN_Y = 20;
int PAD = 5;

public class ColorShiftEffect extends LXEffect {
    public BoundedParameter shift = new BoundedParameter("shift", 0, 360);

    public ColorShiftEffect(LX lx) {
        super(lx);

        addParameter(shift);
    }

    public String getLabel() {
        return "ColorShift";
    }

    public void run(double deltaMs, double enabledAmount) {
        for (LXPoint p : model.points) {
            int o = colors[p.index];
            float h = LXColor.h(o);
            float s = LXColor.s(o);
            float b = LXColor.b(o);
            colors[p.index] = LXColor.hsb(h + shift.getValuef(), s, b);
        }
    }
}


class DeckWindow extends UIWindow {
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
    LXMidiRemote remote;


    DeckWindow(DiscreteParameter sel, int slot, PerformanceManager pm, UI ui, String title, float x, float y, float w, float h) {
        super(ui, title, x, y, w, h);
        this.ui = ui;
        this.pm = pm;
        this.slot = slot;
        this.selection = sel;

        labelListener = new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                setTitle(((StringParameter)parameter).getString());
            }
        };

       
        selection.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                println("PARAMETER CHANGED", parameter.getLabel(), ((DiscreteParameter)parameter).getValuei());
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

        switch(slot) {
            case 0: pm.upL = channel.fader; break;
            case 1: pm.downL = channel.fader; break;
            case 2: pm.upR = channel.fader; break;
            case 3: pm.downR = channel.fader; break;
            default: break;
        }

        LXEffect b = channel.getEffect("Blur");
        if (b == null) {
            b = new BlurEffect(lx);
            channel.addEffect(b);
        }
        b.enabled.setValue(true);


        LXEffect cs = channel.getEffect("ColorShift");
        if (cs == null) {
            cs = new ColorShiftEffect(lx);
            channel.addEffect(cs);
        }
        cs.enabled.setValue(true);

        pm.blurs[slot] = b.getParameter("amount");
        pm.colors[slot] = cs.getParameter("shift");

        for (int i = 0; i < 4; i++) {
            println(i, pm.blurs[i], pm.colors[i]);
        }


    }

    class PatternItem extends UIItemList.AbstractItem {
        final LXPattern pattern;

        PatternItem(LXPattern pattern) {
          this.pattern = pattern;
        }

        String getLabel() {
            return pattern.getLabel();
        }

        boolean isSelected() {
            return false;
        }

        @Override
        boolean isActive() {
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
        patternList = new UIItemList.ScrollList(ui, 4, 500, w-8, 150 - 8);
        patternList.addToContainer(this);
        patternList.setSingleClickActivate(true);
    }

    void buildChannelSelector() {
        float w = getWidth();
        dropMenu = new UIDropMenu(4, 460, w-8, 20, selection);
        dropMenu.addToContainer(this);
    }

    class Listener extends LXChannel.AbstractListener {
        public Listener() {
            super();
        }

        @Override
        void patternDidChange(LXChannel channel, LXPattern pattern) {
            updateChannel(pattern);
        }

        @Override
        void patternAdded(LXChannel channel, LXPattern pattern) {
            updatePatternList();
        }

        @Override
        void patternMoved(LXChannel channel, LXPattern pattern){
            updatePatternList();
        }

        @Override
        void patternRemoved(LXChannel channel, LXPattern pattern) {
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

            LXListenableNormalizedParameter p = (LXListenableNormalizedParameter)param;

            if (param instanceof BooleanParameter) {
                if (si >= switches.size()) continue;
                switches.get(si).setParameter((BooleanParameter)p);
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

    void bindDeck() {
        this.remote = apc40Listener.remote;
        if (remote == null) {
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
                remote.sendController(0, knobCCs[i] + 8, 2);
                remote.bindController(p, 0, knobCCs[i]);
                // remote.sendController(3, knobCCs[i] + 8, 3);
            }
        }

        for (int i = 0; i < switches.size(); i++) {
            UISwitch sw = switches.get(i);
            LXParameter p = sw.getParameter();
            if (p != null) {
                remote.bindNote(p, 0, buttonNotes[i]);
            }
        }

        // for (int i = 0; i < 8; i++) {
        //     remote.sendController(1, 24 + i, 2);
        // }
    }

    void unbindDeck() {
        for (int cc : boundCCs) {
            remote.unbindController(0, cc);
            remote.sendController(0, cc + 8, 0);
        }
        for (int note : boundNotes) {
            remote.unbindNote(0, note);
            remote.sendNoteOff(0, note);
        }

    }

    void rebindDeck() {
        unbindDeck();
        bindDeck();
    }
}

class FaderWindow extends UIWindow {
    final UISlider slider;

    FaderWindow(LXListenableNormalizedParameter param, UI ui, String title, float x, float y, float w, float h) {
        super(ui, title, x, y, w, h);

        float pad = 5;
        slider = new UISlider(UISlider.Direction.HORIZONTAL, pad, pad, w - (2 * pad), h - (2 * pad));
        slider.addToContainer(this);

        slider.setParameter(param);
        
    }

    @Override
    void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    }
}

class PerformanceManager extends LXComponent {
    final CompoundParameter lFader = new CompoundParameter("lFader");
    final CompoundParameter rFader = new CompoundParameter("rFader");
    public LXListenableNormalizedParameter upL = null;
    public LXListenableNormalizedParameter downL = null;
    public LXListenableNormalizedParameter upR = null;
    public LXListenableNormalizedParameter downR = null;



    final CompoundParameter lBlur = new CompoundParameter("lBlur");
    final CompoundParameter rBlur = new CompoundParameter("rBlur");
    public LXParameter[] blurs = new LXParameter[4];

    final BoundedParameter lColor = new BoundedParameter("lColor", 0, 360);
    final BoundedParameter rColor = new BoundedParameter("rColor", 0, 360);
    public LXParameter[] colors = new LXParameter[4];


    final CompoundParameter lDummy = new CompoundParameter("lDummy");
    final CompoundParameter rDummy = new CompoundParameter("rDummy");



    DiscreteParameter deckOneChannel;
    DiscreteParameter deckTwoChannel;
    DiscreteParameter deckThreeChannel;
    DiscreteParameter deckFourChannel;

    DiscreteParameter channelSelections[] = new DiscreteParameter[4];
    public DeckWindow[] windows = new DeckWindow[4];
    public UIWindow[] crossfaders = new UIWindow[3];

    int oldDeck = -1;

    UI ui;

    class Listener implements LXEngine.Listener {
        @Override
        void channelAdded(LXEngine engine, LXChannel channel) {
            println("CHANNEL ADDED");
            setChannelOptions();
        }

        @Override
        void channelMoved(LXEngine engine, LXChannel channel) {
            setChannelOptions();
        }

        @Override
        void channelRemoved(LXEngine engine, LXChannel channel) {
            setChannelOptions();
        }
    }

    void setChannelOptions() {
        List<LXChannel> channels = lx.engine.getChannels();
        if (channels.size() == 0) return;
        String[] options = new String[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            options[i] = channels.get(i).getLabel();
        }


        for (int i = 0; i < 4; i++) {
            DiscreteParameter param = channelSelections[i];
            DeckWindow w = windows[i];
            param.setOptions(options);
            w.dropMenu.setParameter(param);
        }
    }


    public PerformanceManager(LX lx) {
        super(lx);

        lFader.setPolarity(LXParameter.Polarity.BIPOLAR);
        lFader.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                propagateLeftFader();
            }
        });

        rFader.setPolarity(LXParameter.Polarity.BIPOLAR);
        rFader.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                propagateRightFader();
            }
        });

        lBlur.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (blurs[0] != null) blurs[0].setValue(lBlur.getValuef());
                if (blurs[1] != null) blurs[1].setValue(lBlur.getValuef());
            }
        });

        rBlur.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (blurs[2] != null) blurs[2].setValue(rBlur.getValuef());
                if (blurs[3] != null) blurs[3].setValue(rBlur.getValuef());
            }
        });

        lColor.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                println("CHANGED", lColor.getValuef());
                if (colors[0] != null) colors[0].setValue(lColor.getValuef());
                if (colors[1] != null) colors[1].setValue(lColor.getValuef());
            }
        });

        rColor.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (colors[2] != null) colors[2].setValue(rColor.getValuef());
                if (colors[3] != null) colors[3].setValue(rColor.getValuef());
            }
        });

       //  lDummy.addListener(new LXParameterListener() {
       //      public void onParameterChanged(LXParameter parameter) {
       //          int mult = 10;
       //          float v = lDummy.getValuef();
       //          if (v < 0.1) {
       //              v *= mult;
       //              lColor.setValue((lColor.getValuef() + v) % 360);
       //          }
       //          if (v > 0.9) {
       //              v = mult * (1.0 - v);
       //              float raw = (lColor.getValuef() - v);
       //              float mod = raw < 0 ? 360 - raw : raw % 360;
       //              lColor.setValue(raw);
       //          }
       //      }
       //  });

       // rDummy.addListener(new LXParameterListener() {
       //      public void onParameterChanged(LXParameter parameter) {
       //          int mult = 10;
       //          float v = rDummy.getValuef();
       //          if (v < 0.1) {
       //              v *= mult;
       //              rColor.setValue((rColor.getValuef() + v) % 360);
       //          }
       //          if (v > 0.9) {
       //              v = mult * (1.0 - v);
       //              float raw = (rColor.getValuef() - v);
       //              float mod = raw < 0 ? 360 - raw : raw % 360;
       //              rColor.setValue(raw);
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
        float v = lFader.getValuef();
        upL.setValue(1.0 - v);
        downL.setValue(v);
    }

    public void propagateRightFader() {
        float v = rFader.getValuef();
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
            windows[i] = w;
        }

        float w = (2 * CHAN_WIDTH + PAD)/2;
        float h = 50.0;
        float y = CHAN_Y + CHAN_HEIGHT;

        FaderWindow fL = new FaderWindow(lFader, ui, "", CHAN_WIDTH/2, y, w, h);
        ui.addLayer(fL);
        fL.setVisible(false);

        FaderWindow fR = new FaderWindow(rFader, ui, "",  ui.getWidth() - CHAN_WIDTH - (w/2), y, w, h);
        ui.addLayer(fR);
        fR.setVisible(false);

        FaderWindow fC = new FaderWindow(lx.engine.crossfader, ui, "", ui.getWidth()/2 - (w/2), y, w, h);
        ui.addLayer(fC);
        fC.setVisible(false);

        crossfaders[0] = fL;
        crossfaders[1] = fR;
        crossfaders[2] = fC;
    }

    void start(UI ui) {
        this.ui = ui;
        lx.engine.addListener(new Listener());
        addUI();
        setChannelOptions();
        for (DeckWindow w : windows) {
            w.start();
        }

        

        lFader.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                swapDecks();
            }
        });

        if (apc40Listener.hasRemote.isOn()) {
            bindCommon(apc40Listener.remote);
        }
        apc40Listener.hasRemote.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (apc40Listener.hasRemote.isOn()) {
                    bindCommon(apc40Listener.remote);
                }
            }
        });

        lFader.setValue(0.00001);
        rFader.setValue(0.00001);
        propagateLeftFader();
        propagateRightFader();

    }

    void swapDecks() {
        int newDeck = lFader.getValuef() < 0.5 ? 0 : 1;
        if (newDeck == oldDeck) {
            return;
        }
        windows[newDeck].setBackgroundColor(ui.theme.getDeviceFocusedBackgroundColor());
        if (oldDeck != -1) {
            windows[oldDeck].unbindDeck();
            windows[oldDeck].setBackgroundColor(ui.theme.getDeviceBackgroundColor());
        }
        if (apc40Listener.remote != null) {
            windows[newDeck].bindDeck();
        }
        oldDeck = newDeck;
    }

    void bindCommon(LXMidiRemote remote) {
        remote.bindController(lFader, 0, 15);
        remote.bindController(lx.engine.output.brightness, 0, 14);
        remote.bindController(lx.engine.masterChannel.getEffect("Blur").getParameter("amount"), 7, 7);
        swapDecks();
    }

    int getWindowIndex(int side) {
        if (side == 0) {
            return lFader.getValuef() < 0.5 ? 0 : 1;
        } else {
            return rFader.getValuef() < 0.5 ? 2 : 3;
        }
    }

}


        