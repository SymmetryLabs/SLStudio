package com.symmetrylabs.slstudio.performance;

import heronarts.lx.LXComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.blend.LXBlend;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.*;

import static processing.core.PApplet.print;
import static processing.core.PApplet.println;

import com.symmetrylabs.slstudio.*;

import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class PerformanceGUIController extends LXComponent {

    final UI ui;
    final LX lx;
    final PerformanceManager pm;
    final ChannelWindow[] channelWindows;
    final FaderWindow[] faderWindows;

    public PerformanceGUIController(LX lx, UI ui, PerformanceManager pm) {
        super(lx);
        this.ui = ui;
        this.lx = lx;
        this.pm = pm;

        channelWindows = new ChannelWindow[PerformanceManager.N_CHANNELS];
        faderWindows = new FaderWindow[3];
    }

    static int CHANNEL_WIDTH = 175;

    private Rectangle getWindowCoordinates(int i, boolean sidebarsVisible) {
        int channelSpacing = 20;
        int w = CHANNEL_WIDTH;
        int h = 620;
        int xMargin = 10;
        int yMargin = 30;

        Rectangle rect = new Rectangle();
        rect.height = h;
        rect.width = w;

        int deckIndex = i / 2;
        int chanIndex = i % 2;
        if (deckIndex == 0) {
            rect.x = xMargin + chanIndex * (w + channelSpacing);
            if (sidebarsVisible) {
                rect.x += ((SLStudioLX.UI) ui).leftPane.getWidth();
            }
        } else {
            rect.x = ui.applet.width - w - (w + channelSpacing) * (1 - chanIndex) - xMargin;
            if (sidebarsVisible) {
                rect.x -= ((SLStudioLX.UI) ui).rightPane.getWidth();
            }
        }
        rect.y = yMargin;
        //        rect.y = ui.applet.height - h - yMargin;

        return rect;
    }

    private Rectangle getFaderCoordinates(int index, boolean sidebarsVisible) {
        float w = CHANNEL_WIDTH * 1.7f;
        float pad = CHANNEL_WIDTH + 10 - w / 2;
        Rectangle rect;
        if (index == 0) {
            rect = getWindowCoordinates(0, sidebarsVisible);
            rect.x += pad;
        } else if (index == 2) {
            rect = getWindowCoordinates(2, sidebarsVisible);
            rect.x += pad;
        } else {
            rect = getWindowCoordinates(0, sidebarsVisible);
            rect.x = (int) (ui.applet.width / 2 - (w / 2));
        }
        rect.width = (int) w;
        rect.y += rect.height + 10;
        rect.height = 73;
        return rect;
    }

    public void createGlobalWindow(PerformanceManager pm) {
        Rectangle rect = getFaderCoordinates(0, false);
        int x = ui.applet.width / 2 - CHANNEL_WIDTH / 2;
        GlobalWindow window = new GlobalWindow(pm, ui, x, rect.y + rect.height + 5, CHANNEL_WIDTH, 210);
        ui.addLayer(window);
    }

    public void createFaderWindow(
            LXListenableNormalizedParameter param, ObjectParameter<LXBlend> blendParam, int index) {
        Rectangle rect = getFaderCoordinates(index, false);

        FaderWindow window =
                new FaderWindow(param, blendParam, ui, "HI", rect.x, rect.y, rect.width, rect.height);
        ui.addLayer(window);
        faderWindows[index] = window;
    }

    public void createChannelWindow(PerformanceManager.PerformanceChannel channel) {
        Rectangle rect = getWindowCoordinates(channel.globalIndex, false);

        ChannelWindow window =
                new ChannelWindow(lx, ui, channel, rect.x, rect.y, rect.width, rect.height);
        ui.addLayer(window);
        //        window.setVisible(false);
        channelWindows[channel.globalIndex] = window;

        // RAPHTODO
        //        new java.util.Timer().schedule(
        //            new java.util.TimerTask() {
        //                @Override
        //                public void run() {
        //                    Rectangle rect = getWindowCoordinates(channel.globalIndex, false);
        //                    window.setPosition(rect.x, rect.y);
        //                    window.setVisible(true);
        //                }
        //            },
        //            2000
        //        );
    }

    public void moveWindows(boolean sidebarsVisible) {
        for (int i = 0; i < channelWindows.length; i++) {
            Rectangle rect = getWindowCoordinates(i, sidebarsVisible);
            channelWindows[i].setPosition(rect.x, rect.y);
        }
        for (int i = 0; i < faderWindows.length; i++) {
            Rectangle rect = getFaderCoordinates(i, sidebarsVisible);
            faderWindows[i].setPosition(rect.x, rect.y);
        }
    }

    public void updateAllPatternLists() {
        for (ChannelWindow w : channelWindows) {
            w.updatePatternList();
        }
    }

    public void updateAllPresetUIs() {
        for (ChannelWindow w : channelWindows) {
            w.updatePresetUI();
        }
    }

    public class ChannelWindow extends UIWindow {
        final ArrayList<OverrideLabeledKnob> knobs = new ArrayList();
        final ArrayList<UISwitch> switches = new ArrayList();
        final PerformanceManager pm;
        final PerformanceManager.PerformanceChannel channel;
        final PerformanceManager.PerformanceDeck deck;
        final DiscreteParameter selectedPreset;

        UIButtonGroup presetGroup;
        UIButton fireButton;
        UIButton saveButton;

        public BooleanParameter firePushed;
        public BooleanParameter savePushed;

        public DiscreteParameter activePatternIndex;



        PatternList patternList = null;

        //        final Listener listener = new Listener();

        private final LX lx;
        private final UI ui;

        ChannelWindow(
                LX lx,
                UI ui,
                PerformanceManager.PerformanceChannel channel,
                float x,
                float y,
                float w,
                float h) {

            super(ui, "", x, y, w, h);
            this.ui = ui;
            this.lx = lx;
            this.pm = channel.manager;
            this.channel = channel;
            deck = channel.deck;

            selectedPreset = new DiscreteParameter("selectedPreset", 0, 0, PerformanceManager.Preset.MAX_PRESETS);

            activePatternIndex = new DiscreteParameter("activePatternIndex", 0, 0, 1);



            setTitle(String.format("Channel %d", channel.globalIndex + 1));

            //
            //
            //
            //            selection.addListener(new LXParameterListener() {
            //                public void onParameterChanged(LXParameter parameter) {
            //                    println("PARAMETER CHANGED", parameter.getLabel(), ((DiscreteParameter)
            // parameter).getValuei());
            //                    setSelectedChannel();
            //                }
            //            });

            float afterPatterns = buildPatternList(30);
            float afterPresets = buildPresetSelector(afterPatterns + 10);
            float afterKnobs = buildKnobs(afterPresets + 15);
            float afterSwitches = buildSwitches(afterKnobs + 30);

            refreshPattern();
            updatePatternList();
            setActivePatternIndex();

            deck.activeChannel.addListener(
                    new LXParameterListener() {
                        @Override
                        public void onParameterChanged(LXParameter lxParameter) {
                            setBackground();
                        }
                    });

            lx.engine.crossfader.addListener(
                    new LXParameterListener() {
                        @Override
                        public void onParameterChanged(LXParameter lxParameter) {
                            setBackground();
                        }
                    });

            setBackground();

            activePatternIndex.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    int i = activePatternIndex.getValuei();
//                    System.out.println(deckI);
                    patternList.getItems().get(i).onActivate();
                }
            });

        }

        private void setBackground() {
            boolean channelActive = deck.activeChannel.getValuei() == channel.indexInDeck;
            int focusedDeck = pm.globalParams.crossfade.getValuef() < 0.5 ? 0 : 1;
            boolean deckActive = deck.globalIndex == focusedDeck;
            setBackgroundColor(computeDeckColor(channelActive, deckActive));
        }

        private int computeDeckColor(boolean channelActive, boolean deckActive) {
            int color = ui.theme.getDeviceBackgroundColor();
            if (deckActive) color = LXColor.lerp(color, LXColor.WHITE, .2f);
            if (channelActive) color = LXColor.lerp(color, LXColor.RED, deckActive ? .2f : .1f);
            return color;
        }

        class PatternList extends UIItemList.ScrollList {


            public PatternList(UI ui, float x, float y, float w, float h) {
                super(ui, x, y, w, h);

            }


            @Override
            public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                super.onKeyPressed(keyEvent, keyChar, keyCode);

                if (keyChar == 'h') {
                    PatternItem item = (PatternItem) getFocusedItem();
                    item.window.pm.toggleHidden(item.pattern);
                    if (item.isHidden()) {
                        int afterI = Math.min(getItems().size() - 1, item.index);
                        PatternItem after = (PatternItem) (getItems().get(afterI));
                        after.onActivate();
                        pm.updateAllTwisters();
                    }
                }
            }
        }

        class PatternItem extends UIItemList.AbstractItem {
            final LXPattern pattern;
            final PerformanceManager.PerformanceChannel channel;
            final ChannelWindow window;
            public int index = 0;

            PatternItem(LXPattern pattern, ChannelWindow window) {
                this.pattern = pattern;
                this.window = window;
                this.channel = window.channel;
            }

            public void setIndex(int i) {
                index = i;
            }

            public String getLabel() {
                return pattern.getLabel();
            }

            boolean isSelected() {
                return false;
            }

            private boolean isHidden() {
                return channel.manager.isHidden(pattern);
            }

            @Override
            public boolean isActive() {
                if (isHidden()) {
                    return true;
                }
                return pattern == channel.channel.getActivePattern();
            }

            @Override
            public int getActiveColor(UI ui) {
                return isHidden() ? LXColor.hsb(0, 100, 20) : ui.theme.getPrimaryColor();
            }

            @Override
            public void onActivate() {
                channel.channel.goPattern(pattern);
                patternList.setFocusIndex(index);
                activePatternIndex.setValue(index);
                window.refreshPattern();
            }
        }

        PerformanceManager.Preset getActivePreset() {
            return pm.presets.get(selectedPreset.getValuei());
        }

        void savePreset() {
            PerformanceManager.Preset preset = getActivePreset();
            preset.loadFrom(channel.channel);
            updateAllPresetUIs();
        }

        void firePreset() {
            PerformanceManager.Preset preset = getActivePreset();
            LXPattern pattern = preset.applyTo(channel.channel);
            List<? extends UIItemList.Item> items = patternList.getItems();
            for (int i = 0; i < items.size(); i++) {
                PatternItem pi = (PatternItem)items.get(i);
                if (pi.pattern == pattern) {
                    pi.onActivate();
                    patternList.setFocusIndex(i);
                    break;
                }
            }
        }

        float buildPresetSelector(float yStart) {
            float pad = 5;
            float w = getWidth();
            int h = 20;
            int n = PerformanceManager.Preset.MAX_PRESETS;
//        final UIDropMenu menu = new UIDropMenu(pad, yStart, w - (pad *2), h, selectedPreset);
//            menu.addToContainer(this);

            final ChannelWindow window = this;

            String[] options = new String[n];
            for (int i = 0; i < n; i++) {
                String name = pm.presets.get(i).name.substring(0, 1);
                options[i] = options[i] = name;
            }
            selectedPreset.setOptions(options);

            presetGroup = new UIButtonGroup(selectedPreset, pad, yStart, w - (pad *2), h);
            presetGroup.addToContainer(window);

            firePushed = new BooleanParameter("firePushed", false);
            savePushed = new BooleanParameter("savePushed", false);

            float fireStart = yStart + h + 5;
            float bWidth = (w - pad * 3) / 2;
            saveButton = new UIButton(pad, fireStart, bWidth, h);
//            saveButton = new UIButton(pad, fireStart, bWidth, h) {
//                public void onToggle(boolean on) {
//                    if (!on) {
//                        return;
//                    }
//                    savePreset();
//                }
//            };
            saveButton.setLabel("Save");
            saveButton.setMomentary(true);
            saveButton.setParameter(savePushed);
            saveButton.addToContainer(this);

            fireButton = new UIButton(bWidth + 2 * pad, fireStart, bWidth, h);
//            fireButton = new UIButton(bWidth + 2 * pad, fireStart, bWidth, h) {
//                public void onToggle(boolean on) {
//                    if (!on) {
//                        return;
//                    }
//                    firePreset();
//                }
//            };
            fireButton.setLabel("Fire");
            fireButton.setMomentary(true);
            fireButton.setParameter(firePushed);

            fireButton.addToContainer(this);

            selectedPreset.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    updatePresetUI();
                }
            });

            firePushed.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    if (firePushed.isOn()) {
                        firePreset();
                        firePushed.setValue(false);
                    }
                }
            });

            savePushed.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    if (savePushed.isOn()) {
                        savePreset();
                        savePushed.setValue(false);
                    }
                }
            });

            updatePresetUI();

            lx.engine.addLoopTask(new LXLoopTask() {
                @Override
                public void loop(double v) {
                    int i = selectedPreset.getValuei();
                    if (!presetGroup.buttons[i].isActive()) {
                        presetGroup.buttons[i].setActive(true);
                    }
                }
            });





