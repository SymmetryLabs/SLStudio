package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;


public class SunOrientation extends SLPattern implements SLTestPattern {

     public SunOrientation(LX lx) {
         super(lx);
     }

     public void run(double deltaMs) {
         for (Sun sun : this.model.getSuns()) {
             int i = 0;
             for (Slice slice : sun.getSlices()) {
                 int col = ((i++ & 1) == 0) ? LXColor.BLUE : LXColor.RED;
                 for (LXPoint p : slice.points) {
                     colors[p.index] = col;
                 }
             }
         }
     }
 }
