package com.symmetrylabs.slstudio.performance;

import heronarts.lx.LXComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.*;


import static processing.core.PApplet.print;
import static processing.core.PApplet.println;

import com.symmetrylabs.slstudio.*;

import processing.event.MouseEvent;

public class PerformanceGUIController extends LXComponent{

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
        int h = 560;
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
                rect.x += ((SLStudioLX.UI)ui).leftPane.getWidth();
            }
        } else {
            rect.x = ui.applet.width - w - (w + channelSpacing) * (1 - chanIndex) - xMargin;
            if (sidebarsVisible) {
                rect.x -= ((SLStudioLX.UI)ui).rightPane.getWidth();
            }
        }
        rect.y = yMargin;
//        rect.y = ui.applet.height - h - yMargin;

        return rect;
    }

    private Rectangle getFaderCoordinates(int index, boolean sidebarsVisible) {
        int pad = CHANNEL_WIDTH/2 + 10;
        Rectangle rect;
        if (index == 0) {
            rect = getWindowCoordinates(0, sidebarsVisible);
            rect.x += pad;
        } else if (index == 2) {
            rect = getWindowCoordinates(3, sidebarsVisible);
            rect.x -= pad;
        } else {
            rect = getWindowCoordinates(0, sidebarsVisible);
            rect.x = ui.applet.width / 2 - (rect.width / 2);
        }
        rect.y += rect.height + 30;
        rect.height = 50;
        return rect;
    }

    public void createGlobalWindow(PerformanceManager pm) {
        int x = ui.applet.width / 2 - CHANNEL_WIDTH / 2;
        GlobalWindow window = new GlobalWindow(pm, ui, x, 700, CHANNEL_WIDTH, 225);
        ui.addLayer(window);
    }

    public void createFaderWindow(LXListenableNormalizedParameter param, int index) {
        Rectangle rect = getFaderCoordinates(index, false);

        FaderWindow window = new FaderWindow(param, ui, "HI", rect.x, rect.y, rect.width, rect.height);
        ui.addLayer(window);
        faderWindows[index] = window;
    }

    public void createChannelWindow(PerformanceManager.PerformanceChannel channel) {
        Rectangle rect = getWindowCoordinates(channel.globalIndex, false);

        ChannelWindow window = new ChannelWindow(lx, ui, channel, rect.x, rect.y, rect.width, rect.height);
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

    public class ChannelWindow extends UIWindow {
        final ArrayList<OverrideLabeledKnob> knobs = new ArrayList();
        final ArrayList<UISwitch> switches = new ArrayList();
        final PerformanceManager pm;
        final PerformanceManager.PerformanceChannel channel;

        UIItemList.ScrollList patternList = null;
        UIDropMenu dropMenu;

//        final Listener listener = new Listener();

        private final LX lx;
        private final UI ui;

        ChannelWindow(LX lx, UI ui, PerformanceManager.PerformanceChannel channel, float x, float y, float w, float h) {

            super(ui, "", x, y, w, h);
            this.ui = ui;
            this.lx = lx;
            this.pm = channel.manager;
            this.channel = channel;

            setTitle(String.format("Channel %d", channel.globalIndex + 1));

//
//
//
//            selection.addListener(new LXParameterListener() {
//                public void onParameterChanged(LXParameter parameter) {
//                    println("PARAMETER CHANGED", parameter.getLabel(), ((DiscreteParameter) parameter).getValuei());
//                    setSelectedChannel();
//                }
//            });

            float afterPatterns = buildPatternList(30);

            float afterKnobs = buildKnobs(afterPatterns + 10);
            float afterSwitches = buildSwitches(afterKnobs + 30);

            refreshPattern();


        }





        class PatternItem extends UIItemList.AbstractItem {
            final LXPattern pattern;
            final PerformanceManager.PerformanceChannel channel;
            final ChannelWindow window;

            PatternItem(LXPattern pattern, ChannelWindow window) {
                this.pattern = pattern;
                this.window = window;
                this.channel = window.channel;
            }

            public String getLabel() {
                return pattern.getLabel();
            }

            boolean isSelected() {
                return false;
            }

            @Override
            public boolean isActive() {
                return pattern == channel.channel.getActivePattern();
            }

            @Override
            public int getActiveColor(UI ui) {
                return ui.theme.getPrimaryColor();
            }

            @Override
            public void onActivate() {
                channel.channel.goPattern(pattern);
                window.refreshPattern();
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
            patternList = new UIItemList.ScrollList(ui, 4, yStart, w - 8, h);
            patternList.addToContainer(this);
            patternList.setSingleClickActivate(true);

            final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
            for (LXPattern pattern : channel.channel.getPatterns()) {
                items.add(new PatternItem(pattern, this));
            }
            patternList.setItems(items);

            return yStart + h;
        }




        void refreshPattern() {

            LXPattern pattern = channel.channel.getActivePattern();

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

            for (LXParameter param : pattern.getParameters()) {
                if (!(param instanceof LXListenableNormalizedParameter)) continue;

                LXListenableNormalizedParameter p = (LXListenableNormalizedParameter) param;

                if (param instanceof BooleanParameter) {
                    if (si >= switches.size()) continue;
                    switches.get(si).setParameter((BooleanParameter) p);
                    switches.get(si).setEnabled(true);
                    si++;

                } else {
                    if (ki >= PARAM_KNOBS) continue;
                    knobs.get(ki).setParameter(p);
                    knobs.get(ki).setEnabled(true);
                    ki++;
                }

            }
            redraw();

            OverrideLabeledKnob blurKnob = knobs.get(PARAM_KNOBS + 0);
            OverrideLabeledKnob desatKnob = knobs.get(PARAM_KNOBS + 1);
            OverrideLabeledKnob hueKnob = knobs.get(PARAM_KNOBS + 2);
            OverrideLabeledKnob cueKnob = knobs.get(PARAM_KNOBS + 3);



            blurKnob.setParameter(channel.effectParams.blur);
            blurKnob.setOverrideLabel("Blur");

            desatKnob.setParameter(channel.effectParams.desaturation);
            desatKnob.setOverrideLabel("Desat");

            hueKnob.setParameter(channel.effectParams.hueShift);
            hueKnob.setOverrideLabel("Hue");

            cueKnob.setParameter(channel.manager.cueState);
            cueKnob.setOverrideLabel("Cue");

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

        OverrideLabelSlider(float x, float y, float w, float h) {
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

    class GlobalWindow extends UIWindow {
        final UIButton[] cueButtons;
        final UIButton liveButton;
        final UIButton sidebarToggle;
        final UISlider globalSliders[];
        final PerformanceManager pm;

        GlobalWindow(PerformanceManager pm, UI ui, float x, float y, float w, float h) {
            super(ui, "Global Params", x, y, w, h);

            this.pm = pm;

            int nQ = 7;
            cueButtons = new UIButton[nQ];

            int toggleY = 30;
            int togglePad = 5;
            float toggleWidth = (w - (togglePad * 3)) / 2;
            liveButton = new UIButton(togglePad, toggleY, toggleWidth, 15);
            liveButton.setLabel("Live");
            liveButton.setParameter(lx.engine.output.enabled);
            liveButton.addToContainer(this);

            sidebarToggle = new UIButton(toggleWidth + (togglePad * 2), toggleY, toggleWidth, 15) {
                public void onToggle(boolean on) {
                    if (!on) {
                        return;
                    }
                    SLStudioLX.UI slUI = (SLStudioLX.UI)ui;
                    slUI.toggleSidebars();
                    pm.gui.moveWindows(slUI.areSidebarsVisible());
                }
            };
            sidebarToggle.setMomentary(true);
            sidebarToggle.setLabel("Toggle Sidebars");
            sidebarToggle.addToContainer(this);


            int nSliders = 4;
            globalSliders = new UISlider[nSliders];
            LXListenableNormalizedParameter[] params = new LXListenableNormalizedParameter[]{
                pm.globalParams.brightness,
                pm.globalParams.speed,
                pm.globalParams.effectParams.desaturation,
                pm.globalParams.effectParams.blur
            };
            String[] labels = new String[]{
                "Brightness",
                "Speed",
                "Desat",
                "Blur"
            };

            for (int i = 0; i < nSliders; i++) {
                float sw = w - togglePad*2;
                float sh = 20;
                float sx = togglePad;
                float sy = 50 + i * (sh + 15);
                OverrideLabelSlider slider = new OverrideLabelSlider(sx, sy, sw, sh);
                slider.setOverrideLabel(labels[i]);
                slider.setParameter(params[i]);
                slider.addToContainer(this);
                globalSliders[i] = slider;
            }


            buildCues();
        }

        void buildCues() {
            float w = getWidth();
            float h = getHeight();
            int nQ = cueButtons.length;

            String[] labels = new String[] {
                "1",
                "L",
                "2",
                "All",
                "3",
                "R",
                "4"
            };

            float queueWidth = 20;
            float cueSpacing = (w - (nQ * queueWidth)) / (nQ + 1);
            for (int i = 0; i < nQ; i++) {
                float qx = (queueWidth * i) + (cueSpacing * (i + 1));
                float qy = h - queueWidth - 3;
                final int j = i;
                UIButton q = new UIButton(qx, qy, queueWidth, queueWidth) {
                    public void onToggle(boolean on) {
                        if (!on) {
                            return;
                        }
                        int nEffects = lx.engine.masterChannel.effects.size();
                        pm.cueState.setValue(j);
                    }
                };
                q.setLabel(labels[i]);
                cueButtons[i] = q;
                q.addToContainer(this);
            }

            pm.cueState.addListener(new LXParameterListener() {
                @Override
                public void onParameterChanged(LXParameter lxParameter) {
                    updateCues();
                }
            });
            updateCues();
        }

        void updateCues() {
            int c = pm.cueState.getValuei();
            for (int i = 0; i < cueButtons.length; i++) {
                cueButtons[i].setActive(i == c);
            }
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
        public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        }
    }
}
