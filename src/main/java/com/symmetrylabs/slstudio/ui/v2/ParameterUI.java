package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

public class ParameterUI {
    enum WidgetType {
        KNOB,
        SLIDER,
    };

    public static void draw(LX lx, BoundedParameter p) {
        draw(lx, p, WidgetType.SLIDER);
    }

    public static void draw(LX lx, BoundedParameter p, WidgetType wt) {
        float start = p.getValuef();
        float res = start;
        if (wt == WidgetType.SLIDER) {
            res = UI.sliderFloat(p.getLabel(), start, (float) p.range.v0, (float) p.range.v1);
        } else if (wt == WidgetType.KNOB) {
            res = UI.knobFloat(p.getLabel(), start, (float) p.range.v0, (float) p.range.v1);
        }
        if (start != res) {
            final float fres = res;
            lx.engine.addTask(() -> p.setValue(fres));
        }
    }

    public static void draw(LX lx, DiscreteParameter p) {
        String[] options = p.getOptions();
        if (options == null) {
            int start = p.getValuei();
            final int res = UI.sliderInt(
                p.getLabel(), start, p.getMinValue(), p.getMaxValue() - 1);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        } else {
            int start = p.getValuei();
            int res = UI.combo(p.getLabel(), start, options);
            if (start != res) {
                lx.engine.addTask(() -> p.setValue(res));
            }
        }
    }

    public static void draw(LX lx, BooleanParameter p) {
        boolean start = p.getValueb();
        boolean res;
        if (p.getMode() == BooleanParameter.Mode.TOGGLE) {
            res = UI.checkbox(p.getLabel(), start);
        } else {
            UI.button(p.getLabel());
            res = UI.isItemActive();
        }
        if (res != start) {
            lx.engine.addTask(() -> p.setValue(res));
        }
    }

    public static void draw(LX lx, ColorParameter p) {
        int start = p.getColor();
        int res = UI.colorPicker(p.getLabel(), start);
        if (res != start) {
            lx.engine.addTask(() -> p.setColor(res));
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
}
