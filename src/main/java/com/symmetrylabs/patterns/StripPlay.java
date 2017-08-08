package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.audio.*;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.CubesModel;
import com.symmetrylabs.util.MathUtils;

public class StripPlay extends CubesPattern {
    private final int NUM_OSC = 300;
    private final int MAX_PERIOD = 20000;
    private CompoundParameter brightParameter = new CompoundParameter("bright", 96, 70, 100);
    private CompoundParameter hueSpread = new CompoundParameter("hueSpread", 1);
    private CompoundParameter speed = new CompoundParameter("speed", .5);
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

            for (int i=0;i<NUM_OSC;i++) {
                fX[i] = new SinLFO(model.xMin, model.xMax, MathUtils.random(2000, MAX_PERIOD));
                fY[i] = new SinLFO(model.yMin, model.yMax, MathUtils.random(2000, MAX_PERIOD));
                fZ[i] = new SinLFO(model.zMin, model.zMax, MathUtils.random(2000, MAX_PERIOD));
                sat[i] = new SinLFO(80, 100, MathUtils.random(2000, 5000));
                addModulator(fX[i]).trigger();
                addModulator(fY[i]).trigger();
                addModulator(fZ[i]).trigger();
                colorOffset[i]= (float)Math.sin(MathUtils.random(-(float)Math.PI, (float)Math.PI)) * 40;
            }
    }

    public void onParameterChanged(LXParameter parameter) {
        if (parameter == xSpeed) {
            for (int i = 0; i < NUM_OSC; i++) {
                fX[i].setPeriod(MAX_PERIOD + 1 - xSpeed.getValue());
            }
        } else if (parameter == ySpeed) {
            for (int i = 0; i < NUM_OSC; i++) {
                fY[i].setPeriod(MAX_PERIOD + 1  - ySpeed.getValue());
            }
        } else if (parameter == zSpeed) {
            for (int i = 0; i < NUM_OSC; i++) {
                fZ[i].setPeriod(MAX_PERIOD + 1  - zSpeed.getValue());
            }
        }  else if (parameter == hueSpread) {
            for (int i = 0; i < NUM_OSC; i++) {
                colorOffset[i] = colorOffset[i]*hueSpread.getValuef();
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        setColors(0);
        float[] bright = new float[model.points.length];
        for (CubesModel.Strip strip : model.strips) {
            LXPoint centerPoint = strip.points[8];
            for (int i=0;i<numOsc.getValue();i++) {
                float avgdist = MathUtils.dist(centerPoint.x,centerPoint.y,centerPoint.z,fX[i].getValuef(),fY[i].getValuef(),fZ[i].getValuef());
                boolean on = avgdist<30;
                if (on) {
                    float hv = palette.getHuef()+colorOffset[i];
                    float br = Math.max(0,100-avgdist*2*(100 - brightParameter.getValuef()));
                    int colr = lx.hsb(hv, sat[i].getValuef(), br);
                    for (LXPoint p : strip.points) {
                        if (br>bright[p.index]) {
                            //colors[p.index] = lx.hsb(hv,sat[i].getValuef(),br);
                            addColor(p.index, colr);
                            bright[p.index] = br;
                        }
                    }
                }
            }
        }
    }
}
