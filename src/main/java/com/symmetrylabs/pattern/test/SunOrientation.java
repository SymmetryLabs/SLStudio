package com.symmetrylabs.pattern.test;

import com.symmetrylabs.model.Slice;
import com.symmetrylabs.model.Sun;
import com.symmetrylabs.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;


public class SunOrientation extends SLPattern {

     public SunOrientation(LX lx) {
         super(lx);
     }

     public void run(double deltaMs) {
         for (Sun sun : this.model.suns) {
             int i = 0;
             for (Slice slice : sun.slices) {
                 int col = ((i++ & 1) == 0) ? LXColor.BLUE : LXColor.RED;
                 for (LXPoint p : slice.points) {
                     colors[p.index] = col;
                 }
             }
         }
     }
 }
