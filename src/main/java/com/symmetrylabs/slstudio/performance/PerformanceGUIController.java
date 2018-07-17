package com.symmetrylabs.slstudio.performance;

import heronarts.lx.LXChannel;
import heronarts.lx.LXComponent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import heronarts.lx.LX;
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
        float w = CHANNEL_WIDTH * 1.7f;
        float pad = CHANNEL_WIDTH + 10 - w/2;
        Rectangle rect;
        if (index == 0) {
            rect = getWindowCoordinates(0, sidebarsVisible);
            rect.x += pad;
        } else if (index == 2) {
            rect = getWindowCoordinates(2, sidebarsVisible);
            rect.x += pad;
        } else {
            rect = getWindowCoordinates(0, sidebarsVisible);
            rect.x = (int)(ui.applet.width / 2 - (w / 2));
        }
        rect.width = (int)w;
        rect.y += rect.height + 30;
        rect.height = 73;
        return rect;
    }

    public void createGlobalWindow(PerformanceManager pm) {
        int x = ui.applet.width / 2 - CHANNEL_WIDTH / 2;
        GlobalWindow window = new GlobalWindow(pm, ui, x, 710, CHANNEL_WIDTH, 210);
        ui.addLayer(window);
    }

    public void createFaderWindow(LXListenableNormalizedParameter param, ObjectParameter<LXBlend> blendParam, int index) {
        Rectangle rect = getFaderCoordinates(index, false);

        FaderWindow window = new FaderWindow(param, blendParam, ui, "HI", rect.x, rect.y, rect.width, rect.height);
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

    public void updateAllPatternLists() {
        for (ChannelWindow w : channelWindows) {
            w.updatePatternList();
        }
    }


    public class ChannelWindow extends UIWindow {
        final ArrayList<OverrideLabeledKnob> knobs = new ArrayList();
        final ArrayList<UISwitch> switches = new ArrayList();
        final PerformanceManager pm;
        final PerformanceManager.PerformanceChannel channel;

        PatternList patternList = null;

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
            updatePatternList();


        }




        class PatternList extends UIItemList.ScrollList {

            public PatternList(UI ui, float x, float y, float w, float h) {
                super(ui, x, y, w, h);
            }

            @Override
            public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                super.onKeyPressed(keyEvent, keyChar, keyCode);

                if (keyChar == 'h') {
                    PatternItem item = (PatternItem)getFocusedItem();
                    item.window.pm.toggleHidden(item.pattern);
                    if (item.isHidden()) {
                        PatternItem newTop = (PatternItem)(getItems().get(0));
                        newTop.onActivate();
                    }

                }
            }
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

            Comparator<LXPattern> labelSort = new Comparator<LXPattern>() {
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
            patternList.setItems(items);
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
//            OverrideLabeledKnob cueKnob = knobs.get(PARAM_KNOBS + 3);



            blurKnob.setParameter(channel.effectParams.blur);
            blurKnob.setOverrideLabel("Blur");

            desatKnob.setParameter(channel.effectParams.desaturation);
            desatKnob.setOverrideLabel("Desat");

            hueKnob.setParameter(channel.effectParams.hueShift);
            hueKnob.setOverrideLabel("Hue");

//            cueKnob.setParameter(channel.manager.cueState);
//            cueKnob.setOverrideLabel("Cue");

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
        final UIButton[] cueButtons;
        final UIButton liveButton;
        final UIButton sidebarToggle;
        final UISlider globalSliders[];
        final PerformanceManager pm;

        GlobalWindow(PerformanceManager pm, UI ui, float x, float y, float w, float h) {
            super(ui, "", x, y, w, h);

            this.pm = pm;

            int nQ = 7;
            cueButtons = new UIButton[nQ];

            int toggleY = 35;
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
//                float sw = w - togglePad*2;
//                float sh = 20;
//                float sx = togglePad;
//                float sy = 50 + i * (sh + 15);
                float sw = (w - (togglePad * (nSliders + 1))) / nSliders;
                float sh = 125;
                float sy = 66;

                float sx =     (togglePad * (i + 1)) + (sw * i);

                OverrideLabelSlider slider = new OverrideLabelSlider(UISlider.Direction.VERTICAL, sx, sy, sw, sh);
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
                float qy = 5; //h - queueWidth - 3;
                final int j = i;
                UIButton q = new UIButton(qx, qy, queueWidth, queueWidth) {
                    public void onToggle(boolean on) {
                        if (!on) {
                            return;
                        }
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
//        final UIDropMenu blendMenu;
        final UIButtonGroup blendGroup;

        FaderWindow(LXListenableNormalizedParameter param, ObjectParameter<LXBlend> blendParam, UI ui, String title, float x, float y, float w, float h) {
            super(ui, title, x, y, w, h);

            float pad = 5;
            float sw = w - (2 * pad);
            float sh = 30;
            slider = new UISlider(UISlider.Direction.HORIZONTAL, pad, pad, sw, sh);
            slider.setShowLabel(false);
            slider.addToContainer(this);
            slider.setParameter(param);

//            blendMenu = new UIDropMenu(pad, h - (2 * pad) - 20, w - (2 * pad), 20, blendParam);
             blendGroup = new UIButtonGroup(blendParam, pad, pad + sh + 10, sw, 20);
            blendGroup.addToContainer(this);


        }

        @Override
        public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
        }
    }
}
