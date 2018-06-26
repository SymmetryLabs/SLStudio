package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LXLayer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.LinearEnvelope;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.p3lx.P3LX;
import heronarts.lx.midi.MidiNote;

import processing.event.KeyEvent;

import java.util.*;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.Utils.millis;

public class Pads extends SLPattern<CubesModel> {

    private boolean debug = false;
    float counter = 0;
    int count = 0;
    CompoundParameter cutoff = new CompoundParameter("cutoff", 200, 0, 500);
    CompoundParameter sweepSpeed = new CompoundParameter("speed", 1500, 500, 3000);
    CompoundParameter falloffParam = new CompoundParameter("falloff", 5, .5, 20);
    private final Stack<LXLayer> newLayers = new Stack<LXLayer>();
    private final Stack<LXLayer> sweepUp = new Stack<LXLayer>();
    private final Stack<LXLayer> sweepDown = new Stack<LXLayer>();
    private final Map<Integer, LightUp> lightMap = new HashMap<Integer, LightUp>();
    private final List<LightUp> lights = new ArrayList<LightUp>();
    private final CompoundParameter lightSize = new CompoundParameter("SIZE", 1, .5, 2);

    private final List<Sweep> sweeps = new ArrayList<Sweep>();

    private final LinearEnvelope sparkle = new LinearEnvelope(0, 1, 500);
    private boolean sparkleDirection = true;
    private float sparkleBright = 100;
    enum Direction {DOWN, UP, LEFT, RIGHT, BACK, FORWARD};

    private final CompoundParameter wave = new CompoundParameter("WAVE", 0);

    Pads(P3LX lx) {
        super(lx);
        addParameter(lightSize);
        addParameter(wave);
        addParameter(falloffParam);
        addModulator(sparkle).setValue(1);

        lx.addKeyEventHandler(this);
    }

    void onReset() {
        for (LightUp light : lights) {
            light.noteOff(null);
        }
    }

    public class Sweep extends LXLayer {
        public com.symmetrylabs.slstudio.pattern.Pads.Direction direction;
        LinearEnvelope position = new LinearEnvelope(0, 1, 1500);
        LXProjection projection = new LXProjection(model);
        float bright = 100;
        float falloff = 10;


        public Sweep(Direction whichWay) {
            super(Pads.this.lx);
            this.direction = whichWay;
            addModulator(position);
        }

        public void run(double deltaMs) {
            if (!position.isRunning()) {
                return;
            }
            float timer = millis();
            //projection.reset()
            //.center()
            //.rotate()
            falloff = falloffParam.getValuef();
            float posf = position.getValuef();
            switch (direction) {
                case UP:
                    for (LXPoint p : model.points) {
                        colors[p.index] = lx.hsb(
                            palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                            100,
                            max(0, bright - posf*100 - falloff*abs(p.y - posf*model.yMax))
                        );
                    }
                    break;
                case DOWN:
                    for (LXPoint p : model.points) {
                        colors[p.index] =lx.hsb(
                            palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                            100,
                            max(0, bright - posf*100 - falloff*abs(p.y -  (model.yMax - posf*model.yMax)))
                        );
                    }
                    break;
                case RIGHT:
                    for (LXPoint p : model.points) {
                        colors[p.index] =lx.hsb(
                            palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                            100,
                            max(0, bright - posf*100 - falloff*abs(p.x - posf*model.xMax))
                        );
                    }
                    break;
                case LEFT:
                    for (LXPoint p : model.points) {
                        colors[p.index] =lx.hsb(
                            palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                            100,
                            max(0, bright - posf*100 - falloff*abs(p.x -  (model.xMax - posf*model.xMax)))
                        );
                    }
                    break;
                case FORWARD:
                    for (LXPoint p : model.points) {
                        colors[p.index] =lx.hsb(
                            palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                            100,
                            max(0, bright - posf*100 - falloff*abs(p.z - posf*model.zMax))
                        );
                    }
                    break;
                case BACK:
                    for (LXPoint p : model.points) {
                        colors[p.index] =lx.hsb(
                            palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                            100,
                            max(0, bright - posf*100 - falloff*abs(p.z -  (model.zMax - posf*model.zMax)))
                        );
                    }
                    break;

            }
            timer = millis() - timer;
            if (debug && (millis() % 100) < 20) System.out.println("timer on Sweep: "  + timer);
        }

    }

    class LightUp extends LXLayer {

        private final LinearEnvelope brt = new LinearEnvelope(0, 0, 0);
        private final Accelerator yPos = new Accelerator(0, 0, 0);
        private float xPos;
        private float lifetime = 0;

