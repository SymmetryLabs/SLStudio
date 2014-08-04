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

import heronarts.lx.LX;
import heronarts.lx.LXColor;
import heronarts.lx.LXUtils;
import heronarts.lx.audio.GraphicEQ;
import heronarts.lx.transition.WipeTransition;

public class GraphicEqualizerPattern extends LXPattern {

    private final GraphicEQ eq;

    public GraphicEqualizerPattern(LX lx) {
        super(lx);
        addModulator(this.eq = new GraphicEQ(lx.audioInput())).start();
        this.transition = new WipeTransition(lx, WipeTransition.Direction.UP);
    }

    @Override
    public void run(double deltaMs) {
        for (int i = 0; i < this.lx.width; ++i) {
            int avgIndex = (int) (i / (double) this.lx.width * (eq.numBands - 1));
            double value = eq.getBand(avgIndex);
            for (int j = 0; j < this.lx.height; ++j) {
                double jscaled = (this.lx.height - 1 - j)
                        / (double) (this.lx.height - 1);
                double b = LXUtils.constrain(400. * (value - jscaled), 0, 100);
                this.setColor(i, j, LXColor.hsb(this.lx.getBaseHue(), 100., b));
            }
        }
    }

}