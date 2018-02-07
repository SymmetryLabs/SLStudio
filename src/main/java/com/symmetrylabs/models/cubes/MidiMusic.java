package com.symmetrylabs.models.cubes;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.lang.Math;

import java.util.Stack;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.LXLayer;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;

import com.symmetrylabs.slstudio.model.Strip;

public class MidiMusic extends SLPattern {

    private final Stack<LXLayer> newLayers = new Stack<LXLayer>();

    private final Map<Integer, LightUp> lightMap = new HashMap<Integer, LightUp>();
    private final List<LightUp> lights = new ArrayList<LightUp>();
    private final CompoundParameter lightSize = new CompoundParameter("SIZE", 0.5);

    private final List<Sweep> sweeps = new ArrayList<Sweep>();

    private final LinearEnvelope sparkle = new LinearEnvelope(0, 1, 500);
    private boolean sparkleDirection = true;
    private float sparkleBright = 100;

    private final CompoundParameter wave = new CompoundParameter("WAVE", 0);
    private final BooleanParameter triggerSweep = new BooleanParameter("SWEEP", false);

    public MidiMusic(LX lx) {
        super(lx);
        addModulator(sparkle).setValue(1);
        addParameter(lightSize);
        addParameter(wave);
        addParameter(triggerSweep);

        triggerSweep.setMode(BooleanParameter.Mode.MOMENTARY);
        triggerSweep.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (((BooleanParameter)parameter).isOn()) {
                    float velocity = 135.f;
                    Sweep s = getSweep();
                    s.bright = 50 + velocity / 127.f * 50;
                    s.falloff = 20 - velocity / 127.f * 17;
                    s.position.trigger();
                }
            }
        });
    }

    void onReset() {
        for (LightUp light : lights) {
            light.noteOff(null);
        }
    }

    private class Sweep extends LXLayer {

        final LinearEnvelope position = new LinearEnvelope(0, 1, 1000);
        float bright = 100;
        float falloff = 10;

        Sweep() {
            super(MidiMusic.this.lx, MidiMusic.this);
            addModulator(position);
        }

        public void run(double deltaMs) {
            if (!position.isRunning()) {
                return;
            }
            float posf = position.getValuef();
            for (LXPoint p : model.points) {
                blendColor(p.index, lx.hsb(
                    palette.getHuef() + 0.2f*Math.abs(p.x - model.cx) + 0.2f*Math.abs(p.y - model.cy),
                    100,
                    Math.max(0, bright - posf*100 - falloff*Math.abs(p.y - posf*model.yMax))
                ), LXColor.Blend.ADD);
            }
        }
    }

    private class LightUp extends LXLayer {

        private final LinearEnvelope brt = new LinearEnvelope(0, 0, 0);
        private final Accelerator yPos = new Accelerator(0, 0, 0);
        private float xPos;

        LightUp() {
            super(MidiMusic.this.lx, MidiMusic.this);
            addModulator(brt);
            addModulator(yPos);
        }

        boolean isAvailable() {
            return brt.getValuef() <= 0;
        }

        void noteOn(MidiNote note) {
            xPos = model.xMin + ((note.getPitch() / 30.f) * model.xRange);
            yPos.setValue(LXUtils.lerpf(20, model.yMax*0.72f, note.getVelocity() / 127.f)).stop();
            brt.setRangeFromHereTo(LXUtils.lerpf(60, 100, note.getVelocity() / 127.f), 20).start();
        }

        void noteOff(MidiNote note) {
            yPos.setVelocity(0).setAcceleration(-380).start();
            brt.setRangeFromHereTo(0, 1000).start();
        }

        public void run(double deltaMs) {
            float bVal = brt.getValuef();
            if (bVal <= 0) {
                return;
            }
            float yVal = yPos.getValuef();
            for (LXPoint p : model.points) {
                float falloff = 6 - 7*lightSize.getValuef();
                float b = (float)Math.max(0, bVal - falloff*LXUtils.distance(p.x, p.y, xPos, yVal));
                if (b > 0) {
                    blendColor(p.index, lx.hsb(
                        palette.getHuef() + 0.2f*Math.abs(p.x - model.cx) + 0.2f*Math.abs(p.y - model.cy),
                        100,
                        b
                    ), LXColor.Blend.ADD);
                }
            }
        }
    }

    private LightUp getLight() {
        for (LightUp light : lights) {
            if (light.isAvailable()) {
                return light;
            }
        }
        LightUp newLight = new LightUp();
        lights.add(newLight);
        synchronized(newLayers) {
            newLayers.push(newLight);
        }
        return newLight;
    }

    private Sweep getSweep() {
        for (Sweep s : sweeps) {
            if (!s.position.isRunning()) {
                return s;
            }
        }
        Sweep newSweep = new Sweep();
        sweeps.add(newSweep);
        synchronized(newLayers) {
            newLayers.push(newSweep);
        }
        return newSweep;
    }

    public void noteOnReceived(MidiNoteOn note) {
        if (note.getPitch() < 40) {
            LightUp light = getLight();
            lightMap.put(note.getPitch(), light);
            light.noteOn(note);
        }
         else {
            if (note.getVelocity() > 0) {
                switch (note.getPitch()) {
                    case 41:
                        Sweep s = getSweep();
                        s.bright = 50 + note.getVelocity() / 127.f * 50;
                        s.falloff = 20 - note.getVelocity() / 127.f * 17;
                        s.position.trigger();
                        break;
                    case 42:
                        sparkleBright = note.getVelocity() / 127.f * 100;
                        sparkleDirection = true;
                        sparkle.trigger();
                        break;
                    case 43:
                        sparkleBright = note.getVelocity() / 127.f * 100;
                        sparkleDirection = false;
                        sparkle.trigger();
                        break;
                    case 44:
                        //effects.boom.trigger();
                        break;
                    case 45:
                        //effects.flash.trigger();
                        break;
                }
            }
        }
        //return true;
    }

 // public synchronized boolean noteOff(MidiNote note) {

    public void noteOffReceived(MidiNote note) {
        //if (note.getChannel() == 0) {
            LightUp light = lightMap.get(note.getPitch());
            if (light != null) {
                light.noteOff(note);
            }
        //}
        //return true;
    }

    final float[] wval = new float[16];
    float wavoff = 0;

    public synchronized void run(double deltaMs) {
        wavoff += deltaMs * .001;
        for (int i = 0; i < wval.length; ++i) {
            wval[i] = model.cy + 0.2f * model.yMax/2.0f * (float)Math.sin(wavoff + i / 1.9f);
        }
        for (Strip s : ((CubesModel)model).getStrips()) {
            float sparklePos = (sparkleDirection ? sparkle.getValuef() : (1 - sparkle.getValuef())) * (s.points.length)/2.f;
            float maxBright = sparkleBright * (1 - sparkle.getValuef());

            int i = 0;
            for (LXPoint p : s.points) {
                int wavi = (int)LXUtils.constrain(p.x / model.xMax * wval.length, 0, wval.length-1);
                float wavb = (float)Math.max(0, wave.getValuef()*100. - 8.*Math.abs(p.y - wval[wavi]));
                colors[p.index] = lx.hsb(
                    palette.getHuef() + 0.2f*Math.abs(p.x - model.cx) + 0.2f*Math.abs(p.y - model.cy),
                    100,
                    (float)LXUtils.constrain(wavb + Math.max(0, maxBright - 40.*Math.abs(sparklePos - Math.abs(i - (s.points.length-1)/2.f))), 0, 100)
                );
                ++i;
            }
        }

        if (!newLayers.isEmpty()) {
            synchronized(newLayers) {
                while (!newLayers.isEmpty()) {
                    addLayer(newLayers.pop());
                }
            }
        }
    }
}
