package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

public class CircleWipe extends LXEffect {
    private CompoundParameter radius = new CompoundParameter("rad", 0, 0, model.rMax);
    private BooleanParameter invert = new BooleanParameter("invert", false);
    private final CompoundParameter cXParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private final CompoundParameter cYParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);

    public CircleWipe(LX lx) {
        super(lx);
        addParameter(radius);
        addParameter(invert);
        addParameter(cXParam);
        addParameter(cYParam);
    }

    @Override
    public void run(double deltaMs, double amount) {
        double thresholdRadius = Math.pow(radius.getValue(), 2);
        boolean outside = invert.getValueb();
        for (LXVector v : getVectors()) {
            double rad = Math.pow(v.x - cXParam.getValue(), 2) + Math.pow(v.y - cYParam.getValue(), 2);
            boolean inside = rad < thresholdRadius;
            if (!(inside ^ outside)) {
                colors[v.index] = LXColor.BLACK;
            }
        }
    }
}
