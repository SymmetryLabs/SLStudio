package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.LXUtils;
import heronarts.lx.transform.LXVector;

import com.symmetrylabs.layouts.dollywood.DollywoodModel;

import java.util.List;
import java.util.LinkedList;
import processing.core.PImage;
import java.util.Iterator;

import java.lang.Math;
import static processing.core.PApplet.*;
import static com.symmetrylabs.util.MathUtils.random;

public class TestSelectedWing extends SLPattern {

    private final List<DollywoodModel.Wing> wings;

    public final CompoundParameter selectedWingIndex = new CompoundParameter("Strp", 0, 0, ((DollywoodModel)model).getWings().size()-1);

    public TestSelectedWing(LX lx) {
        super(lx);
        this.wings = ((DollywoodModel)model).getWings();
        addParameter(selectedWingIndex);
    }

    public void run(double deltaMs) {
        setColors(0);

        DollywoodModel.Wing wing = wings.get((int)selectedWingIndex.getValue());

        for (LXPoint p : wing.points) {
            colors[p.index] = LXColor.RED;
        }
    }
}
