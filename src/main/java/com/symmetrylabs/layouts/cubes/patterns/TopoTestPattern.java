package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsTopology;
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

    public TopoTestPattern(LX lx) {
        super(lx);
        addParameter(modeParam);
    }

    private void setStripColor(Strip s, int color) {
        for (LXPoint p : s.points) {
            colors[p.index] = color;
        }
    }
    private void setEdgeColor(StripsTopology.Bundle e, int color) {
        for (int strip : e.strips) {
            setStripColor(model.getStripByIndex(strip), color);
        }
    }

    @Override
    public void run(double deltaMs) {
        switch (modeParam.getValuei()) {
            case 0:
                for (StripsTopology.Bundle e : model.getTopology().edges) {
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
                if (elapsed < 500) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                StripsTopology topo = model.getTopology();
                StripsTopology.Bundle e = topo.edges.get(r.nextInt(topo.edges.size()));
                setEdgeColor(e, LXColor.rgb(255,255,255));
                for (StripsTopology.Bundle n = e.na; n != null; n = n.na) {
                    setEdgeColor(n, LXColor.rgb(0,255,0));
                }
                for (StripsTopology.Bundle p = e.pa; p != null; p = p.pa) {
                    setEdgeColor(p, LXColor.rgb(255,0,100));
                }
                break;

            case 2: {
                elapsed += deltaMs;
                if (elapsed < 500) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                i++;
                if (i > model.getTopology().edges.size())
                    i = 0;
                StripsTopology.Bundle edge = model.getTopology().edges.get(i);
                setEdgeColor(edge, LXColor.rgb(255, 255, 255));
                System.out.println(String.format("%d / %d %d %d %d",
                    i,
                    edge.strips.length > 0 ? edge.strips[0] : -1,
                    edge.strips.length > 1 ? edge.strips[1] : -1,
                    edge.strips.length > 2 ? edge.strips[2] : -1,
                    edge.strips.length > 3 ? edge.strips[3] : -1));
                break;
            }

            case 3: {
                elapsed += deltaMs;
                if (elapsed < 500) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                i++;
                if (i > model.getTopology().edges.size())
                    i = 0;
                System.out.println(i);
                StripsTopology.Bundle edge = model.getTopology().edges.get(i);
                setEdgeColor(edge, LXColor.rgb(255, 255, 255));
                if (edge.na != null) setEdgeColor(edge.na, LXColor.hsb(0, 100, 100));
                if (edge.nbn != null) setEdgeColor(edge.nbn, LXColor.hsb(30, 100, 100));
                if (edge.ncn != null) setEdgeColor(edge.ncn, LXColor.hsb(60, 100, 100));
                if (edge.nbp != null) setEdgeColor(edge.nbp, LXColor.hsb(90, 100, 100));
                if (edge.ncp != null) setEdgeColor(edge.ncp, LXColor.hsb(120, 100, 100));
                if (edge.pa != null) setEdgeColor(edge.pa, LXColor.hsb(150, 100, 100));
                if (edge.pbn != null) setEdgeColor(edge.pbn, LXColor.hsb(180, 100, 100));
                if (edge.pcn != null) setEdgeColor(edge.pcn, LXColor.hsb(210, 100, 100));
                if (edge.pbp != null) setEdgeColor(edge.pbp, LXColor.hsb(240, 100, 100));
                if (edge.pcp != null) setEdgeColor(edge.pcp, LXColor.hsb(270, 100, 100));
                break;
            }
        }
    }
}
