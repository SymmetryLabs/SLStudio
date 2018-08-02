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
            }

            case 1: {
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
                if (edge.nxn != null) setEdgeColor(edge.nxn, LXColor.hsb(0, 100, 100));
                if (edge.nxp != null) setEdgeColor(edge.nxp, LXColor.hsb(30, 100, 100));
                if (edge.nyn != null) setEdgeColor(edge.nyn, LXColor.hsb(60, 100, 100));
                if (edge.nyp != null) setEdgeColor(edge.nyp, LXColor.hsb(90, 100, 100));
                if (edge.nzn != null) setEdgeColor(edge.nzn, LXColor.hsb(120, 100, 100));
                if (edge.nzp != null) setEdgeColor(edge.nzp, LXColor.hsb(150, 100, 100));
                if (edge.pxn != null) setEdgeColor(edge.pxn, LXColor.hsb(180, 100, 100));
                if (edge.pxp != null) setEdgeColor(edge.pxp, LXColor.hsb(210, 100, 100));
                if (edge.pyn != null) setEdgeColor(edge.pyn, LXColor.hsb(240, 100, 100));
                if (edge.pyp != null) setEdgeColor(edge.pyp, LXColor.hsb(270, 100, 100));
                if (edge.pzn != null) setEdgeColor(edge.pzn, LXColor.hsb(300, 100, 100));
                if (edge.pzp != null) setEdgeColor(edge.pzp, LXColor.hsb(330, 100, 100));
                break;
            }
        }
    }
}
