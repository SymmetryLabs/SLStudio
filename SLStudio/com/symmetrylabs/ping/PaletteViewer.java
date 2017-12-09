package com.symmetrylabs.ping;

import com.symmetrylabs.p3lx.LXStudio;
import com.symmetrylabs.palettes.ZigzagPalette;
import com.symmetrylabs.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PaletteViewer extends SLPattern {
    DiscreteParameter palette = new DiscreteParameter("palette", ((LXStudio) lx).paletteLibrary.getNames());
        // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

    ZigzagPalette pal = new ZigzagPalette();

    public PaletteViewer(LX lx) {
        super(lx);
        addParameter(palette);
        addParameter(palStart);
        addParameter(palStop);
        addParameter(palShift);
        addParameter(palBias);
        addParameter(palCutoff);
    }

    public void run(double deltaMs) {
        pal.setPalette(((LXStudio) lx).paletteLibrary.get(palette.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());
        for (LXPoint p : model.points) {
            colors[p.index] = pal.getColor((p.y - model.yMin) / (model.yMax - model.yMin));
        }
    }
}
