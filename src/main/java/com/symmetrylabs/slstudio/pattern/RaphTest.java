package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.transform.*;
import heronarts.lx.LXPattern;
import heronarts.lx.color.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;

import java.awt.*;
import java.util.*;
import java.util.HashMap;
import java.util.LinkedList;

public class RaphTest extends LXPattern {
    SinLFO lfo;
    CompoundParameter size;


    LinkedList<HashMap<Integer, Integer>> frameStates = new LinkedList<>();


    int framesPer = 1000;
    int n = 100;
    int gW = 16;
    LXVector[] destinations;
    LXVector[] currents;
    LXVector[] dirs;
    boolean assignGrid = false;

    float[] hues = new float[n];


    float random(float min, float range) {
        return min + (float)Math.random() * range;
    }

    LXVector randomModelVector() {
        return new LXVector(random(model.xMin, model.xRange), random(model.yMin, model.yRange), random(model.zMin, model.zRange));
    }

    synchronized void putColor(HashMap<Integer, Integer> m, int k, int v) {
        m.put(k, v);
    }

    void assignDestinations() {
        assignGrid = true;
        for (int i = 0; i < n; i++) {
            if (assignGrid) {
                int x = i % gW;
                int y = i / gW;
                float gH = (float)n / (float)gW;
                LXVector randZ = randomModelVector();
                randZ.x = model.xMin + ((model.xRange / gW) * x);
                randZ.y = model.yMin + ((model.yRange / gH) * y);
                destinations[i] = randZ;
            } else {
                destinations[i] = randomModelVector();
            }
            LXVector vec = destinations[i].copy().add(currents[i].copy().mult(-1));
            vec.div(framesPer);
            dirs[i] = vec;
        }
//        assignGrid = !assignGrid;

    }


    public RaphTest(LX lx) {
        super(lx);

//        lfo = new SinLFO(0, 1, 20000);
//        startModulator(lfo);
        size = new CompoundParameter("size", 40, 30, 120);
        addParameter(size);

        destinations = new LXVector[n];
        currents = new LXVector[n];
        dirs = new LXVector[n];


        for (int i = 0; i < n; i++) {
            currents[i] = randomModelVector();
        }

        for (int i = 0; i < n; i++) {
            hues[i] = random(275, 75);
        }

        assignDestinations();

    }

    double dAcc = 0;
    int fc = 0;



    @Override
    protected void run(double deltaMs) {
        dAcc += deltaMs;
        fc++;
//        dAcc += deltaMs;
//        if (dAcc > 100) {
//            dAcc = 0;
//            for (int i = 0; i < colors.length; i++) {
//                float h = LXColor.h(colors[i]);
//                float s = LXColor.s(colors[i]);
//                float b = LXColor.b(colors[i]);
//                colors[i] = LXColor.hsb(h, s * 0.97, b * 0.999);
//            }
//        }

        float s = size.getValuef();
        double sSq = Math.pow(s, 2);


        HashMap<Integer, Integer> fs = new HashMap<>();

        for (int i = 0; i < n; i++) {
            double d = (Math.random() * 2) - 1;
            hues[i] = (float)Math.max(275, Math.min(350, hues[i] + d));
        }

        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.BLACK;
        }

//        for (HashMap<Integer, Integer> old : frameStates) {
//            for (Map.Entry<Integer, Integer> e : old.entrySet()) {
//                colors[e.getKey()] = e.getValue();
//            }
//        }


        model.getPoints().parallelStream().forEach(p -> {
            for (int i = 0; i < n; i++) {
                LXVector c = currents[i];
                double d = Math.sqrt(Math.pow(c.x - p.x, 2) + Math.pow(c.y - p.y, 2) + Math.pow(c.z - p.z, 2));
                if (d < s) {
                    int col = LXColor.hsb(hues[i], 100, (1.0 - d/s) * 100);
//                    putColor(fs, p.index, col);
//                    fs.put(p.index, col);
                    colors[p.index] = col;
                    return;
                }
            }
//            colors[p.index] = LXColor.BLACK;
        });

        if (dAcc > 100) {
            frameStates.add(fs);
            dAcc = 0;
        }

        for (int i = 0; i < n; i++) {
            currents[i].add(dirs[i]);
        }

        if (fc == framesPer) {
            fc = 0;
            assignDestinations();
        }

//        boolean allClose = true;
//        for (int i = 0; i < n; i++) {
//            LXVector v = destinations[i].copy();
//            LXVector minus = currents[i].copy().mult(-1);
//            float step = distances[i] / 100;
//            LXVector dir = v.add(minus).normalize().mult(step);
//            float remain = minus.mag();
//            if (remain > step) {
//                allClose = false;
//                currents[i].add(dir);
//            } else {
////                destinations[i] = randomModelVector();
//            }
//        }
//
//        if (allClose) {
//            assignDestinations();
//        }

//        if (frameStates.size() > 10) {
//            frameStates.removeFirst();
//        }

//        if (allClose) {
//            for (int i = 0; i < n; i++) {
//                destinations[i] = randomModelVector();
//            }
//        }

    }
}
