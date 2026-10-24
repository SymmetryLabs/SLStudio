package com.symmetrylabs.shows.kaleidoscope;

import com.symmetrylabs.shows.kaleidoscope.ui.UIKaleidoscopeModelingTool;
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

public class UniverseSelectorKaleidoscope extends SLPattern<StripsModel> {

    private final DiscreteParameter universe;
    private final DiscreteParameter pixel;
    private final BooleanParameter wholeUniverse;

    // datagram index -> list of LXPoints in output order (mirrors KaleidoscopePixlite mapping)
    // Index order is controller 0's universes (U0, U1, ...) followed by controller 1's (U0, U1, ...).
    private final List<List<LXPoint>> universePoints = new ArrayList<>();

    public UniverseSelectorKaleidoscope(LX lx) {
        super(lx);

        // Use the exact datagram->points mapping built by KaleidoscopePixlite so this
        // tool always matches what is actually sent on the wire. Only fall back to
        // re-deriving from the on-disk counts if the output has not been built.
        List<List<LXPoint>> authoritative = KaleidoscopeShow.KaleidoscopePixlite.universePoints;
        int maxPixels = 1;
        if (!authoritative.isEmpty()) {
            for (List<LXPoint> pts : authoritative) {
                universePoints.add(pts);
                if (pts.size() > maxPixels) maxPixels = pts.size();
            }
        } else {
            List<Strip> strips = model.getStrips();
            int[] counts = UIKaleidoscopeModelingTool.loadStripCountsFromDisk();
            for (List<LXPoint> pts : KaleidoscopeShow.KaleidoscopePixlite.computeUniversePoints(strips, counts)) {
                universePoints.add(pts);
                if (pts.size() > maxPixels) maxPixels = pts.size();
            }
        }

        addParameter(universe      = new DiscreteParameter("universe", 1, 1, universePoints.size() + 1));
        addParameter(pixel         = new DiscreteParameter("pixel",    1, 1, maxPixels + 1));
        addParameter(wholeUniverse = new BooleanParameter("all-pixels", true));
    }

    @Override
    public void run(double deltaMs) {
        setColors(LXColor.BLACK);

        int u = universe.getValuei() - 1;  // 0-based index into the datagram list
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
