package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.layouts.cubes.topology.CubeTopology;
import com.symmetrylabs.layouts.cubes.topology.EdgeAStar;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class PilotsTree extends SLPattern<CubesModel> {
    double elapsed = 0;
    CubeTopology topology;
    EdgeAStar aStar;

    public PilotsTree(LX lx) {
        super(lx);
        topology = new CubeTopology(model);
        aStar = new EdgeAStar(topology);
    }

    @Override
    public void run(double deltaMs) {
        elapsed += deltaMs;
        if (elapsed < 100)
            return;
        elapsed = 0;

        setColors(0);

        Random r = new Random();
        int ia = r.nextInt(topology.edges.size());
        int ib = r.nextInt(topology.edges.size());
        CubeTopology.Bundle a = topology.edges.get(ia);
        CubeTopology.Bundle b = topology.edges.get(ib);

        List<CubeTopology.Bundle> path = null;
        try {
            path = aStar.findPath(a, b);
        } catch (EdgeAStar.NotConnectedException ex) {
            System.out.println(String.format("Error in path planning from %d to %d", ia, ib));
            ex.printStackTrace();
        }

        if (path != null) {
            for (CubeTopology.Bundle e : path) {
                for (int strip : e.strips) {
                    for (LXPoint p : model.getStripByIndex(strip).points) {
                        colors[p.index] = LXColor.gray(100f);
                    }
                }
            }
        }
        for (int strip : a.strips) {
            for (LXPoint p : model.getStripByIndex(strip).points) {
                colors[p.index] = LXColor.rgb(255,0,100);
            }
        }
        for (int strip : b.strips) {
            for (LXPoint p : model.getStripByIndex(strip).points) {
                colors[p.index] = LXColor.rgb(0,255,0);
            }
        }
    }
}
