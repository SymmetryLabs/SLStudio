package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

public class ComponentWindow extends CloseableWindow {
    private final LX lx;
    private final LXComponent comp;

    public ComponentWindow(LX lx, String name, LXComponent comp) {
        super(name);
        this.lx = lx;
        this.comp = comp;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaultToCursor(UI.DEFAULT_WIDTH, 350);
    }

    @Override
    protected void drawContents() {
        for (LXParameter param : comp.getParameters()) {
            if (param instanceof BoundedParameter) {
                drawBoundedParam((BoundedParameter) param);
            } else if (param instanceof DiscreteParameter) {
                drawDiscreteParam((DiscreteParameter) param);
            } else if (param instanceof BooleanParameter) {
                drawBooleanParam((BooleanParameter) param);
            }
        }
    }

    public static void drawBoundedParam(BoundedParameter p) {
        float start = p.getValuef();
        float res = UI.sliderFloat(p.getLabel(), start, (float) p.range.v0, (float) p.range.v1);
        if (start != res) {
            p.setValue(res);
        }
    }

    public static void drawDiscreteParam(DiscreteParameter p) {
        String[] options = p.getOptions();
        if (options == null) {
            int start = p.getValuei();
            int res = UI.sliderInt(p.getLabel(), start, p.getMinValue(), p.getMaxValue() - 1);
            if (start != res) {
                p.setValue(res);
            }
        } else {
            int start = p.getValuei();
            int res = UI.combo(p.getLabel(), start, options);
            if (start != res) {
                p.setValue(res);
            }
        }
    }

    public static void drawBooleanParam(BooleanParameter p) {
        boolean start = p.getValueb();
        boolean res;
        if (p.getMode() == BooleanParameter.Mode.TOGGLE) {
            res = UI.checkbox(p.getLabel(), start);
        } else {
            UI.button(p.getLabel());
            res = UI.isItemActive();
        }
        if (res != start) {
            p.setValue(res);
        }
    }

    protected boolean isEligibleControlParameter(LXParameter parameter) {
        return parameter instanceof BoundedParameter
            || parameter instanceof DiscreteParameter
            || parameter instanceof BooleanParameter;
    }
}