        LightUp() {
            super(Pads.this.lx);
            addModulator(brt);
            addModulator(yPos);

        }

        boolean isAvailable() {
            return brt.getValuef() <= 0;
        }

        void noteOn(MidiNote note) {
            if (debug) System.out.println("AlexPlay noteOn called: "  + note.getPitch());
            xPos = lerp(0, model.xMax, constrain(0.5f + (note.getPitch() - 60) / 40f, 0, 1));
            yPos.setValue(lerp(20, model.yMax*.85f, note.getVelocity() / 127f)).stop();
            brt.setRangeFromHereTo(lerp(80, 100, note.getVelocity() / 127f), 20).start();
        }

        void noteOff(MidiNote note) {
            if (debug) System.out.println("AlexPlay with noteOff called: " + note.getPitch());
            yPos.setVelocity(0).setAcceleration(-380).start();
            brt.setRangeFromHereTo(0, 1000).start();
        }

        public void run(double deltaMs) {
            float time = millis();
            lifetime += deltaMs;
            if (lifetime > 5000) {this.noteOff(null);}
            //{this.noteOff(new MidiNoteOff(new ShortMessage(MidiNote.NOTE_OFF, 1, 40)));}
            //new MidiNoteOff(new ShortMessage(MidiNote.NOTE_OFF, nm.channel, nm.number + octaveShift*12)), 0)
            float bVal = brt.getValuef();
            if (bVal <= 0) {
                return;
            }
            float yVal = yPos.getValuef();
            for (LXPoint p : model.points) {
                float falloff = 6 - 5*lightSize.getValuef();
                float b = max(0, bVal - falloff*dist(p.x, p.y, xPos, yVal));
                if (b > 0) {
                    colors[p.index] = lx.hsb(
                        palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                        100,
                        b
                    );
                }
            }
            time += millis();
            if (debug && (millis()%100) <50) System.out.println("time = " + time );
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
    private Sweep getSweep(Direction whichWay) {
        for (Sweep s : sweeps) {
            if (!s.position.isRunning()) {
                //s.Direction.whichWay;
                return s;
            }
        }
        Sweep newSweep = new Sweep(whichWay);
        sweeps.add(newSweep);
        synchronized(newLayers) {
            newLayers.push(newSweep);
        }
        return newSweep;
    }


    private Sweep getSweepUp() {
        for (Sweep s : sweeps) {
            if (!s.position.isRunning()) {
                return s;
            }
        }
        Sweep newSweep = new Sweep(Direction.UP);
        sweeps.add(newSweep);
        synchronized(newLayers) {
            newLayers.push(newSweep);
        }
        return newSweep;
    }
    private Sweep getSweepDown() {
        for (Sweep s : sweeps) {
            if (!s.position.isRunning()) {
                return s;
            }
        }
        Sweep newSweep = new Sweep(Direction.DOWN);
        sweeps.add(newSweep);
        synchronized(newLayers) {
            newLayers.push(newSweep);
        }
        return newSweep;
    }
    public void keyEvent(KeyEvent key) {
        if (!(keyEvent.getAction() == KeyEvent.PRESS)) {return;}
        else switch(key.getKey()) {
            case 'r':
                this.reset();
                break;
            case 'U':
                Sweep s = getSweep(Direction.UP);
                s.direction = com.symmetrylabs.slstudio.pattern.Pads.Direction.UP;
                s.bright = 50 + 127/ 127f * 50;
                s.falloff = 20 - 127 / 127f * 17;
                s.position.trigger();
                count++;
                System.out.println("Sweep Count:  " +   count);
                break;
            case 'D':
                Sweep _s = getSweep(Direction.DOWN);
                _s.direction = Direction.DOWN;
                _s.bright = 50f + 127f / 127f * 50f;
                _s.falloff = 20f - 127f / 127f * 17f;
                _s.position.trigger();
                break;
            case 'R':
                Sweep r = getSweep(Direction.RIGHT);
                r.direction = Direction.RIGHT;
                r.bright = 50 + 127 / 127f * 50;
                r.falloff = 20 - 127 / 127f * 17;
                r.position.trigger();
                break;
            case 'L':
                Sweep l = getSweep(Direction.LEFT);
                l.direction = Direction.LEFT;
                l.bright = 50 + 127 / 127f * 50;
                l.falloff = 20 - 127 / 127f * 17;
                l.position.trigger();
                break;
            case 'F':
                Sweep f = getSweep(Direction.FORWARD);
                f.direction = Direction.FORWARD;
                f.bright = 50 + 127 / 127f * 50;
                f.falloff = 20 - 127 / 127f * 17;
                f.position.trigger();
                break;

            case 'B':
                Sweep b = getSweep(Direction.BACK);
                b.direction = Direction.BACK;
                b.bright = 50f + 127f / 127f * 50;
                b.falloff = 20 - 127 / 127f * 17;
                b.position.trigger();
                break;
            case 'S':

                sparkleBright = 100;
                sparkleDirection = true;
                sparkle.trigger();
                break;
            case 'Q':

                sparkleBright = 100;
                sparkleDirection = false;
                sparkle.trigger();
                break;
        }
    }

    public synchronized boolean noteOn(MidiNote note) {
        // if (debug) {println("AlexPlay noteOn called with Note:" + note.getPitch());}
        if ((note.getChannel() == 0) && (note.getPitch() <= 62)) {
            if (counter < cutoff.getValuef()) {return false;}
            LightUp light = getLight();
            lightMap.put(note.getPitch(), light);
            light.noteOn(note);
        }
        else {
            if (note.getVelocity() > 0) {
                switch (note.getPitch()) {
                    case 63:
                        Sweep up = getSweep(Direction.UP);
                        up.bright = 50 + note.getVelocity() / 127f * 50;
                        up.falloff = 20 - note.getVelocity() / 127f * 15;
                        up.position.trigger();
                        break;
                    case 64:
                        Sweep down = getSweep(Direction.DOWN);
                        down.bright = 50 + note.getVelocity() / 127f * 50;
                        down.falloff = 20 - note.getVelocity() / 127f * 17;
                        down.position.trigger();
                    case 65:
                        Sweep right = getSweep(Direction.RIGHT);
                        right.bright = 50 + note.getVelocity() / 127f * 50;
                        right.falloff = 20 - note.getVelocity() / 127f * 17;
                        right.position.trigger();
                        break;
                    case 66:
                        Sweep left = getSweep(Direction.LEFT);
                        left.bright = 50 + note.getVelocity() / 127f * 50;
                        left.falloff = 20 - note.getVelocity() / 127f * 17;
                        left.position.trigger();
                        break;
                    case 67:
                        Sweep back = getSweep(Direction.BACK);
                        back.bright = 50 + note.getVelocity() / 127f * 50;
                        back.falloff = 20 - note.getVelocity() / 127f * 17;
                        back.position.trigger();
                        break;
                    case 68:
                        Sweep forward = getSweep(Direction.FORWARD);
                        forward.bright = 50 + note.getVelocity() / 127f * 50;
                        forward.falloff = 20 - note.getVelocity() / 127f * 17;
                        forward.position.trigger();
                        break;
                    case 69:
                        //println("sparklebright called:" + note.getPitch());
                        sparkleBright = note.getVelocity() / 127f * 200;
                        sparkleDirection = true;
                        sparkle.trigger();
                        break;
                    case 70:
                        //println("sparklebright called:" + note.getPitch());
                        sparkleBright = note.getVelocity() / 127f * 200;
                        sparkleDirection = false;
                        sparkle.trigger();
                        break;
                    case 40:
                        effects.boom.trigger();
                        break;
                    case 41:
                        effects.flash.trigger();
                        break;
                }
            }
        }
        counter = 0;
        return true;
    }

    public synchronized boolean noteOff(MidiNote note) {
        if (note.getChannel() == 0) {
            LightUp light = lightMap.get(note.getPitch());
            if (light != null) {
                light.noteOff(note);
            }
        }
        return true;
    }

    final float[] wval = new float[16];
    float wavoff = 0;

    public synchronized void run(double deltaMs) {
        counter += millis();
        wavoff += deltaMs * .001;
        for (int i = 0; i < wval.length; ++i) {
            wval[i] = model.cy + 0.2f * model.yMax/2f * sin((float)(wavoff + i / 1.9f));
        }
        float sparklePos = (sparkleDirection ? sparkle.getValuef() : (1f - sparkle.getValuef())) * (CubesModel.Cube.Type.LARGE.POINTS_PER_STRIP)/2f;
        float maxBright = sparkleBright * (1 - sparkle.getValuef());
        for (Strip s : model.getStrips()) {
            int i = 0;
            for (LXPoint p : s.points) {
                int wavi = (int) constrain(p.x / model.xMax * wval.length, 0, wval.length-1);
                float wavb = max(0, wave.getValuef()*100f - 8f*abs(p.y - wval[wavi]));
                colors[p.index] =  lx.hsb(
                    palette.getHuef() + .2f*abs(p.x - model.cx) + .2f*abs(p.y - model.cy),
                    100,
                    constrain(wavb + max(0, (float)(maxBright - 40.*abs(sparklePos - abs(i - (CubesModel.Cube.Type.LARGE.POINTS_PER_STRIP-1f)/2f)))), 0, 100));
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

