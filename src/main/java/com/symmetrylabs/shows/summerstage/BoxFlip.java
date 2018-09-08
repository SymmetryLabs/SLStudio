package com.symmetrylabs.shows.summerstage;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import com.symmetrylabs.util.NoiseUtils;
import heronarts.lx.LX;
import heronarts.lx.Tempo;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SquareLFO;
import heronarts.lx.parameter.*;
import heronarts.lx.LXPattern;
import heronarts.lx.LXUtils;
import heronarts.lx.transform.LXVector;

import java.util.List;
import java.util.LinkedList;
import processing.core.PImage;
import java.util.Iterator;

import java.lang.Math;
import static processing.core.PApplet.*;

import heronarts.lx.color.*;

import java.util.*;

import com.symmetrylabs.shows.summerstage.SummerStageShow;

public class BoxFlip extends SLPattern<CubesModel> implements Tempo.Listener {
    public static final String GROUP_NAME = SummerStageShow.SHOW_NAME;

    CompoundParameter hue1 = new CompoundParameter("hue1", 0, 0, 360);
    CompoundParameter hue2 = new CompoundParameter("hue2", 180, 0, 360);
    CompoundParameter sat1 = new CompoundParameter("sat1", 100, 0, 100);
    CompoundParameter sat2 = new CompoundParameter("sat2", 100, 0, 100);
    CompoundParameter yBorder = new CompoundParameter("yBorder", model.cy, model.yMin, model.yMax);
    CompoundParameter xBorder = new CompoundParameter("xBorder", model.cx, model.xMin, model.xMax);
    BooleanParameter trigger = new BooleanParameter("trigger", false);
    BooleanParameter useXAxis = new BooleanParameter("useX", false);
    BooleanParameter tempo = new BooleanParameter("tempo", false);
    BooleanParameter useOscillator = new BooleanParameter("useOscillator", false);
    DiscreteParameter oscillatorBPM = new DiscreteParameter("oscBPM", 250, 10, 1000);
    SquareLFO lfo = new SquareLFO(0, 1, 100000);




    float msAcc = 0;
    boolean flip = false;

    public BoxFlip(LX lx) {
        super(lx);

        addParameter(hue1);
        addParameter(hue2);
        addParameter(sat1);
        addParameter(sat2);
        addParameter(yBorder);
        addParameter(xBorder);

        trigger.setMode(BooleanParameter.Mode.MOMENTARY);
        addParameter(trigger);


        trigger.addListener(lxParameter -> {
                        if (trigger.isOn()) {
                                flip = !flip;
                        }
                });

        addParameter(useXAxis);
        addParameter(tempo);

        lx.tempo.addListener(this);

        addParameter(useOscillator);
        addParameter(oscillatorBPM);

        startModulator(lfo);
        updateLFOPeriod();

        oscillatorBPM.addListener(lxParameter -> {
            updateLFOPeriod();
        });


    }

    void updateLFOPeriod() {
        lfo.setPeriod(2 * (float)(60 * 1000) / oscillatorBPM.getValuef());
    }

    public void run(double deltaMs) {
        int c1 = LXColor.hsb(hue1.getValuef(), sat1.getValuef(), 100);
        int c2 = LXColor.hsb(hue2.getValuef(), sat2.getValuef(), 100);
        if (flip) {
            int temp = c2;
            c2 = c1;
            c1 = temp;
        }
        for (LXVector v : getVectorList()) {
            int c;
            if (useXAxis.isOn()) {
                c = v.x < xBorder.getValuef() ? c1 : c2;
            } else {
                c = v.y < yBorder.getValuef() ? c1 : c2;
            }
            colors[v.index] = c;
        }

        if (useOscillator.isOn()) {
            float v = lfo.getValuef();
            if (v == 1.0f && !flip) {
                flip = true;
            }
            if (v == 0.0f && flip) {
                flip = false;
            }
        }
    }

    @Override
    public void onBeat(Tempo tempo, int i) {
        if (this.tempo.isOn()) {
            flip = !flip;
        }
    }

    @Override
    public void onMeasure(Tempo tempo) {

    }
}

