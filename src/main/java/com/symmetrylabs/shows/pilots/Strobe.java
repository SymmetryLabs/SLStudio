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

public class Strobe<T extends Strip> extends SLPattern<StripsModel<T>> {
    public static final String GROUP_NAME = PilotsShow.SHOW_NAME;

    private BooleanParameter trigger = new BooleanParameter("trigger", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private CompoundParameter length = new CompoundParameter("length", 100, 10, 500);
    private int next = 0;
    private List<Set<Bundle>> layers;
    private boolean active;

    public Strobe(LX lx) {
        super(lx);

        addParameter(trigger);
        addParameter(length);

        layers = new ArrayList<>();

        Set<Bundle> back = new HashSet<>();
        for (Bundle b : model.getTopology().bundles) {
            if (b.dir != Dir.Z)
                continue;
            if (b.get(Sign.POS).get(Dir.Z, Sign.POS) == null) {
                back.add(b);
            }
        }
        layers.add(back);
        for (int attempts = 0; attempts < 300; attempts++) {
            Set<Bundle> layer = new HashSet<>();
            for (Bundle b : layers.get(layers.size() - 1)) {
                if (b.dir != Dir.Z)
                    continue;
                Bundle next = b.get(Sign.NEG).get(Dir.Z, Sign.NEG);
                if (next != null) {
                    layer.add(next);
                }
            }
            if (layer.isEmpty()) {
                break;
            }
            layers.add(layer);
        }
    }

    @Override
    public String getCaption() {
        return String.format("%d layers, on %d", layers.size(), next);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == trigger && trigger.getValueb()) {
            next++;
            if (next >= layers.size()) {
                next = 0;
            }
            active = true;
        } else if (p == trigger) {
            active = false;
        }
    }

    @Override
    public void run(double deltaMs) {
        Set<Bundle> cur = layers.get(next);
        for (Bundle b : model.getTopology().bundles) {
            for (int stripIdx : b.strips) {
                for (LXPoint p : model.getStripByIndex(stripIdx).points) {
                    colors[p.index] = active && cur.contains(b) ? LXColor.WHITE : LXColor.BLACK;
                }
            }
        }
    }
}
