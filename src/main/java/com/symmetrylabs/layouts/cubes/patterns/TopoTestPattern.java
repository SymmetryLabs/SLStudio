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
            case 0: {
                if (model.getTopology() == null)
                    return;
                for (StripsTopology.Bundle e : model.getTopology().bundles) {
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
            }

            case 1: {
                if (model.getTopology() == null)
                    return;
                elapsed += deltaMs;
                if (elapsed < 500) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                i++;
                if (i > model.getTopology().bundles.size())
                    i = 0;
                System.out.println(i);
                StripsTopology.Bundle edge = model.getTopology().bundles.get(i);
                setEdgeColor(edge, LXColor.rgb(255, 255, 255));
                if (edge.n.nx != null && edge.n.nx != edge)
                    setEdgeColor(edge.n.nx, LXColor.hsb(0, 100, 100));
                if (edge.n.px != null && edge.n.px != edge)
                    setEdgeColor(edge.n.px, LXColor.hsb(30, 100, 100));
                if (edge.n.ny != null && edge.n.ny != edge)
                    setEdgeColor(edge.n.ny, LXColor.hsb(60, 100, 100));
                if (edge.n.py != null && edge.n.py != edge)
                    setEdgeColor(edge.n.py, LXColor.hsb(90, 100, 100));
                if (edge.n.nz != null && edge.n.nz != edge)
                    setEdgeColor(edge.n.nz, LXColor.hsb(120, 100, 100));
                if (edge.n.pz != null && edge.n.pz != edge)
                    setEdgeColor(edge.n.pz, LXColor.hsb(150, 100, 100));
                if (edge.p.nx != null && edge.p.nx != edge)
                    setEdgeColor(edge.p.nx, LXColor.hsb(180, 100, 100));
                if (edge.p.px != null && edge.p.px != edge)
                    setEdgeColor(edge.p.px, LXColor.hsb(210, 100, 100));
                if (edge.p.ny != null && edge.p.ny != edge)
                    setEdgeColor(edge.p.ny, LXColor.hsb(240, 100, 100));
                if (edge.p.py != null && edge.p.py != edge)
                    setEdgeColor(edge.p.py, LXColor.hsb(270, 100, 100));
                if (edge.p.nz != null && edge.p.nz != edge)
                    setEdgeColor(edge.p.nz, LXColor.hsb(300, 100, 100));
                if (edge.p.pz != null && edge.p.pz != edge)
                    setEdgeColor(edge.p.pz, LXColor.hsb(330, 100, 100));
                break;
            }
        }
    }
}
