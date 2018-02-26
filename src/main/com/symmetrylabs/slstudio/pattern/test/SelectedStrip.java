package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.suns.Slice;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.suns.Sun;
import com.symmetrylabs.slstudio.pattern.SunsPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

public class SelectedStrip extends SunsPattern implements SLTestPattern {

     public SelectedStrip(LX lx) {
         super(lx);
         addParameter(SLStudio.applet.selectedStrip);
     }

     public void run(double deltaMs) {
         setColors(0);
         for (Sun sun : this.model.getSuns()) {
             for (Slice slice : sun.getSlices()) {
                 int stripIndex = SLStudio.applet.selectedStrip.getValuei();

                 if (stripIndex > slice.getStrips().size()) {
                     break;
                 }

                 Strip strip = slice.getStrips().get(SLStudio.applet.selectedStrip.getValuei() - 1);
                 int n = 0 ;
                 int end = strip.points.length;

                 for (LXPoint p : strip.points) {
                     //if (n == 0 || n == end-1){
                     if (n == 0){
                            colors[p.index] = LXColor.GREEN;
                        //}
                         //else {
                        //    colors[p.index] = LXColor.WHITE;
                    //    }
                       }
                     else {
                         colors[p.index] = LXColor.RED;
                     }
                     n++;
                 }
             }
         }
     }
 }


