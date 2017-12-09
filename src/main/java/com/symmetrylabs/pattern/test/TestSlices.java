package com.symmetrylabs.pattern.test;

import com.symmetrylabs.model.Slice;
import com.symmetrylabs.model.Sun;
import com.symmetrylabs.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;


public class TestSlices extends SLPattern {

     public TestSlices(LX lx) {
         super(lx);
     }

     public void run(double deltaMs) {
         for (Sun sun : model.suns) {
             float hue = 0;

             for (Slice slice : sun.slices) {
                 for (LXPoint p : slice.points) {
                     colors[p.index] = lx.hsb(hue, 100, 100);
                 }

                 hue += 70;
             }
         }
     }
 }
