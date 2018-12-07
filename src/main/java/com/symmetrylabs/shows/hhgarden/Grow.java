package com.symmetrylabs.shows.hhgarden;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Grow extends FlowerPattern {
    private final CompoundParameter rateParam =
        new CompoundParameter("rate", 5, 0.1, 10);
    private final CompoundParameter growParam =
        new CompoundParameter("grow", 0.01, 0.5, 10);
    private final BooleanParameter resetParam =
        new BooleanParameter("reset", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final DiscreteParameter zonesParam =
        new DiscreteParameter("zones", 5, 1, 20);

    private final double NEIGHBOR_MAX_DIST = 60;

    private static class Neighbor implements Comparable<Neighbor> {
        FlowerModel m;
        double d;

        public int compareTo(Neighbor n) {
            return Double.compare(d, n.d);
        }
    }

    private static class Visited {
        FlowerModel m;
        int zone;

        Visited(FlowerModel m, int zone) {
            this.m = m;
            this.zone = zone;
        }
    }

    private HashMap<FlowerModel, List<Neighbor>> neighbors;
    private HashSet<FlowerModel> inside = new HashSet<>();
    private List<Visited> fringe = new ArrayList<>();
    private Random rand = new Random();
    private double tAccum = 0;
    private boolean inverting = false;
    private double[] age;

    public Grow(LX lx) {
        super(lx);
        addParameter(rateParam);
        addParameter(growParam);
        addParameter(resetParam);
        addParameter(zonesParam);

        neighbors = new HashMap<>();
        for (FlowerModel fm1 : model.getFlowers()) {
            neighbors.put(fm1, new ArrayList<>());
            LXVector v1 = fm1.getFlowerData().location;
            for (FlowerModel fm2 : model.getFlowers()) {
                LXVector v2 = fm2.getFlowerData().location;
                double d = v1.dist(v2);
                if (d < NEIGHBOR_MAX_DIST) {
                    Neighbor n = new Neighbor();
                    n.m = fm2;
                    n.d = d;
                    neighbors.get(fm1).add(n);
                }
            }
            Collections.sort(neighbors.get(fm1));
        }

        age = new double[model.getFlowers().size()];
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == resetParam && resetParam.getValueb()) {
            reset();
        }
    }

    private boolean fringeContains(FlowerModel m) {
        for (Visited v : fringe) {
            if (v.m == m) {
                return true;
            }
        }
        return false;
    }

    private void reset() {
        inside.clear();
        fringe.clear();
        Arrays.fill(age, 0.0);
    }

    private void finishOneFringe() {
        if (inside.size() == model.getFlowers().size()) {
            boolean allDone = true;
            for (double a : age) {
                if (a < 0.9999) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                inverting = !inverting;
                reset();
            }
        }
        if (fringe.isEmpty() && inside.size() != model.getFlowers().size()) {
            List<FlowerModel> avail = new ArrayList<FlowerModel>();
            for (FlowerModel f : model.getFlowers()) {
                if (!inside.contains(f)) {
                    avail.add(f);
                }
            }
            Collections.shuffle(avail);
            for (int i = 0; i < avail.size() && i < zonesParam.getValuei(); i++) {
                fringe.add(new Visited(avail.get(i), i));
            }
        }

        List<Visited> finished = new ArrayList<>();
        HashSet<Integer> zonesDone = new HashSet<>();
        List<Visited> newFringe = new ArrayList<>();
        for (Visited f : fringe) {
            if (zonesDone.contains(f.zone)) {
                continue;
            }
            boolean added = false;
            for (Neighbor n : neighbors.get(f.m)) {
                if (inside.contains(n.m) || fringeContains(n.m)) {
                    continue;
                }
                newFringe.add(new Visited(n.m, f.zone));
                added = true;
            }

            if (added) {
                finished.add(f);
                zonesDone.add(f.zone);
            } else {
                /* this node has no neighbors that aren't in our set, so we can
                     remove it from the fringe */
                finished.add(f);
                zonesDone.add(f.zone);
            }
        }

        fringe.removeAll(finished);
        fringe.addAll(newFringe);
        for (Visited v : finished) {
            inside.add(v.m);
        }
    }

    @Override
    public void run(double elapsedMs) {
        tAccum += elapsedMs;
        double rate = rateParam.getValue();
        while (tAccum > rate) {
            finishOneFringe();
            tAccum -= rate;
        }

        double grow = elapsedMs / 1000 * growParam.getValue();
        List<FlowerModel> fms = model.getFlowers();
        for (int i = 0; i < fms.size(); i++) {
            FlowerModel fm = fms.get(i);
            if (inside.contains(fm) || fringe.contains(fm)) {
                age[i] = Double.min(grow + age[i], 1.0);
            }
            float gray = 100.f * (float) age[i];
            if (inverting) {
                gray = 100.f - gray;
            }
            for (LXPoint p : fm.points) {
                colors[p.index] = LXColor.gray(gray);
            }
        }
    }
}
