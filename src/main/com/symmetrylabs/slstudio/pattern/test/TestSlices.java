package com.symmetrylabs.slstudio.pattern.test;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.model.suns.Slice;
import com.symmetrylabs.slstudio.model.suns.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

public class TestSlices extends SunsPattern implements SLTestPattern {

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
