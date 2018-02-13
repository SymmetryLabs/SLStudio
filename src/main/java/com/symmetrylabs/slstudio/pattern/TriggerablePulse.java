package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

import processing.core.PVector;

import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.modulator.LinearEnvelope;

import com.symmetrylabs.slstudio.pattern.base.DPat;

public class TriggerablePulse extends DPat {

    public final CompoundParameter size = new CompoundParameter("size", model.xRange*0.1, model.xRange*0.01, model.xRange*0.5);
    public final CompoundParameter speed = new CompoundParameter("speed", 1000, 200, 5000);
    public final BooleanParameter trigger = new BooleanParameter("trig", false);

    private final CompoundParameter position = new CompoundParameter("pos", 0.5, 0, 1);
    private final LinearEnvelope positionEnvelope = new LinearEnvelope("xPosEnv", 0, 1, speed);

    float lastTrigger = 0;
    float elapsedTime = 0;
    float period = 0;
    float actualxPos = 0;
    float xSize = 0;
    float xStart = 0;
    float xRange = 0;
    float xPos = 0;

    public TriggerablePulse(LX lx) {
        super(lx);
        addParameter(size);
        addParameter(speed);
        addParameter(trigger);
        addModulator(positionEnvelope);
        positionEnvelope.setLooping(false);

        trigger.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter param) {
                if (((BooleanParameter)param).isOn()) {
                    triggerPulse();
                }
            }
        });
    }

    private void triggerPulse() {
        positionEnvelope.trigger();
        lastTrigger = elapsedTime;
    }

    public void StartRun(double deltaMs) {
        elapsedTime += deltaMs;

        xSize = size.getValuef();
        xStart = model.xMin - xSize;
        xRange = model.xRange + xSize*2;
        xPos = -xSize;
    }

    public int CalcPoint(PVector p) {
        if (positionEnvelope.running.isOn()) {

            xPos = xStart + (xRange * positionEnvelope.getValuef());
            float distance = Math.abs(p.x - xPos);

            if (distance < xSize) {
                float brightness = 100 - (distance / xSize * 100);
                return lx.hsb(palette.getHuef(), 100, brightness);
            } else {
                return LXColor.BLACK;
            }
        }
        return LXColor.BLACK;
    }
}
