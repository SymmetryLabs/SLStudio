package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.*;
import com.symmetrylabs.slstudio.model.nissan.NissanCar;
import com.symmetrylabs.slstudio.model.nissan.PanelPoint;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.SLStudio;

import java.util.List;

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
        addParameter(SLStudio.applet.selectedColumn);
    }

    public void run(double deltaMs) {
        int x = SLStudio.applet.selectedColumn.getValuei();
        int y = SLStudio.applet.selectedStrip.getValuei();

        setColors(0);
        for (NissanCar car : this.model.getCars()) {
            int stripIndex = SLStudio.applet.selectedStrip.getValuei();
            List<Strip> strips = car.getStrips();
            Strip strip = strips.get(stripIndex);

            for (Strip stripx : strips){
                for (LXPoint p : stripx.points) {
                    if ( ((PanelPoint) p).getPanel_x() == x ){
                        colors[p.index] = LXColor.GREEN;
                    }
                }
            }
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

