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

    /* type-safe booleans */
    public enum ShowLabel {
        YES,
        NO,
    };

    public static String getID(LXParameter p) {
        return getID(p, ShowLabel.YES);
    }

    public static String getID(LXParameter p, ShowLabel invis) {
        LXComponent parent = p.getComponent();
        if (parent == null) {
            return (invis == ShowLabel.NO ? "##" : "") + p.getLabel();
        }
        return String.format(
            invis == ShowLabel.NO ? "##%s/%d/%s" : "%s##%d/%s",
            p.getLabel(), parent.getId(), p.getLabel());
    }

    public static void draw(LX lx, BoundedParameter p) {
        draw(lx, p, WidgetType.SLIDER);
    }

    public static void draw(LX lx, BoundedParameter p, WidgetType wt) {
        if (wt == WidgetType.SLIDER) {
            float start = p.getNormalizedf();
            final float res = UI.sliderFloat(getID(p), start, (float) p.range.v0, (float) p.range.v1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
            return;
        }
        final float start = p.getNormalizedf();
        final float res =
            p instanceof CompoundParameter
            ? compoundKnob(lx, (CompoundParameter) p)
            : UI.knobFloat(getID(p), p.getValuef(), start);
        if (start != res) {
            final float fres = res;
            lx.engine.addTask(() -> p.setNormalized(fres));
        }
    }

    private static float compoundKnob(LX lx, CompoundParameter cp) {
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
            mins[i] = Float.min(modStart, modEnd);
            maxs[i] = Float.max(modStart, modEnd);
            colors[i] = modulation.color.getColor();
        }
        return UI.knobModulatedFloat(
            getID(cp), (float) cp.getBaseValue(), (float) cp.getBaseNormalized(), cp.getNormalizedf(), N, mins, maxs, colors);
    }

    public static void draw(LX lx, DiscreteParameter p, ShowLabel showLabel) {
        String[] options = p.getOptions();
        if (options == null) {
            int start = p.getValuei();
            final int res = UI.sliderInt(
                getID(p, showLabel), start, p.getMinValue(), p.getMaxValue() - 1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        } else {
            int start = p.getValuei();
            int res = UI.combo(getID(p, showLabel), start, options);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        }
    }

    public static boolean toggle(String label, boolean active, boolean important, float w) {
        if (active) {
            UI.pushColor(UI.COLOR_BUTTON, important ? UI.RED : UI.BLUE);
            UI.pushColor(UI.COLOR_BUTTON_HOVERED, important ? UI.RED_HOVER : UI.BLUE_HOVER);
        }
        boolean flip = UI.button(label, w, 0);
        if (active) {
            UI.popColor(2);
        }
        return flip ? !active : active;
    }

    public static void toggle(LX lx, BooleanParameter p, boolean important, float w) {
        final boolean start = p.getValueb();
        final boolean res = toggle(getID(p), start, important, w);
        if (res != start) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }

    public static void draw(LX lx, BooleanParameter p) {
        draw(lx, p, false);
    }

    public static void draw(LX lx, BooleanParameter p, boolean important) {
        final boolean start = p.getValueb();
        boolean res;
        if (p.getMode() == BooleanParameter.Mode.TOGGLE) {
            if (important && start) {
                UI.pushColor(UI.COLOR_WIDGET, UI.RED);
                UI.pushColor(UI.COLOR_WIDGET_HOVERED, UI.RED_HOVER);
            }
            res = UI.checkbox(getID(p), start);
            if (important && start) {
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
        float[] res = UI.colorPickerHSV(getID(p), h, s, b);
        if (h != res[0] || s != res[1] || b != res[2]) {
            lx.engine.addTask(() -> {
                    p.hue.setValue(res[0]);
                    p.saturation.setValue(res[1]);
                    p.brightness.setValue(res[2]);
                });
        }
    }

    public static void draw(LX lx, LXParameter param) {
        if (param instanceof BoundedParameter) {
            ParameterUI.draw(lx, (BoundedParameter) param);
        } else if (param instanceof DiscreteParameter) {
            ParameterUI.draw(lx, (DiscreteParameter) param, ShowLabel.YES);
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
