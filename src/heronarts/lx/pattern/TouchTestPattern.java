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
import heronarts.lx.modulator.LinearEnvelope;


public class TouchTestPattern extends LXPattern {
    private final LinearEnvelope brightness;
    
    public TouchTestPattern(HeronLX lx) {
        super(lx);
        this.addModulator(this.brightness = new LinearEnvelope(0, 0, 100));
    }
    
    public void run(int deltaMs) {
        double touchX = (this.lx.width-1) * this.lx.touch().getX();
        double touchY = (this.lx.height-1) * this.lx.touch().getY();
        
        if (this.lx.touch().isActive()) {
            this.brightness.setEndValue(100).trigger();
        } else {
            this.brightness.setEndValue(20).trigger();
        }
        
        for (int i = 0; i < this.lx.total; ++i) {
            double distance = LXUtils.distance(this.lx.column(i), this.lx.row(i), touchX, touchY);
            this.colors[i] = this.lx.colord(
                    this.lx.getBaseHue(),
                    100,
                    Math.max(this.brightness.getValue() - distance*40., 0)            
                    );
        }
    }
}
