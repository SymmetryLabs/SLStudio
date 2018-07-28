package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.midi.LXMidiListener;
import heronarts.lx.midi.MidiNote;
import heronarts.lx.midi.MidiNoteOn;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.CompoundParameter;

import static heronarts.lx.LX.TWO_PI;

public class BouncyBalls extends LXPattern implements LXMidiListener {

    static final int NUM_BALLS = 6;

    class BouncyBall {

        Accelerator yPos;
        TriangleLFO xPos = new TriangleLFO(model.xMin, model.xMax, MathUtils.random(8000, 19000));
        float zPos;

        BouncyBall(int i) {
            addModulator(xPos.setBasis(MathUtils.random(0, ((float) TWO_PI)))).start();
            addModulator(yPos = new Accelerator(0, 0, 0));
            zPos = MathUtils.lerp(model.zMin, model.zMax, (i+2f) / (NUM_BALLS + 4f));
        }

        void bounce(float midiVel) {
            float v = 100 + 8*midiVel;
            yPos.setSpeed(v, getAccel(v, 60 / lx.tempo.bpmf())).start();
        }

        float getAccel(float v, float oneBeat) {
            return -2*v / oneBeat;
        }

        void run(double deltaMs) {
            float flrLevel = flr.getValuef() * model.xMax/2f;
            if (yPos.getValuef() < flrLevel) {
                if (yPos.getVelocity() < -50) {
                    yPos.setValue(2*flrLevel-yPos.getValuef());
                    float v = -yPos.getVelocityf() * bounce.getValuef();
                    yPos.setSpeed(v, getAccel(v, 60 / lx.tempo.bpmf()));
                } else {
                    yPos.setValue(flrLevel).stop();
                }
            }
            float falloff = 130.f / (12 + blobSize.getValuef() * 36);
            float xv = xPos.getValuef();
            float yv = yPos.getValuef();

            for (LXPoint p : model.points) {
                float d = MathUtils.sqrt((p.x-xv)*(p.x-xv) + (p.y-yv)*(p.y-yv) + .1f*(p.z-zPos)*(p.z-zPos));
                float b = MathUtils.constrain(130 - falloff*d, 0, 100);
                if (b > 0) {
                    blendColor(p.index, lx.hsb(
                        palette.getHuef() + p.y*.5f + MathUtils.abs(model.cx - p.x) * .5f,
                        MathUtils.max(0, 100 - .45f*(p.y - flrLevel)),
                        b
                    ), LXColor.Blend.ADD);
                }
            }
        }
    }

    final BouncyBall[] balls = new BouncyBall[NUM_BALLS];

    final CompoundParameter bounce = new CompoundParameter("BNC", .8);
    final CompoundParameter flr = new CompoundParameter("FLR", 0);
    final CompoundParameter blobSize = new CompoundParameter("SIZE", 0.5);

    public BouncyBalls(LX lx) {
        super(lx);
        for (int i = 0; i < balls.length; ++i) {
            balls[i] = new BouncyBall(i);
        }
        addParameter(bounce);
        addParameter(flr);
        addParameter(blobSize);

        lx.engine.midi.addListener(this);
    }

    public void run(double deltaMs) {
        setColors(0);
        for (BouncyBall b : balls) {
            b.run(deltaMs);
        }
    }

    @Override
    public void noteOnReceived(MidiNoteOn note) {
        int pitch = (note.getPitch() + note.getChannel()) % NUM_BALLS;
        balls[pitch].bounce(note.getVelocity());
    }
}
