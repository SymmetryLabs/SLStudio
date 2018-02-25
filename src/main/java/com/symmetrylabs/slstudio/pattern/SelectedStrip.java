package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.*;
import com.symmetrylabs.slstudio.pixlites.NissanPixlite;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.pattern.SunsPattern;

//public class SelectedStrip extends SunsPattern {
//
//   public SelectedStrip(LX lx) {
//     super(lx);
//     addParameter(SLStudio.applet.selectedStrip);
//   }
//
//   public void run(double deltaMs) {
//     setColors(0);
//
//     for (Sun sun : model.getSuns()) {
//       for (Slice slice : sun.getSlices()) {
//         int stripIndex = SLStudio.applet.selectedStrip.getValuei();
//
//         if (stripIndex > slice.getStrips().size()) {
//           break;
//         }
//
//         Strip strip = slice.getStrips().get(SLStudio.applet.selectedStrip.getValuei() - 1);
//
//         for (LXPoint p : strip.points) {
//           colors[p.index] = LXColor.RED;
//         }
//       }
//     }
//   }
// }


public class SelectedStrip extends NissanPattern {

    public SelectedStrip(LX lx) {
        super(lx);
        addParameter(SLStudio.applet.selectedStrip);
    }

    public void run(double deltaMs) {
        setColors(0);
        for (NissanCar car : this.model.getCars()) {
            int stripIndex = SLStudio.applet.selectedStrip.getValuei();
            Strip strip = car.getStrips().get(stripIndex);

            int n = 0;
            int end = strip.points.length;

            for (LXPoint p : strip.points) {
                if (n == 0 || n == end - 1) {
                    if (n == 0) {
                        colors[p.index] = LXColor.GREEN;
                    } else {
                        colors[p.index] = LXColor.WHITE;
                    }
                } else {
                    colors[p.index] = LXColor.RED;
                }
                n++;
            }

        }
    }
}

