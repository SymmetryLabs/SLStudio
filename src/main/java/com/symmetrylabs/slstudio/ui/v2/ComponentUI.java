package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.HashSet;
import java.util.List;
import heronarts.lx.parameter.BooleanParameter.Mode;
import java.util.ArrayList;

public class ComponentUI {
    private final LX lx;
    private final LXComponent comp;

    private final List<BoundedParameter> knobs = new ArrayList<>();
    private final List<BooleanParameter> triggers = new ArrayList<>();
    private final List<LXParameter> params = new ArrayList<>();

    public ComponentUI(LX lx, LXComponent comp) {
        this.lx = lx;
        this.comp = comp;

        /* some parameters add other sub-parameters to be "helpful"; ColorParameter
           adds three child parameters for H/S/B when you add it. We just want to draw
           a rich UI for the parent pattern instead, so we detect and hide those
           sub-parameters. */
        HashSet<LXParameter> blacklist = new HashSet<>();
        for (LXParameter param : comp.getParameters()) {
            if (param instanceof ColorParameter) {
                ColorParameter cp = (ColorParameter) param;
                blacklist.add(cp.hue);
                blacklist.add(cp.saturation);
                blacklist.add(cp.brightness);
                params.add(param);
            } else if (param instanceof BoundedParameter) {
                knobs.add((BoundedParameter) param);
            } else if (param instanceof DiscreteParameter) {
                params.add(param);
            } else if (param instanceof BooleanParameter) {
                BooleanParameter bp = (BooleanParameter) param;
                if (bp.getMode() == BooleanParameter.Mode.MOMENTARY) {
                    triggers.add(bp);
                } else {
                    params.add(bp);
                }
            }
        }
        knobs.removeAll(blacklist);
        triggers.removeAll(blacklist);
        params.removeAll(blacklist);
    }

    public void draw() {
        boolean needSep = false;
        for (int i = 0; i < knobs.size(); i++) {
            needSep = true;
            ParameterUI.draw(lx, knobs.get(i), ParameterUI.WidgetType.KNOB);
            if (i % 4 != 3 && i != knobs.size() - 1) {
                UI.sameLine();
            }
        }
        if (!triggers.isEmpty() && needSep) {
            UI.separator();
            needSep = false;
        }
        for (int i = 0; i < triggers.size(); i++) {
            needSep = true;
            ParameterUI.draw(lx, triggers.get(i));
            if (i % 4 != 3 && i != triggers.size() - 1) {
                UI.sameLine();
            }
        }
        if (!params.isEmpty() && needSep) {
            UI.separator();
            needSep = false;
        }
        for (LXParameter param : params) {
            needSep = true;
            ParameterUI.draw(lx, param);
        }
    }
}