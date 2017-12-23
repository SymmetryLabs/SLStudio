package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Slice;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

public class SelectedStrip extends SunsPattern {

     public SelectedStrip(LX lx) {
         super(lx);
         addParameter(SLStudio.applet.selectedStrip);
     }

     public void run(double deltaMs) {
         setColors(0);

         for (Sun sun : model.getSuns()) {
             for (Slice slice : sun.getSlices()) {
                 int stripIndex = SLStudio.applet.selectedStrip.getValuei();

                 if (stripIndex > slice.getStrips().size()) {
                     break;
                 }

                 Strip strip = slice.getStrips().get(SLStudio.applet.selectedStrip.getValuei() - 1);

                 for (LXPoint p : strip.points) {
                     colors[p.index] = LXColor.RED;
                 }
             }
         }
     }
 }


