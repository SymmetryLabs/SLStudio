package com.symmetrylabs.shows.firefly;

import art.lookingup.AnchorTree;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUFlower;
import com.symmetrylabs.slstudio.palettes.PaletteLibrary;
import com.symmetrylabs.slstudio.palettes.ZigzagPalette;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

public class FWPalette extends BFBase {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    private final PaletteLibrary paletteLibrary = PaletteLibrary.getInstance();
    DiscreteParameter gradpal = new DiscreteParameter("gradpal", paletteLibrary.getNames());
    // selected colour palette
    CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
    CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
    CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
    CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
    CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)
    ZigzagPalette pal = new ZigzagPalette();

    public FWPalette(LX lx) {
        super(lx);

        addParameter(gradpal);
        addParameter(palStart);
        addParameter(palStop);
        addParameter(palBias);
        addParameter(palShift);
        addParameter(palCutoff);
    }

    protected void renderFlower(double drawDeltaMs, LUFlower flower, int randomInt) {
        pal.setPalette(paletteLibrary.get(gradpal.getOption()));
        pal.setBottom(palStart.getValue());
        pal.setTop(palStop.getValue());
        pal.setBias(palBias.getValue());
        pal.setShift(palShift.getValue());
        pal.setCutoff(palCutoff.getValue());
        AnchorTree tree = KaledoscopeModel.anchorTrees.get(flower.anchorTree);
        float t = (flower.y - tree.flowerMin) / (tree.flowerMax - tree.flowerMin);
        int color = pal.getColor(t);
        for (LXPoint p : flower.allPoints) {
            colors[p.index] = color;
        }
    }
}
