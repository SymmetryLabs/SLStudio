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

public class SolidColorPattern extends LXPattern {
    final int color;
    
    public SolidColorPattern(HeronLX lx, int color) {
        super(lx);
        this.color = color;
        for (int i = 0; i < this.colors.length; ++i) {
            this.colors[i] = color;
        }
    }
    
    public void run(int deltaMs) {}
}
