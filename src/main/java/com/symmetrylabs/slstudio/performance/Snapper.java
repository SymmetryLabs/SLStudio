package com.symmetrylabs.slstudio.performance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.*;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.*;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UISwitch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class Snapper extends LXComponent {
    final SLStudioLX lx;
    final SLStudioLX.UI ui;
    final UIWindow window;
    final UI2dContainer hueGrid;

    final ArrayList<CompoundParameter> hueParams;
    final ArrayList<BooleanParameter> triggerParams;

    final ArrayList<UIButton> hueButtons;
    final String HUES_KEY = "hues";
    final int BUTTON_WIDTH = 30;
    final int CONTROL_HEIGHT = 20;
    final int MARGIN = 7;
    final int PER_ROW = 4;
    final LXParameterListener channelChangeListener;

    LXNormalizedParameter targetHueParam = null;
    int lastActivePatternIndex = -1;

    public static enum Mode {
        NORMAL,
        LOAD,
        DELETE
    }

    EnumParameter<Mode> mode;

    float startHue;
    float goalHue;
    float elapsed;
    boolean fadeRunning = false;
    CompoundParameter fadeDuration = new CompoundParameter("fadeDuration", 500, 0, 5000);
    BooleanParameter useFade = new BooleanParameter("useFade", false);


    public Snapper(SLStudioLX lx, SLStudioLX.UI ui) {
        super(lx, "Snapper");

        addParameter(fadeDuration);
        addParameter(useFade);

        this.lx = lx;
        this.ui = ui;

        mode = new EnumParameter<Mode>("mode", Mode.NORMAL);

        float xOff = 215;
        float yOff = 250;
        float width = (BUTTON_WIDTH * PER_ROW) + (MARGIN * (PER_ROW + 1));
        window = new UIWindow(ui, "Snapper", xOff, yOff, width, 300);
        window.setLayout(UI2dContainer.Layout.VERTICAL);
        window.setChildMargin(7);
        ui.addLayer(window);

        hueGrid = new UI2dContainer(0, 0, width, 225);
        hueGrid.setLayout(UI2dContainer.Layout.HORIZONTAL_GRID);
        hueGrid.setChildMargin(7);
        hueGrid.setPadding(7);
        hueGrid.addToContainer(window);

        UI2dContainer controls = new UI2dContainer(0, 0, width, 50);
        controls.setLayout(UI2dContainer.Layout.HORIZONTAL_GRID);
        controls.setChildMargin(7);
        controls.setPadding(7);
        controls.addToContainer(window);

        UIButton newButton = new UIButton(0, 0, BUTTON_WIDTH, CONTROL_HEIGHT) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    onNewHue();
                }
            }
        };
        newButton.setMomentary(true);
        newButton.setLabel("NEW");
        newButton.addToContainer(controls);

        UIButton deleteButton = new UIButton(0, 0, BUTTON_WIDTH, CONTROL_HEIGHT) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    onDelHue();
                } else {
                    mode.setValue(Mode.NORMAL);
                }
            }
        };
        deleteButton.setMomentary(false);
        deleteButton.setLabel("DEL");
        deleteButton.addToContainer(controls);

        UIButton loadButton = new UIButton(0, 0, BUTTON_WIDTH, CONTROL_HEIGHT) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    onLoadHue();
                } else {
                    mode.setValue(Mode.NORMAL);
                }
            }
        };
        loadButton.setMomentary(false);
        loadButton.setLabel("LOAD");
        loadButton.addToContainer(controls);

        UIButton sortButton = new UIButton(0, 0, BUTTON_WIDTH, CONTROL_HEIGHT) {
            @Override
            protected void onToggle(boolean active) {
                if (active) {
                    onSortHues();
                }
            }
        };
        sortButton.setMomentary(true);
        sortButton.setLabel("SORT");
        sortButton.addToContainer(controls);

        mode.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                switch (mode.getEnum()) {
                    case NORMAL:
                        loadButton.setActive(false);
                        deleteButton.setActive(false);
                        break;
                    case LOAD:
                        loadButton.setActive(true);
                        deleteButton.setActive(false);
                        break;
                    case DELETE:
                        loadButton.setActive(false);
                        deleteButton.setActive(true);
                        break;
                }
            }
        });

        UIKnob durationKnob = new UIKnob();
        durationKnob.setParameter(fadeDuration);
        durationKnob.addToContainer(controls);

        UISwitch useFadeSwitch = new UISwitch(0, 0);
        useFadeSwitch.setParameter(useFade);
        useFadeSwitch.addToContainer(controls);


        hueParams = new ArrayList<>();
        hueButtons = new ArrayList<>();
        triggerParams = new ArrayList<>();

        initDefaultHues();
        setButtons();

        onChannelChange();
        channelChangeListener = new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                onChannelChange();
            }
        };
        lx.engine.focusedChannel.addListener(channelChangeListener);

        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double deltaMs) {
                listenForPatternChanges();
                runFade(deltaMs);

            }
        });
    }

    void listenForPatternChanges() {
        LXBus bus = lx.engine.getFocusedChannel();
        int patternIndex;
        if (bus instanceof LXChannel) {
            patternIndex = ((LXChannel)bus).getActivePatternIndex();
        } else {
            patternIndex = -1;
        }
        if (patternIndex != lastActivePatternIndex) {
            onChannelChange();
        }
        lastActivePatternIndex = patternIndex;
    }

    void runFade(double deltaMs) {
        if (!fadeRunning) return;
        float done = MathUtils.constrain(elapsed / fadeDuration.getValuef(), 0, 1);
        float c;
        if ((goalHue - startHue) > 0.5) {
            float margin = 1.0f - goalHue;
            float in = MathUtils.lerp(startHue, -margin, done);
            c = in < 0 ? 1.0f + in : in;
        } else if ((startHue - goalHue) > 0.5) {
            float in = MathUtils.lerp(startHue, 1.0f + goalHue, done);
            c = in % 1.0f;
        } else {
            c = MathUtils.lerp(startHue, goalHue, done) % 1.0f;
        }
        targetHueParam.setNormalized(c);
        elapsed += deltaMs;
        if (done >= 1.0) {
            fadeRunning = false;
        }
    }

    LXNormalizedParameter findHueParameter(Collection<LXParameter> parameters) {
        for (LXParameter param : parameters) {
            if (!(param instanceof  LXNormalizedParameter)) continue;
            String name = param.getLabel().toLowerCase();
            String[] candidates = new String[]{
                "hue",
                "hue1",
                "color-h",
            };
            for (String cand : candidates) {
                if (name.equals(cand)) {
                    return (LXNormalizedParameter)param;
                }
            }
        }
        return null;
    }

    LXNormalizedParameter findHueParameter(LXChannel c) {
        LXEffect e = c.getEffect("ColorFilter");
        if (e != null && e.enabled.isOn()) {
            return findHueParameter(e.getParameters());
        }

        LXPattern p = c.getActivePattern();
        return findHueParameter(p.getParameters());
    }

    void setTargetHueParam() {
        LXBus bus = lx.engine.getFocusedChannel();

        if (bus instanceof LXChannel) {
            LXChannel chan = (LXChannel)bus;
            targetHueParam = findHueParameter(chan);
        } else {
            targetHueParam = null;
            lastActivePatternIndex = -1;
        }
    }

    void onChannelChange() {
        setTargetHueParam();

        for (UIButton b : hueButtons) {
            b.setEnabled(targetHueParam != null);
        }

    }


    void initDefaultHues() {
        int n = 10;
        for (int i = 0; i < n; i++) {
            hueParams.add(new CompoundParameter("hue", (360 / n) * i, 0, 360));
        }
    }

    void setButtons() {
        for (UIButton b : hueButtons) {
            b.removeFromContainer();
        }
        hueButtons.clear();

        int i = 0;
        for (CompoundParameter param : hueParams) {
            UIButton b = new UIButton(0, 0, BUTTON_WIDTH, BUTTON_WIDTH) {

                @Override
                protected void onToggle(boolean active) {
                    boolean actuallyActive = targetHueParam != null;
                    setActive(actuallyActive);

                    if (!active) {
                        return;
                    }

                    Mode m = mode.getEnum();

                    setTargetHueParam();

                    if (m == Mode.NORMAL && actuallyActive) {
                        startHue = targetHueParam.getNormalizedf();
                        goalHue = param.getNormalizedf();
                        if (!useFade.isOn()) {
                            targetHueParam.setNormalized(param.getNormalized());
                        } else {
                            elapsed = 0;
                            fadeRunning = true;
                        }

                    }

                    if (m == Mode.DELETE) {
                        hueParams.remove(param);
                        mode.setValue(Mode.NORMAL);
                        setButtons();
                    }

                    if (m == Mode.LOAD) {
                        mode.setValue(Mode.NORMAL);
                        if (actuallyActive) {
                            param.setNormalized(targetHueParam.getNormalized());
                            setButtons();
                        }
                    }
                }
            };

            int bright = LXColor.hsb(param.getValuef(), 100, 100);
            int dim = LXColor.hsb(param.getValuef(), 100, 50);
            b.setActiveColor(bright);
            b.setInactiveColor(bright);
            b.addToContainer(hueGrid);
            b.setMappable(true);
            b.setTriggerable(true);
            BooleanParameter p;
            if (i >= triggerParams.size()) {
                String name = String.format("trig-%d", i);
                p = new BooleanParameter(name);
                addParameter(p);
                triggerParams.add(p);
            } else {
                p = triggerParams.get(i);
            }
            i++;
            b.setParameter(p);
            hueButtons.add(b);
        }
        onChannelChange();
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        JsonArray jHues = new JsonArray();
        for (CompoundParameter param : hueParams) {
            jHues.add(param.getValuef());
        }
        obj.add(HUES_KEY, jHues);

        super.save(lx, obj);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        super.load(lx, obj);

        if (obj.has(HUES_KEY)) {
            hueParams.clear();

            JsonArray jHues = obj.getAsJsonArray(HUES_KEY);
            for (JsonElement e : jHues) {
                float hue = e.getAsFloat();
                CompoundParameter param = new CompoundParameter("hue", hue, 0, 360);
                hueParams.add(param);
            }
        }

        setButtons();

    }

    void onNewHue() {
        CompoundParameter param = new CompoundParameter("hue", 0, 0, 360);
        if (targetHueParam != null) {
            param.setNormalized(targetHueParam.getNormalized());
        }
        hueParams.add(param);
        setButtons();
    }

    void onDelHue() {
        if (mode.getEnum() == Mode.DELETE) {
            mode.setValue(Mode.NORMAL);
        } else {
            mode.setValue(Mode.DELETE);
        }
    }

    void onLoadHue() {
        if (mode.getEnum() == Mode.LOAD) {
            mode.setValue(Mode.NORMAL);
        } else {
            mode.setValue(Mode.LOAD);
        }
    }

    void onSortHues() {
        hueParams.sort((Comparator<CompoundParameter>) (o1, o2) -> Float.compare(o1.getValuef(), o2.getValuef()));
        setButtons();
    }

}