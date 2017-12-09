package com.symmetrylabs.ping;

import com.symmetrylabs.model.Sun;
import com.symmetrylabs.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.println;


public class RipplePads extends SLPattern {
    CompoundParameter intensity = new CompoundParameter("intensity", 1, 0, 3);
    CompoundParameter velocity = new CompoundParameter("velocity", 80, 0, 127);
    CompoundParameter speed = new CompoundParameter("speed", 200, 0, 500);
    CompoundParameter decaySec = new CompoundParameter("decaySec", 1, 0, 10);
    CompoundParameter nextHue = new CompoundParameter("nextHue", 0, 0, 360);
    CompoundParameter nextSat = new CompoundParameter("nextSat", 0, 0, 1);

    String[] sunIds = {"sun3", "sun5", "sun8", "sun11", "sun10", "sun9", "sun7", "sun6", "sun4", "sun1", "sun2"};
    String[] buttonNames = {"K", "J", "I", "H", "G", "F", "E", "D", "C", "B", "A"};
    int[] buttonPitches = {60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77};

    BooleanParameter[] buttons;
    boolean[] lastState;

    Sun[] sunsByPitch = new Sun[128];
    Ripple[] lastRipple = new Ripple[128];
    List<Ripple> ripples = new ArrayList<Ripple>();

    public RipplePads(LX lx) {
        super(lx);

        addParameter(intensity);
        addParameter(velocity);
        addParameter(speed);
        addParameter(decaySec);
        addParameter(nextHue);
        addParameter(nextSat);


        buttons = new BooleanParameter[sunIds.length];
        lastState = new boolean[sunIds.length];

        for (int i = 0; i < sunIds.length; i++) {
            sunsByPitch[buttonPitches[i]] = model.sunTable.get(sunIds[i]);
            BooleanParameter param = new BooleanParameter(buttonNames[i]);
            param.setMode(BooleanParameter.Mode.MOMENTARY);
            addParameter(param);
            buttons[i] = param;
        }
    }

    public void run(double deltaMs) {
        float deltaSec = (float) deltaMs * 0.001f;

        for (int b = 0; b < buttons.length; b++) {
            boolean state = buttons[b].isOn();
            if (state != lastState[b]) {
                if (state) {
                    noteOn(buttonPitches[b], (int) velocity.getValuef());
                } else {
                    noteOff(buttonPitches[b]);
                }
                lastState[b] = state;
            }
        }

        List<Ripple> expired = new ArrayList<Ripple>();
        for (Ripple ripple : ripples) {
            ripple.advance(deltaSec);
            if (ripple.isExpired()) {
                expired.add(ripple);
            }
        }
        ripples.removeAll(expired);

        for (Sun sun : sunsByPitch) {
            if (sun == null) continue;
            for (int i = 0; i < sun.points.length; i++) {
                LXPoint point = sun.points[i];
                int sum = 0xff000000;
                for (Ripple ripple : ripples) {
                    if (ripple.sun == sun) {
                        sum = LXColor.add(sum, ripple.getColor(sun.distances[i]));
                    }
                }
                colors[point.index] = sum;
            }
        }
    }

    public void noteOnReceived(MidiNoteOn note) {
        println("note on " + note);
        noteOn(note.getPitch(), note.getVelocity());
    }

    public void noteOffReceived(MidiNote note) {
        println("note off " + note);
        noteOff(note.getPitch());
    }

    void noteOn(int pitch, int velocity) {
        if (pitch > 127) return;
        Sun sun = sunsByPitch[pitch];
        if (sun != null) {
            if (lastRipple[pitch] != null) {
                lastRipple[pitch].release();
            }
            lastRipple[pitch] = addRipple(sun, velocity / 128f);
        }
    }

    void noteOff(int pitch) {
        if (pitch > 127) return;
        if (lastRipple[pitch] != null) {
            lastRipple[pitch].release();
        }
        lastRipple[pitch] = null;
    }

    Ripple addRipple(Sun sun, float velocity) {
        Ripple ripple = new Ripple(
            sun, sun.boundingBox.size.z / 2,
            intensity.getValuef() * velocity, speed.getValuef() * velocity,
            decaySec.getValuef(), nextHue.getValuef(), nextSat.getValuef()
        );
        ripples.add(ripple);
        return ripple;
    }

    class Ripple {
        Sun sun;
        float intensity;
        float speed;
        float decaySec;
        float hue;
        float sat;

        float ageSec;
        float radius;
        float value;
        int[] layerColors;
        boolean held;

        Ripple(Sun sun, float radius, float intensity, float speed, float decaySec, float hue, float sat) {
            this.sun = sun;
            this.radius = radius;
            this.intensity = intensity;
            this.speed = speed;
            this.decaySec = decaySec;
            this.hue = hue;
            this.sat = sat;
            this.held = true;
            ageSec = 0;
        }

        void advance(float deltaSec) {
            if (!held) ageSec += deltaSec;
            radius += deltaSec * speed;
            value = intensity / (1f + 10f * ageSec / decaySec);
        }

        void release() {
            held = false;
        }

        int getColor(float distance) {
            if (distance < radius) {
                float brightness = value > 1 ? 1 : value;
                return lx.hsb(hue, sat * 100f, brightness * 100f);
            } else {
                return 0;
            }
        }

        boolean isExpired() {
            return ageSec > decaySec * 2;
        }
    }
}
