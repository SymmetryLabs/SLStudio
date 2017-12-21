package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;


public class TestSlices extends SLPattern implements SLTestPattern {

     public TestSlices(LX lx) {
         super(lx);
     }

     public void run(double deltaMs) {
         for (Sun sun : model.getSuns()) {
             float hue = 0;

             for (Slice slice : sun.getSlices()) {
                 for (LXPoint p : slice.points) {
                     colors[p.index] = lx.hsb(hue, 100, 100);
                 }

                 hue += 70;
             }
         }
     }
 }
