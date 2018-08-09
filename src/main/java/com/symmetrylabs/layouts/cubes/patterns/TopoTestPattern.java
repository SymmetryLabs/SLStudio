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
    private DiscreteParameter modeParam = new DiscreteParameter("mode", 0, 0, 2);
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
                float h = 0;
                for (StripsTopology.Sign end : StripsTopology.Sign.values()) {
                    for (StripsTopology.Sign s : StripsTopology.Sign.values()) {
                        for (StripsTopology.Dir d : StripsTopology.Dir.values()) {
                            StripsTopology.Bundle b = edge.get(end).get(d, s);
                            if (b != null) setEdgeColor(b, LXColor.hsb(h, 100, 100));
                            h += 30;
                        }
                    }
                }
                break;
            }
        }
    }
}
