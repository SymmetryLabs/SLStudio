package com.symmetrylabs.slstudio.pattern.ping;

import java.util.List;
import java.util.ArrayList;

import processing.core.PVector;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.model.nissan.NissanModel;
import com.symmetrylabs.slstudio.model.nissan.NissanCar;
import com.symmetrylabs.slstudio.model.nissan.NissanWindow;
import com.symmetrylabs.slstudio.pattern.NissanPattern;
import com.symmetrylabs.slstudio.util.BlobTracker;
import com.symmetrylabs.slstudio.util.MathUtils;

import static com.symmetrylabs.slstudio.util.MathUtils.random;
import static processing.core.PApplet.println;

public class TouchRipples extends NissanPattern {
    private BlobTracker blobTracker;

    CompoundParameter initialRadius = new CompoundParameter("radius", 4, 1, 30);
    CompoundParameter intensity = new CompoundParameter("intensity", 1, 0, 3);
    CompoundParameter velocity = new CompoundParameter("velocity", 7, 0, 127);
    CompoundParameter speed = new CompoundParameter("speed", 25, 0, 500);
    CompoundParameter decaySec = new CompoundParameter("decaySec", 1.8, 0, 10);
    CompoundParameter nextHue = new CompoundParameter("nextHue", 0, 0, 360);
    CompoundParameter hueVariance = new CompoundParameter("hueVar", 180, 0, 360);
    CompoundParameter nextSat = new CompoundParameter("nextSat", 1, 0, 1);

    BooleanParameter trigger = new BooleanParameter("trigger");
    Ripple triggerRipple = null;
    boolean lastTriggerState;

    List<Ripple> ripples = new ArrayList<Ripple>();

    public TouchRipples(LX lx) {
        super(lx);
        this.blobTracker = BlobTracker.getInstance(lx);

        addParameter(initialRadius);
        addParameter(intensity);
        addParameter(velocity);
        addParameter(speed);
        addParameter(decaySec);
        addParameter(nextHue);
        addParameter(hueVariance);
        addParameter(nextSat);

        // test
        addParameter(trigger);
        trigger.setMode(BooleanParameter.Mode.MOMENTARY);
        lastTriggerState = false;
    }

    private Ripple createRipple(BlobTracker.Blob blob) {
        Ripple ripple = new Ripple(
            blob,
            initialRadius.getValuef(),
            intensity.getValuef(),
            speed.getValuef(),
            decaySec.getValuef(),
            nextHue.getValuef(),
            nextSat.getValuef()
        );
        ripples.add(ripple);
        return ripple;
    }

    public void run(double deltaMs) {
        float deltaSec = (float)deltaMs * 0.001f;

        // if (triggerRipple == null) {
        //   System.out.println("no ripples");
        // } else {
        //   System.out.println(triggerRipple.pos.x + ", " + triggerRipple.pos.y);
        // }

        // create new ripples
        for (BlobTracker.Blob blob : blobTracker.getBlobs()) {
            boolean skip = false;
            for (Ripple ripple : ripples) {
                if (ripple.matchesBlob(blob)) {
                    skip = true;
                    continue;
                }
            }
            if (skip) continue;
            createRipple(blob);
        }

        // create a "test" ripple if pressing button
        if (trigger.getValueb() != lastTriggerState) {
            boolean state = trigger.getValueb();
            if (state) {
                LXPoint ranPoint = model.points[(int)random(model.points.length-1)];
                PVector ranPos = new PVector(ranPoint.x, ranPoint.y, ranPoint.z);

                this.triggerRipple = new Ripple(
                    ranPos,
                    initialRadius.getValuef(),
                    intensity.getValuef(),
                    speed.getValuef(),
                    decaySec.getValuef(),
                    nextHue.getValuef(),
                    nextSat.getValuef()
                );
            } else {
                if (triggerRipple != null) {
                    this.triggerRipple.release();
                    this.triggerRipple = null;
                }
            }
            lastTriggerState = state;
        }

        // remove ripples that have expired
        List<Ripple> expired = new ArrayList<Ripple>();
        for (Ripple ripple : ripples) {
            ripple.advance(deltaSec);
            if (ripple.isExpired()) {
                expired.add(ripple);
            }
        }
        ripples.removeAll(expired);

        if (triggerRipple != null) {
            triggerRipple.advance(deltaSec);
            if (triggerRipple.isExpired()) {
                triggerRipple = null;
            }
        }

        // draw ripples
        for (LXPoint p : model.points) {
            int sum = 0xff000000;
            for (Ripple ripple : ripples) {
                sum += LXColor.add(sum, ripple.getColor(p));
            }

            if (triggerRipple != null) {
                sum += LXColor.add(sum, triggerRipple.getColor(p));
            }
            colors[p.index] = sum;
        }
    }

    class Ripple {
        BlobTracker.Blob blob;
        PVector pos;
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

        Ripple(PVector pos, float radius, float intensity, float speed, float decaySec, float hue, float sat) {
            this.blob = null;
            this.pos = pos;
            this.radius = radius;
            this.intensity = intensity;
            this.speed = speed;
            this.decaySec = decaySec;
            this.hue = hue + random(hueVariance.getValuef());
            this.sat = sat;
            this.ageSec = 0;
        }

        Ripple(BlobTracker.Blob blob, float radius, float intensity, float speed, float decaySec, float hue, float sat) {
            this.blob = blob;
            this.radius = radius;
            this.intensity = intensity;
            this.speed = speed;
            this.decaySec = decaySec;
            this.hue = hue + random(hueVariance.getValuef());
            this.sat = sat;
            this.ageSec = 0;

            NissanWindow window = ((NissanModel)model).getWindowById(blob.id);
            // window.uvTransform.push();
            // window.uvTransform.translate(blob.pos.x, blob.pos.y, blob.pos.z);
            // PVector worldPos = new PVector(window.uvTransform.x(), window.uvTransform.y(), window.uvTransform.z());
            // window.uvTransform.pop();
            PVector worldPos = blob.pos;
            this.pos = worldPos;
        }

        void advance(float deltaSec) {
            if (!held) ageSec += deltaSec;
            radius += deltaSec * speed;
            value = intensity / (1f + 0.10f * ageSec / decaySec);
        }

        void release() {
            held = false;
        }

        int getColor(LXPoint p) {
            float distance = distanceToPoint(p);
            if (distance < radius) {
                float normDist = distance / radius;
                float brightness = 0;

                if (normDist < 0.8) {
                    brightness = MathUtils.pow((float)(distance / radius*1.2f), 4f) * 100;
                } else {
                    float d = MathUtils.pow((float)((distance/radius - (radius*0.8f/radius)) / 0.2f), 1.2f);
                    brightness = MathUtils.map(d, 0, 1, 100f, 1f);
                }

                // fade brightness over distance
                brightness = MathUtils.constrain(brightness - (ageSec / decaySec * 100f), 0, 100);

                return lx.hsb(hue, sat * 100f, brightness);
            } else {
                return 0;
            }
        }

        boolean matchesBlob(BlobTracker.Blob blob) {
            if (this.blob.id.equals(blob.id)
             && Math.abs(this.blob.pos.x - blob.pos.x) < 10
             && Math.abs(this.blob.pos.y - blob.pos.y) < 10) {
                return true;
            } else {
                return false;
            }
        }

        boolean isExpired() {
            return ageSec > decaySec; // * 2;
        }

        float distanceToPoint(LXPoint p) {
            return (float)Math.sqrt(Math.pow(pos.x - p.x, 2) + Math.pow(pos.y - p.y, 2) + Math.pow(pos.z - p.z, 2));
        }
    }
}
