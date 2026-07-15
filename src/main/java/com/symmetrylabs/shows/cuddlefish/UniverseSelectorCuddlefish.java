package com.symmetrylabs.shows.cuddlefish;

import com.symmetrylabs.shows.cuddlefish.ui.UICuddlefishModelingTool;
import static com.symmetrylabs.shows.cuddlefish.ui.UICuddlefishModelingTool.UNIVERSE_COUNT;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniverseSelectorCuddlefish extends SLPattern<StripsModel> {

    private final DiscreteParameter universe;
    private final DiscreteParameter pixel;
    private final BooleanParameter wholeUniverse;

    // universe index → list of LXPoints in output order (mirrors CuddlefishPixlite mapping)
    private final List<List<LXPoint>> universePoints = new ArrayList<>();

    public UniverseSelectorCuddlefish(LX lx) {
        super(lx);

        // Use the exact universe→points mapping built by CuddlefishPixlite so this
        // tool always matches what is actually sent on the wire. Only fall back to
        // re-deriving from the on-disk counts if the output has not been built.
        List<List<LXPoint>> authoritative = CuddlefishShow.CuddlefishPixlite.universePoints;
        int maxPixels = 1;
        Map<Integer, Integer> pointIndexToUniverse = new HashMap<>();
        if (!authoritative.isEmpty()) {
            for (List<LXPoint> pts : authoritative) {
                universePoints.add(pts);
                if (pts.size() > maxPixels) maxPixels = pts.size();
            }
        } else {
        int[] counts = UICuddlefishModelingTool.loadStripCountsFromDisk();
        List<Strip> strips = model.getStrips();
        int stripIndex = 0;
        for (int u = 0; u < UNIVERSE_COUNT; u++) {
            List<LXPoint> pts = new ArrayList<>();
            for (int s = 0; s < counts[u] && stripIndex < strips.size(); s++) {
                for (LXPoint p : strips.get(stripIndex).getPoints()) {
                    pts.add(p);
                }
                stripIndex++;
            }
            universePoints.add(pts);
            for (LXPoint p : pts) {
                Integer otherU = pointIndexToUniverse.put(p.index, u);
                if (otherU != null) {
                    System.err.println("UniverseSelectorCuddlefish: point index " + p.index + " assigned to both U" + (otherU + 1) + " and U" + (u + 1));
                }
            }
            if (pts.size() > maxPixels) maxPixels = pts.size();
        }
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
