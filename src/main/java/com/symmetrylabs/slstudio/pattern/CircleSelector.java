package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.slstudio.model.CirclesModel;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

public class CircleSelector extends SLPattern<CirclesModel<DoubleStrip>> {
    private DiscreteParameter circleNumber;

    public CircleSelector(LX lx) {
        super(lx);

        addParameter(circleNumber = new DiscreteParameter("CircleNumber", getModel().getCircles().size()));
    }

    public void run(double deltaMs) {
        setColors(LXColor.BLACK);

        for (LXPoint point : getModel().getCircles().get(circleNumber.getValuei()).getPoints()) {
            colors[point.index] = LXColor.RED;
        }
    }
}
