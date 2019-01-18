package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.EdgeAStar;
import com.symmetrylabs.util.StripsTopologyComponents;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Lightning<T extends Strip> extends SLPattern<StripsModel<T>> {
    private final BooleanParameter trigger = new BooleanParameter("trigger", false);
    private final BooleanParameter debounce = new BooleanParameter("debounce", false);
    private final DiscreteParameter boltsParameter = new DiscreteParameter("bolts", 6, 1, 30);
    private final CompoundParameter releaseParam = new CompoundParameter("release", 80, 0, 2000);

    StripsTopology topo;
    Random random = new Random();
    EdgeAStar aStar;
    boolean debouncing = false;

    private static class Component {
        ArrayList<Junction> center = new ArrayList<>();
        ArrayList<Junction> edge = new ArrayList<>();
    }
    List<Component> components;

    private class Strike {
        ArrayList<Bundle> bundles = new ArrayList<>();
        double age = 0;
        boolean displayed = false;
    }
    List<Strike> strikes;

    public Lightning(LX lx) {
        super(lx);
        addParameter(releaseParam);
        addParameter(boltsParameter);
        addParameter(trigger);
        addParameter(debounce);
        trigger.setMode(BooleanParameter.Mode.MOMENTARY);

        topo = model.getTopology();
        if (topo == null) {
            return;
        }

        components = new ArrayList<>();
        strikes = new ArrayList<>();
        aStar = new EdgeAStar(topo);
        StripsTopologyComponents stc = new StripsTopologyComponents(topo);

        for (StripsTopologyComponents.ConnectedComponent cc : stc.getComponents()) {
            Component c = new Component();
            for (Junction j : cc.junctions) {
                if (j.degree() > 4) {
                    c.center.add(j);
                } else {
                    c.edge.add(j);
                }
            }
            if (!c.center.isEmpty() && !c.edge.isEmpty()) {
                components.add(c);
            }
        }
    }

    @Override
    public String getCaption() {
        return String.format("%d components, %d strikes", components.size(), strikes.size());
    }

    @Override
    public void run(double deltaMs) {
        if (topo == null) {
            return;
        }
        if (trigger.getValueb()) {
            if (debounce.getValueb()) {
                if (!debouncing) {
                    strikes.add(newStrike());
                    debouncing = true;
                }
            } else {
                strikes.add(newStrike());
            }
        } else {
            debouncing = false;
        }

        for (Strike s : strikes) {
            s.age += deltaMs;
        }

        int black = LXColor.gray(0);
        for (int i = 0; i < colors.length; i++) {
            colors[i] = black;
        }

        for (Iterator<Strike> iter = strikes.iterator(); iter.hasNext();) {
            Strike s = iter.next();
            float bright = s.displayed ? 100f * (float) (1f - s.age / releaseParam.getValuef()) : 100f;
            s.displayed = true;
            if (bright < 1) {
                iter.remove();
            } else {
                int color = LXColor.gray(bright);
                for (Bundle b : s.bundles) {
                    for (int strip : b.strips) {
                        for (LXPoint p : model.getStripByIndex(strip).points) {
                            colors[p.index] = Ops8.lightest(colors[p.index], color, 1f);
                        }
                    }
                }
            }
        }
    }

    private Strike newStrike() {
        Strike s = new Strike();
        Component c = components.get(random.nextInt(components.size()));
        Junction start = c.edge.get(random.nextInt(c.edge.size()));
        int added = 0;
        int bolts = boltsParameter.getValuei();

        for (int attempts = 0; attempts < 10 * bolts && added < bolts; attempts++) {
            Junction end = c.center.get(random.nextInt(c.center.size()));
            List<Bundle> path;
            try {
                path = aStar.findPath(randomAdjacentBundle(start), randomAdjacentBundle(end));
            } catch (EdgeAStar.NotConnectedException e) {
                continue;
            }
            s.bundles.addAll(path);
            added++;
        }

        return s;
    }

    private Bundle randomAdjacentBundle(Junction j) {
        if (j.degree() == 0)
            return null;

        Sign[] signs = Sign.values();
        Dir[] dirs = Dir.values();
        while (true) {
            Sign s = signs[random.nextInt(signs.length)];
            Dir d = dirs[random.nextInt(dirs.length)];
            Bundle b = j.get(d, s);
            if (b != null)
                return b;
        }
    }
}
