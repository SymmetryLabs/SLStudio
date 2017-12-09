package com.symmetrylabs.pattern.test;

import com.symmetrylabs.SLStudio;
import com.symmetrylabs.model.Slice;
import com.symmetrylabs.model.Strip;
import com.symmetrylabs.model.Sun;
import com.symmetrylabs.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class SelectedStrip extends SLPattern {

     public SelectedStrip(LX lx) {
         super(lx);
         addParameter(SLStudio.applet.selectedStrip);
     }

     public void run(double deltaMs) {
         setColors(0);
         for (Sun sun : this.model.suns) {
             for (Slice slice : sun.slices) {
                 int stripIndex = SLStudio.applet.selectedStrip.getValuei();

                 if (stripIndex > slice.strips.size()) {
                     break;
                 }

                 Strip strip = slice.strips.get(SLStudio.applet.selectedStrip.getValuei() - 1);

                 for (LXPoint p : strip.points) {
                     colors[p.index] = LXColor.RED;
                 }
             }
         }
     }
 }