//            pm.presetsLoaded.addListener(new LXParameterListener() {
//                @Override
//                public void onParameterChanged(LXParameter lxParameter) {
//                    if (pm.presetsLoaded.isOn()) {
//                        String[] options = new String[n];
//                        for (int deckI = 0; deckI < n; deckI++) {
//                            String name = pm.presets.get(deckI).name.substring(0, 1);
//                            options[deckI] = options[deckI] = name;
//                        }
//                        selectedPreset.setOptions(options);
//                        final UIButtonGroup group = new UIButtonGroup(selectedPreset, pad, yStart, w - (pad *2), h);
//                        group.addToContainer(window);
//                        redraw();
//                    }
//                }
//            });

            return fireStart + h;
        }

        void updatePresetUI() {
            PerformanceManager.Preset p = getActivePreset();
            fireButton.setEnabled(p.patternName != null);

            for (int i = 0; i < pm.presets.size(); i++) {
                PerformanceManager.Preset preset = pm.presets.get(i);
                float hue = preset.hue;
                boolean saved = preset.patternName != null;
                UIButton b = presetGroup.buttons[i];
                b.setActiveColor(LXColor.hsb(hue, 100, 70));
                b.setInactiveColor(LXColor.hsb(hue, saved ? 100 : 0, 30));
                b.setFontColor(LXColor.gray(50));
            }

        }

        float buildKnobs(float yStart) {
            for (UIKnob knob : knobs) {
                knob.removeFromContainer();
            }
            knobs.clear();

            float xOffset = ((getWidth() - (UIKnob.WIDTH * 4)) / 5);
            float yOffset = xOffset + 20;

            float kx = 0;
            float ky = 0;
            for (int i = 0; i < 16; i++) {
                int xi = i % 4;
                int yi = i / 4;
                kx = xOffset + xi * (xOffset + UIKnob.WIDTH);
                ky = yStart + yi * (yOffset + UIKnob.HEIGHT);
                OverrideLabeledKnob knob = new OverrideLabeledKnob(kx, ky, UIKnob.WIDTH, UIKnob.HEIGHT);
                knob.addToContainer(this);
                knobs.add(knob);
            }

            return ky + UIKnob.HEIGHT;
        }

        float buildSwitches(float yStart) {
            for (UISwitch sw : switches) {
                sw.removeFromContainer();
            }
            switches.clear();

            float xOffset = ((getWidth() - (UISwitch.WIDTH * 4)) / 5);
            float yOffset = xOffset + 20;

            float sx = 0;
            float sy = 0;
            for (int i = 0; i < 8; i++) {
                int xi = i % 4;
                int yi = i / 4;
                sx = xOffset + xi * (xOffset + UISwitch.WIDTH);
                sy = yStart + yi * (yOffset + UISwitch.WIDTH);
                UISwitch sw = new UISwitch(sx, sy);
                sw.addToContainer(this);
                switches.add(sw);
            }

            return sy + UISwitch.WIDTH;
        }

        float buildPatternList(float yStart) {
            float w = getWidth();
            int h = 142;
            patternList = new PatternList(ui, 4, yStart, w - 8, h);
            patternList.addToContainer(this);
            patternList.setSingleClickActivate(true);

            return yStart + h;
        }

        void updatePatternList() {
            final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
            ArrayList<LXPattern> visible = new ArrayList<LXPattern>();
            ArrayList<LXPattern> hidden = new ArrayList<LXPattern>();

            for (LXPattern pattern : channel.channel.getPatterns()) {
                if (channel.manager.isHidden(pattern)) {
                    hidden.add(pattern);
                } else {
                    visible.add(pattern);
                }
            }

            Comparator<LXPattern> labelSort =
                    new Comparator<LXPattern>() {
                        @Override
                        public int compare(LXPattern o1, LXPattern o2) {
                            return o1.getLabel().compareTo(o2.getLabel());
                        }
                    };

            visible.sort(labelSort);
            hidden.sort(labelSort);

            for (LXPattern pattern : visible) {
                items.add(new PatternItem(pattern, this));
            }
            for (LXPattern pattern : hidden) {
                items.add(new PatternItem(pattern, this));
            }

            activePatternIndex.setRange(0, items.size());

            for (int i = 0; i < items.size(); i++) {
                ((PatternItem)items.get(i)).setIndex(i);
            }

            patternList.setItems(items);
        }

        void setActivePatternIndex() {
            List<? extends UIItemList.Item> items = patternList.getItems();
            for (int i = 0; i < items.size(); i++) {
                PatternItem pi = (PatternItem)items.get(i);
                if (pi.isActive()) {
                    patternList.setFocusIndex(i);
                    activePatternIndex.setValue(i);
                    break;
                }
            }
        }

        void refreshPattern() {

            LXPattern pattern = channel.channel.getActivePattern();

//            setActivePatternIndex();


            for (UIKnob knob : knobs) {
                knob.setParameter(null);
                knob.setEnabled(true);
            }
            for (UISwitch sw : switches) {
                sw.setParameter(new BooleanParameter("-"));
                sw.setEnabled(false);
            }

            int ki = 0;
            int si = 0;
            int PARAM_KNOBS = 12;

            ArrayList<BooleanParameter> buttonParams = channel.getButtonParameters();
            ArrayList<LXListenableNormalizedParameter> knobParams = channel.getKnobParameters();
            ArrayList<LXListenableNormalizedParameter> fxParams = channel.getEffectParameters();
            ArrayList<String> fxLabels = channel.getEffectLabels();


            for (int i = 0; i < buttonParams.size(); i++) {
                switches.get(i).setParameter(buttonParams.get(i));
                switches.get(i).setEnabled(true);
            }
            for (int i = 0; i < knobParams.size(); i++) {
                knobs.get(i).setParameter(knobParams.get(i));
                knobs.get(i).setEnabled(true);
            }

            redraw();



            for (int i = 0; i < fxParams.size(); i++) {
                OverrideLabeledKnob knob = knobs.get(PARAM_KNOBS + i);
                LXListenableNormalizedParameter param = fxParams.get(i);
                String label = fxLabels.get(i);
                knob.setParameter(param);
                knob.setOverrideLabel(label);
            }

        }
    }

    class OverrideLabeledKnob extends UIKnob {
        private String overrideLabel = null;

        OverrideLabeledKnob(float x, float y, float w, float h) {
            super(x, y, w, h);
        }

        @Override
        protected String getLabelString() {
            if (getParameter() != null && overrideLabel != null) {
                return overrideLabel;
            } else {
                return super.getLabelString();
            }
        }

        public void setOverrideLabel(String label) {
            overrideLabel = label;
        }
    }

    class OverrideLabelSlider extends UISlider {
        private String overrideLabel = null;

        OverrideLabelSlider(UISlider.Direction direction, float x, float y, float w, float h) {
            super(direction, x, y, w, h);
        }

        @Override
        protected String getLabelString() {
            if (getParameter() != null && overrideLabel != null) {
                return overrideLabel;
            } else {
                return super.getLabelString();
            }
        }

        public void setOverrideLabel(String label) {
            overrideLabel = label;
        }
    }

    class GlobalWindow extends UIWindow {
        UIToggleSet cueToggle;

//    final UIButton[] cueButtons;
        final UIButton liveButton;
        final UIButton sidebarToggle;
        final UISlider globalSliders[];
        final PerformanceManager pm;

        GlobalWindow(PerformanceManager pm, UI ui, float x, float y, float w, float h) {
            super(ui, "", x, y, w, h);

            this.pm = pm;

            int nQ = 7;
//      cueButtons = new UIButton[nQ];

            int toggleY = 35;
            int togglePad = 5;
            float toggleWidth = (w - (togglePad * 3)) / 2;
            liveButton = new UIButton(togglePad, toggleY, toggleWidth, 15);
            liveButton.setLabel("Live");
            liveButton.setParameter(lx.engine.output.enabled);
            liveButton.addToContainer(this);

            sidebarToggle =
                    new UIButton(toggleWidth + (togglePad * 2), toggleY, toggleWidth, 15) {
                        public void onToggle(boolean on) {
                            if (!on) {
                                return;
                            }
                            SLStudioLX.UI slUI = (SLStudioLX.UI) ui;
                            slUI.toggleSidebars();
                            pm.gui.moveWindows(slUI.areSidebarsVisible());
                        }
                    };
            sidebarToggle.setMomentary(true);
            sidebarToggle.setLabel("Toggle Sidebars");
            sidebarToggle.addToContainer(this);

            int nSliders = 4;
            globalSliders = new UISlider[nSliders];
            LXListenableNormalizedParameter[] params =
                    new LXListenableNormalizedParameter[] {
                        pm.globalParams.brightness,
                        pm.globalParams.speed,
                        pm.globalParams.effectParams.desaturation,
                        pm.globalParams.effectParams.blur
                    };
            String[] labels = new String[] {"Brightness", "Speed", "Desat", "Blur"};

            for (int i = 0; i < nSliders; i++) {
                //                float sw = w - togglePad*2;
                //                float sh = 20;
                //                float sx = togglePad;
                //                float sy = 50 + deckI * (sh + 15);
                float sw = (w - (togglePad * (nSliders + 1))) / nSliders;
                float sh = 125;
                float sy = 66;

                float sx = (togglePad * (i + 1)) + (sw * i);

                OverrideLabelSlider slider =
                        new OverrideLabelSlider(UISlider.Direction.VERTICAL, sx, sy, sw, sh);
                slider.setOverrideLabel(labels[i]);
                slider.setParameter(params[i]);
                slider.addToContainer(this);
                globalSliders[i] = slider;
            }

            buildCues();
        }

        void buildCues() {
            float w = getWidth();
            float cueH = 20;
            float cueY = 5;
            float pad = 5;

            cueToggle = new UIToggleSet(pad, cueY, w - (pad * 2), cueH);
            cueToggle.setParameter(pm.cueState);
            cueToggle.setEvenSpacing();

            cueToggle.addToContainer(this);

        }

//    void buildCues() {
//      float w = getWidth();
//      float h = getHeight();
//      int nQ = cueButtons.length;
//
//      String[] labels = new String[] {"1", "L", "2", "All", "3", "R", "4"};
//
//      float queueWidth = 20;
//      float cueSpacing = (w - (nQ * queueWidth)) / (nQ + 1);
//      for (int deckI = 0; deckI < nQ; deckI++) {
//        float qx = (queueWidth * deckI) + (cueSpacing * (deckI + 1));
//        float qy = 5; // h - queueWidth - 3;
//        final int j = deckI;
//        UIButton q =
//            new UIButton(qx, qy, queueWidth, queueWidth) {
//              public void onToggle(boolean on) {
//                if (!on) {
//                  return;
//                }
//                pm.cueState.setValue(j);
//              }
//            };
//        q.setLabel(labels[deckI]);
//        cueButtons[deckI] = q;
//        q.addToContainer(this);
//      }
//
//      pm.cueState.addListener(
//          new LXParameterListener() {
//            @Override
//            public void onParameterChanged(LXParameter lxParameter) {
//              updateCues();
//            }
//          });
//      updateCues();
//    }
//
//    void updateCues() {
//      int c = pm.cueState.getValuei();
//      for (int deckI = 0; deckI < cueButtons.length; deckI++) {
//        cueButtons[deckI].setActive(deckI == c);
//      }
//    }
    }

    class FaderWindow extends UIWindow {
        final UISlider slider;
        final UIToggleSet blendToggle;

        FaderWindow(
                LXListenableNormalizedParameter param,
                ObjectParameter<LXBlend> blendParam,
                UI ui,
                String title,
                float x,
                float y,
                float w,
                float h) {
            super(ui, title, x, y, w, h);

            float pad = 5;
            float sw = w - (2 * pad);
            float sh = 30;
            slider = new UISlider(UISlider.Direction.HORIZONTAL, pad, pad, sw, sh);
            slider.setShowLabel(false);
            slider.addToContainer(this);
            slider.setParameter(param);

            //            blendMenu = new UIDropMenu(pad, h - (2 * pad) - 20, w - (2 * pad), 20, blendParam);
            blendToggle = new UIToggleSet(pad, pad + sh + 10, sw, 20);
            blendToggle.setParameter(blendParam);
            blendToggle.setEvenSpacing();
            blendToggle.addToContainer(this);
        }

        @Override
        public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {}
    }
}
