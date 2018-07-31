package com.symmetrylabs.layouts.cubes.topology;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Random;

public class TopoTestPattern extends SLPattern<CubesModel> {
    private DiscreteParameter modeParam = new DiscreteParameter("mode", 0, 0, 4);
    private float elapsed = 0;
    private int i = 0;
    private Random r = new Random();
    private CubeTopology topology;

    public TopoTestPattern(LX lx) {
        super(lx);
        topology = new CubeTopology(model);
        addParameter(modeParam);
    }

    private void setStripColor(Strip s, int color) {
        for (LXPoint p : s.points) {
            colors[p.index] = color;
        }
    }
    private void setEdgeColor(CubeTopology.Bundle e, int color) {
        for (int strip : e.strips) {
            setStripColor(model.getStripByIndex(strip), color);
        }
    }

    @Override
    public void run(double deltaMs) {
        switch (modeParam.getValuei()) {
            case 0:
                for (CubeTopology.Bundle e : topology.edges) {
                    float h;
                    switch (e.dir) {
                        case X: h = 60; break;
                        case Y: h = 140; break;
                        case Z: h = 200; break;
                        default: h = 0;
                    }
                    setEdgeColor(e, LXColor.hsb(h, 100, 100));
                }
                break;

            case 1:
                elapsed += deltaMs;
                if (elapsed < 1000) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                CubeTopology.Bundle e = topology.edges.get(r.nextInt(topology.edges.size()));
                setEdgeColor(e, LXColor.rgb(255,255,255));
                for (CubeTopology.Bundle n = e.na; n != null; n = n.na) {
                    setEdgeColor(n, LXColor.rgb(0,255,0));
                }
                for (CubeTopology.Bundle p = e.pa; p != null; p = p.pa) {
                    setEdgeColor(p, LXColor.rgb(255,0,100));
                }
                break;

            case 2:
                elapsed += deltaMs;
                if (elapsed < 1000) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                i++;
                if (i > topology.edges.size())
                    i = 0;
                CubeTopology.Bundle edge = topology.edges.get(i);
                setEdgeColor(edge, LXColor.rgb(255,255,255));
                System.out.println(String.format("%d %d %d %d",
                    edge.strips.length > 0 ? edge.strips[0] : -1,
                    edge.strips.length > 1 ? edge.strips[1] : -1,
                    edge.strips.length > 2 ? edge.strips[2] : -1,
                    edge.strips.length > 3 ? edge.strips[3] : -1));
                break;

            case 3:
                setColors(0);
                setStripColor(model.getStripByIndex(3), LXColor.hsb(0, 100, 100));
                setStripColor(model.getStripByIndex(17), LXColor.hsb(80, 100, 100));
                setStripColor(model.getStripByIndex(57), LXColor.hsb(160, 100, 100));
                setStripColor(model.getStripByIndex(71), LXColor.hsb(240, 100, 100));
                break;
        }
    }
}
