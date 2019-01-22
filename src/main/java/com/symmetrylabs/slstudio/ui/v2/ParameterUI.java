package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class ParameterUI {
    public static void draw(LX lx, BoundedParameter p) {
        float start = p.getValuef();
        final float res =
            UI.sliderFloat(p.getLabel(), start, (float) p.range.v0, (float) p.range.v1);
        if (start != res) {
            lx.engine.addTask(() -> p.setValue(res));
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
}
