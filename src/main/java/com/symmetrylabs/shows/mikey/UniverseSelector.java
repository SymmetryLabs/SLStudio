package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.mikey.ui.UIMikeyModelingTool;
import static com.symmetrylabs.shows.mikey.ui.UIMikeyModelingTool.UNIVERSE_COUNT;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.List;

public class UniverseSelector extends SLPattern<StripsModel> {

    private final DiscreteParameter universe;
    private final DiscreteParameter pixel;
    private final BooleanParameter wholeUniverse;

    // universe index → list of LXPoints in output order (mirrors MikeyPixlite mapping)
    private final List<List<LXPoint>> universePoints = new ArrayList<>();

    public UniverseSelector(LX lx) {
        super(lx);

        // Build universe→points mapping from the same strip counts used by MikeyPixlite
        int[] counts = UIMikeyModelingTool.loadStripCountsFromDisk();
        List<Strip> strips = model.getStrips();
        int stripIndex = 0;
        int maxPixels = 1;
        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            List<LXPoint> pts = new ArrayList<>();
            for (int s = 0; s < counts[u] && stripIndex < strips.size(); s++) {
                for (LXPoint p : strips.get(stripIndex).getPoints()) {
                    pts.add(p);
                }
                stripIndex++;
            }
            universePoints.add(pts);
            if (pts.size() > maxPixels) maxPixels = pts.size();
        }

        addParameter(universe      = new DiscreteParameter("universe", 1, 1, UNIVERSE_COUNT + 1));
        addParameter(pixel         = new DiscreteParameter("pixel",    1, 1, maxPixels + 1));
        addParameter(wholeUniverse = new BooleanParameter("all-pixels", true));
    }

    @Override
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);

        int u = universe.getValuei() - 1;  // 0-based
        if (u < 0 || u >= universePoints.size()) return;

        List<LXPoint> pts = universePoints.get(u);
        if (pts.isEmpty()) return;

        if (wholeUniverse.isOn()) {
            // Light the entire universe output white
            for (LXPoint p : pts) {
                colors[p.index] = LXColor.WHITE;
            }
        } else {
            // Light a single pixel on that universe (1-based parameter)
            int px = pixel.getValuei() - 1;  // 0-based
            if (px >= 0 && px < pts.size()) {
                colors[pts.get(px).index] = LXColor.WHITE;
            }
        }
    }
}
