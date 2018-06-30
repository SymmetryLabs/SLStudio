package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.Accelerator;
import heronarts.lx.modulator.Click;
import heronarts.lx.parameter.CompoundParameter;

//import static com.symmetrylabs.slstudio.util.MathUtils.constrain;

public class Pulley extends LXPattern {
    final int NUM_DIVISIONS = 16;
    private final Accelerator[] gravity = new Accelerator[NUM_DIVISIONS];
    private final Click[] delays = new Click[NUM_DIVISIONS];

    private final Click reset = new Click(9000);
    private boolean isRising = false;

    private CompoundParameter sz = new CompoundParameter("SIZE", 0.5);
    private CompoundParameter beatAmount = new CompoundParameter("BEAT", 0);

    public Pulley(LX lx) {
        super(lx);
        for (int i = 0; i < NUM_DIVISIONS; ++i) {
            addModulator(gravity[i] = new Accelerator(0, 0, 0));
            addModulator(delays[i] = new Click(0));
        }
        addModulator(reset).start();
        addParameter(sz);
        addParameter(beatAmount);
        trigger();

    }

    private void trigger() {
        isRising = !isRising;
        int i = 0;
        for (Accelerator g : gravity) {
            if (isRising) {
                g.setSpeed(MathUtils.random(20, 33), 0).start();
            } else {
                g.setVelocity(0).setAcceleration(-420);
                delays[i].setPeriod(MathUtils.random(0, 500)).trigger();
            }
            ++i;
        }
    }

    public void run(double deltaMs) {
        if (reset.click()) {
            trigger();
        }

        if (isRising) {
            // Fucking A, had to comment this all out because of that bizarre
            // Processing bug where some simple loop takes an absurd amount of
            // time, must be some pre-processor bug
//      for (Accelerator g : gravity) {
//        if (g.getValuef() > model.yMax) {
//          g.stop();
//        } else if (g.getValuef() > model.yMax*.55) {
//          if (g.getVelocityf() > 10) {
//            g.setAcceleration(-16);
//          } else {
//            g.setAcceleration(0);
//          }
//        }
//      }
        } else {
            int j = 0;
            for (Click d : delays) {
                if (d.click()) {
                    gravity[j].start();
                    d.stop();
                }
                ++j;
            }
            for (Accelerator g : gravity) {
                if (g.getValuef() < 0) {
                    g.setValue(-g.getValuef());
                    g.setVelocity(-g.getVelocityf() * MathUtils.random(0.74f, 0.84f));
                }
            }
        }

        // A little silliness to test the grid API
//        if (midiEngine != null && midiEngine.getFocusedPattern() == this) {
//            for (int i = 0; i < 5; ++i) {
//                for (int j = 0; j < 8; ++j) {
//                    int gi = (int) MathUtils.constrain(j * NUM_DIVISIONS / 8, 0, NUM_DIVISIONS-1);
//                    float b = 1 - 4.*abs((6-i)/6. - gravity[gi].getValuef() / model.yMax);
//                    midiEngine.grid.setState(i, j, (b < 0) ? 0 : 3);
//                }
//            }
//        }

        float fPos = 1 - lx.tempo.rampf();
        if (fPos < .2) {
            fPos = .2f + 4 * (.2f - fPos);
        }
        float falloff = 100f / (3f + sz.getValuef() * 36f + fPos * beatAmount.getValuef()*48f);
        for (LXPoint p : model.points) {
            int gi = (int) MathUtils.constrain((p.x - model.xMin) * NUM_DIVISIONS / (model.xMax - model.xMin), 0, NUM_DIVISIONS-1);
            colors[p.index] = lx.hsb(
                palette.getHuef() + MathUtils.abs(p.x - model.cx)*.8f + p.y*.4f,
                MathUtils.constrain(130 - p.y*.8f, 0, 100),
                MathUtils.max(0, 100 - MathUtils.abs(p.y - gravity[gi].getValuef())*falloff)
            );
        }
    }
}
