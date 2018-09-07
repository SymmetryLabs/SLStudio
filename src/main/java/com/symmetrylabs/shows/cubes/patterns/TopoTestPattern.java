package com.symmetrylabs.shows.cubes.patterns;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.StripsTopologyComponents;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.EnumParameter;

import java.util.ArrayList;
import java.util.List;

public class TopoTestPattern<T extends Strip> extends SLPattern<StripsModel<T>> {
    public enum Mode {
        DIRS,
        ONE_BY_ONE,
        COMPONENTS,
    }

    private EnumParameter<Mode> modeParam = new EnumParameter<>("mode", Mode.DIRS);
    private float elapsed = 0;
    private int i = 0;
    List<StripsTopologyComponents.ConnectedComponent> components;

    public TopoTestPattern(LX lx) {
        super(lx);
        addParameter(modeParam);

        if (model.getTopology() != null) {
            StripsTopologyComponents stc = new StripsTopologyComponents(model.getTopology());
            components = stc.getComponents();
        } else {
            components = new ArrayList<>();
        }
    }

    private void setStripColor(Strip s, int color) {
        for (LXPoint p : s.points) {
            colors[p.index] = color;
        }
    }

    private void setBundleColor(StripsTopology.Bundle e, int color) {
        for (int strip : e.strips) {
            setStripColor(model.getStripByIndex(strip), color);
        }
    }

    @Override
    public void run(double deltaMs) {
        switch (modeParam.getEnum()) {
            case DIRS: {
                if (model.getTopology() == null) {
                    return;
                }
                for (StripsTopology.Bundle e : model.getTopology().bundles) {
                    float h;
                    switch (e.dir) {
                        case X:
                            h = 60;
                            break;
                        case Y:
                            h = 140;
                            break;
                        case Z:
                            h = 200;
                            break;
                        default:
                            h = 0;
                    }
                    setBundleColor(e, LXColor.hsb(h, 100, 100));
                }
                break;
            }

            case ONE_BY_ONE: {
                if (model.getTopology() == null) {
                    return;
                }
                elapsed += deltaMs;
                if (elapsed < 500) {
                    break;
                }
                elapsed = 0;
                setColors(0);
                i++;
                if (i > model.getTopology().bundles.size()) {
                    i = 0;
                }
                System.out.println(i);
                StripsTopology.Bundle edge = model.getTopology().bundles.get(i);
                setBundleColor(edge, LXColor.rgb(255, 255, 255));
                float h = 0;
                for (StripsTopology.Sign end : StripsTopology.Sign.values()) {
                    for (StripsTopology.Sign s : StripsTopology.Sign.values()) {
                        for (StripsTopology.Dir d : StripsTopology.Dir.values()) {
                            StripsTopology.Bundle b = edge.get(end).get(d, s);
                            if (b != null) {
                                setBundleColor(b, LXColor.hsb(h, 100, 100));
                            }
                            h += 30;
                        }
                    }
                }
                break;
            }

            case COMPONENTS: {
                float hstep = 330f / components.size();
                int h = 0;
                for (StripsTopologyComponents.ConnectedComponent cc : components) {
                    int c = LXColor.hsb(h, 100, 100);
                    h += hstep;
                    for (StripsTopology.Junction j : cc.junctions) {
                        for (StripsTopology.Bundle b : j.getBundles()) {
                            setBundleColor(b, c);
                        }
                    }
                }
            }
        }
    }
}
