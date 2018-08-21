package com.symmetrylabs.shows.pilots;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllVerticals<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;
    private Set<Bundle> bundles;

    public AllVerticals(LX lx) {
        super(lx);

        bundles = new HashSet<>();
        for (Bundle b : model.getTopology().bundles) {
            if (b.dir == Dir.Y) {
                bundles.add(b);
            }
        }
    }

    @Override
    public void run(double deltaMs) {
        for (Bundle b : model.getTopology().bundles) {
            for (int stripIdx : b.strips) {
                for (LXPoint p : model.getStripByIndex(stripIdx).points) {
                    colors[p.index] = bundles.contains(b) ? LXColor.WHITE : LXColor.BLACK;
                }
            }
        }
    }
}
