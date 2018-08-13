package com.symmetrylabs.shows.cubes.patterns;

import static processing.core.PApplet.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;

import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.random;

public class StripPlay extends SLPattern<CubesModel> {
    private final int NUM_OSC = 300;
    private final int MAX_PERIOD = 20000;
    private CompoundParameter brightParameter = new CompoundParameter("Bright", 96, 70, 100);
    private CompoundParameter hueSpread = new CompoundParameter("HueVar", 1);
    private CompoundParameter speed = new CompoundParameter("Speed", .5);
    private CompoundParameter xSpeed = new CompoundParameter("xSpeed", 2000, 500, MAX_PERIOD);
    private CompoundParameter ySpeed = new CompoundParameter("ySpeed", 1600, 500, MAX_PERIOD);
    private CompoundParameter zSpeed = new CompoundParameter("zSpeed", 1000, 500, MAX_PERIOD);
    private DiscreteParameter numOsc = new DiscreteParameter("Strips", 179, 1, NUM_OSC);


    SinLFO[] fX = new SinLFO[NUM_OSC]; //new SinLFO(0, model.xMax, 5000);
    SinLFO[] fY = new SinLFO[NUM_OSC]; //new SinLFO(0, model.yMax, 4000);
    SinLFO[] fZ = new SinLFO[NUM_OSC]; //new SinLFO(0, model.yMax, 3000);
    SinLFO[] sat = new SinLFO[NUM_OSC];
    float[] colorOffset = new float[NUM_OSC];

    public StripPlay(LX lx) {
        super(lx);
        addParameter(brightParameter);
        addParameter(numOsc);
        addParameter(hueSpread);
        addParameter(speed);
        addParameter(xSpeed);
        addParameter(ySpeed);
        addParameter(zSpeed);

        for (int i = 0; i < NUM_OSC; i++) {
            fX[i] = new SinLFO(model.xMin, model.xMax, random(2000, MAX_PERIOD));
            fY[i] = new SinLFO(model.yMin, model.yMax, random(2000, MAX_PERIOD));
            fZ[i] = new SinLFO(model.zMin, model.zMax, random(2000, MAX_PERIOD));
            sat[i] = new SinLFO(80, 100, random(2000, 5000));
            addModulator(fX[i]).trigger();
            addModulator(fY[i]).trigger();
            addModulator(fZ[i]).trigger();
            colorOffset[i] = sin(random(-PI, PI)) * 40;
        }
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == xSpeed) {
            for (int i = 0; i < NUM_OSC; i++) {
                fX[i].setPeriod(MAX_PERIOD + 1 - xSpeed.getValue());
            }
        } else if (parameter == ySpeed) {
            for (int i = 0; i < NUM_OSC; i++) {
                fY[i].setPeriod(MAX_PERIOD + 1 - ySpeed.getValue());
            }
        } else if (parameter == zSpeed) {
            for (int i = 0; i < NUM_OSC; i++) {
                fZ[i].setPeriod(MAX_PERIOD + 1 - zSpeed.getValue());
            }
        } else if (parameter == hueSpread) {
            for (int i = 0; i < NUM_OSC; i++) {
                colorOffset[i] = colorOffset[i] * hueSpread.getValuef();
            }
        }
    }

    public void run(double deltaMs) {
        setColors(0xff000000);

        float[] bright = new float[model.points.length];
        for (Strip strip : model.getStrips()) {
            LXPoint centerPoint = strip.points[8];
            for (int i = 0; i < numOsc.getValue(); i++) {
                float avgdist =
                    dist(centerPoint.x, centerPoint.y, centerPoint.z, fX[i].getValuef(), fY[i].getValuef(), fZ[i].getValuef());
                boolean on = avgdist < 30;
                if (on) {
                    float hv = palette.getHuef() + colorOffset[i];
                    float br = max(0, 100 - avgdist * 2 * (100 - brightParameter.getValuef()));
                    int colr = lx.hsb(hv, sat[i].getValuef(), br);
                    for (LXVector v : getVectors(strip.points)) {
                        if (br > bright[v.index]) {
                            //colors[p.index] = lx.hsb(hv,sat[i].getValuef(),br);
                            addColor(v.index, colr);
                            bright[v.index] = br;
                        }
                    }
                }
            }
        }
    }
}
