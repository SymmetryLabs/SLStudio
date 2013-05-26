/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.pattern;



import heronarts.lx.HeronLX;
import heronarts.lx.LXUtils;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.transition.WipeTransition;

import java.lang.Math;

public class BouncingPattern extends LXPattern {

    private final int BOUNCER_WIDTH = 2;
    private final int NUM_BOUNCERS;

    private final double[] mags;
    private final double[] speeds;
    private final double[] accum;

    private final TriangleLFO[] magLFO;

    public BouncingPattern(HeronLX lx) {
        super(lx);
        this.NUM_BOUNCERS = lx.width / 2;
        this.mags = new double[this.NUM_BOUNCERS];
        this.speeds = new double[this.NUM_BOUNCERS];
        this.accum = new double[this.NUM_BOUNCERS];
        this.magLFO = new TriangleLFO[this.NUM_BOUNCERS];
        
        for (int i = 0; i < this.NUM_BOUNCERS; ++i) {
            this.mags[i] = LXUtils.random(2, 7);
            this.speeds[i] = LXUtils.random(300, 400);
            this.accum[i] = LXUtils.random(0, 5000);
            this.magLFO[i] = new TriangleLFO(2, 8, 8000 + i*1000);
            this.addModulator(this.magLFO[i].trigger().setValue(this.mags[i]));
        }
        this.transition = new WipeTransition(lx, WipeTransition.Direction.RIGHT);
    }

    protected void run(int deltaMs) {
        for (int i = 0; i < this.NUM_BOUNCERS; ++i) {
            this.mags[i] = this.magLFO[i].getValue();
            this.accum[i] += deltaMs / this.speeds[i];
            double v = this.mags[i] * Math.abs(Math.sin(this.accum[i]));
            double h = ((int) (this.accum[i] * 20)) % 360;
            for (int j = 0; j < lx.height; ++j) {
                for (int x = 0; x < this.BOUNCER_WIDTH; ++x) {
                    setColor(this.BOUNCER_WIDTH*i + x, lx.height - 1 - j, this.lx.colord(
                            h, 100., Math.max(0, 100 - 40*(Math.abs(v - j)))));
                }
            }
        }
    }



}
