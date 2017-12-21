package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.StripsPattern;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;


public class MiddleStrip extends StripsPattern implements SLTestPattern {
     public MiddleStrip(LX lx) {
         super(lx);
     }

     public void run(double deltaMs) {
         setColors(0);
         for (Strip strip : model.getStrips()) {
             int counter = 0;

             for (LXPoint p : strip.points) {
                 if (counter++ == strip.metrics.numPoints/2+1) {
                     colors[p.index] = LXColor.BLUE;
                 }
             }
         }
     }
 }
