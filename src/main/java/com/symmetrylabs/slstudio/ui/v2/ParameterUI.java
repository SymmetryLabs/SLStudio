package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.CompoundParameter;

public class ParameterUI {
    public enum WidgetType {
        KNOB,
        SLIDER,
    };

    public static String getID(LXParameter p) {
        return getID(p, false);
    }

    public static String getID(LXParameter p, boolean invis) {
        LXComponent parent = p.getComponent();
        if (parent == null) {
            return (invis ? "##" : "") + p.getLabel();
        }
        return String.format(
            invis ? "##%s/%d/%s" : "%s##%d/%s",
            p.getLabel(), parent.getId(), p.getLabel());
    }

    public static void draw(LX lx, BoundedParameter p) {
        draw(lx, p, WidgetType.SLIDER);
    }

    public static void draw(LX lx, BoundedParameter p, WidgetType wt) {
        float start = p.getValuef();
        float res = start;
        if (wt == WidgetType.SLIDER) {
            res = UI.sliderFloat(getID(p), start, (float) p.range.v0, (float) p.range.v1);
        } else if (wt == WidgetType.KNOB) {
            if (p instanceof CompoundParameter) {
                res = compoundKnob(lx, (CompoundParameter) p);
            } else {
                res = UI.knobFloat(getID(p), start, (float) p.range.v0, (float) p.range.v1);
            }
        }
        if (start != res) {
            final float fres = res;
            lx.engine.addTask(() -> p.setValue(fres));
        }
    }

    private static float compoundKnob(LX lx, CompoundParameter cp) {
        float base = (float) cp.getBaseValue();
        float mod = (float) cp.getValue();
        int N = cp.modulations.size();
        float[] mins = new float[N];
        float[] maxs = new float[N];
        int[] colors = new int[N];
        for (int i = 0; i < N; i++) {
            LXCompoundModulation modulation = cp.modulations.get(i);

            float modStart, modEnd;
            switch (modulation.getPolarity()) {
            case BIPOLAR:
                modStart = -modulation.range.getValuef();
                modEnd = modulation.range.getValuef();
                break;
            default:
            case UNIPOLAR:
                modStart = 0;
                modEnd = modStart + modulation.range.getValuef();
                break;
            }
            modStart = (float) (cp.range.v0 + modStart * (cp.range.v1 - cp.range.v0) + base);
            modEnd = (float) (cp.range.v0 + modEnd * (cp.range.v1 - cp.range.v0) + base);
            mins[i] = Float.min(modStart, modEnd);
            maxs[i] = Float.max(modStart, modEnd);
            colors[i] = modulation.color.getColor();
        }
        return UI.knobModulatedFloat(
            getID(cp), base, (float) cp.range.v0, (float) cp.range.v1, mod, N, mins, maxs, colors);
    }

    public static void draw(LX lx, DiscreteParameter p) {
        String[] options = p.getOptions();
        if (options == null) {
            int start = p.getValuei();
            final int res = UI.sliderInt(
                getID(p), start, p.getMinValue(), p.getMaxValue() - 1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        } else {
            int start = p.getValuei();
            int res = UI.combo(getID(p), start, options);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        }
    }

    public static void draw(LX lx, BooleanParameter p) {
        draw(lx, p, false);
    }

    public static void draw(LX lx, BooleanParameter p, boolean important) {
        boolean start = p.getValueb();
        boolean res;
        if (p.getMode() == BooleanParameter.Mode.TOGGLE) {
            if (important && p.getValueb()) {
                UI.pushColor(UI.COLOR_WIDGET, UI.RED);
                UI.pushColor(UI.COLOR_WIDGET_HOVERED, UI.RED_HOVER);
            }
            res = UI.checkbox(getID(p), start);
            if (important && p.getValueb()) {
                UI.popColor(2);
            }
        } else {
            UI.button(getID(p));
            res = UI.isItemActive();
        }
        if (res != start) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }

    public static void draw(LX lx, ColorParameter p) {
        int start = p.getColor();
        float h = p.hue.getValuef();
        float s = p.saturation.getValuef();
        float b = p.brightness.getValuef();
        UI.pushWidth(UI.calcWidth() / 4);
        float nh = UI.floatBox(getID(p.hue, true), h, 1, 0, 360, "H:%0.0f");
        UI.sameLine();
        float ns = UI.floatBox(getID(p.saturation, true), s, 1, 0, 100, "S:%0.0f");
        UI.sameLine();
        float nb = UI.floatBox(getID(p.brightness, true), b, 1, 0, 100, "B:%0.0f");
        UI.popWidth();
        UI.sameLine();
        UI.colorButton(p.getLabel(), nh, ns, nb);
        UI.sameLine();
        UI.text(p.getLabel());
        if (h != nh || s != ns || b != nb) {
            lx.engine.addTask(() -> {
                    p.hue.setValue(nh);
                    p.saturation.setValue(ns);
                    p.brightness.setValue(nb);
                });
        }
    }

    public static void draw(LX lx, LXParameter param) {
        if (param instanceof BoundedParameter) {
            ParameterUI.draw(lx, (BoundedParameter) param);
        } else if (param instanceof DiscreteParameter) {
            ParameterUI.draw(lx, (DiscreteParameter) param);
        } else if (param instanceof BooleanParameter) {
            ParameterUI.draw(lx, (BooleanParameter) param);
        } else if (param instanceof ColorParameter) {
            ParameterUI.draw(lx, (ColorParameter) param);
        }
    }

    public static void menuItem(LX lx, BooleanParameter p) {
        menuItem(lx, p, getID(p));
    }

    public static void menuItem(LX lx, BooleanParameter p, String label) {
        boolean res = UI.menuItemToggle(label, null, p.getValueb(), true);
        if (res != p.getValueb()) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }
}
