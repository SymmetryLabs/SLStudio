package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.*;

import heronarts.lx.parameter.BooleanParameter.Mode;

public class ComponentUI {
    private final LX lx;
    private final LXComponent comp;
    private final ParameterUI pui;

    private final List<LXParameter> knobs = new ArrayList<>();
    private final List<LXParameter> params = new ArrayList<>();
    private final Set<LXParameter> blacklist = new HashSet<>();

    public ComponentUI(LX lx, LXComponent comp, ParameterUI pui) {
        this.lx = lx;
        this.comp = comp;
        this.pui = pui;

        /* some parameters add other sub-parameters to be "helpful"; ColorParameter
           adds three child parameters for H/S/B when you add it. We just want to draw
           a rich UI for the parent pattern instead, so we detect and hide those
           sub-parameters. */
        for (LXParameter param : comp.getParameters()) {
            if (!param.isVisible()) {
                continue;
            }
            if (param instanceof ColorParameter) {
                ColorParameter cp = (ColorParameter) param;
                blacklist.add(cp.hue);
                blacklist.add(cp.saturation);
                blacklist.add(cp.brightness);
                params.add(param);
            } else if (param instanceof BoundedParameter || param instanceof BooleanParameter) {
                knobs.add(param);
            } else if (param instanceof DiscreteParameter) {
                params.add(param);
            }
        }
        knobs.removeAll(blacklist);
        params.removeAll(blacklist);
    }

    public void draw() {
        boolean needSep = false;
        if (pui.peek().preferKnobsForFloats) {
            for (int i = 0; i < knobs.size(); i++) {
                needSep = true;
                pui.draw(knobs.get(i));
                if (i % 4 != 3 && i != knobs.size() - 1) {
                    UI.sameLine();
                }
            }
            if (!params.isEmpty() && needSep) {
                UI.separator();
                needSep = false;
            }
            for (LXParameter param : params) {
                needSep = true;
                pui.draw(param);
            }
        } else {
            for (LXParameter param : comp.getParameters()) {
                if (blacklist.contains(param)) {
                    continue;
                }
                pui.draw(param);
            }
        }
    }

    public void blacklist(LXParameter[] blacklistParams) {
        blacklist.addAll(Arrays.asList(blacklistParams));
    }
}
