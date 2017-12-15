package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;


public class MiddleStrip extends LXPattern implements SLTestPattern {
     public MiddleStrip(LX lx) {
         super(lx);
     }

     public void run(double deltaMs) {
         setColors(0);
         for (Strip strip : ((SLModel)model).strips) {
             int counter = 0;

             for (LXPoint p : strip.points) {
                 if (counter++ == strip.metrics.numPoints/2+1) {
                     colors[p.index] = LXColor.BLUE;
                 }
             }
         }
     }
 }
