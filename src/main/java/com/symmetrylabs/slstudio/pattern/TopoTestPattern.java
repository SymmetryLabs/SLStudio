package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Junction;
import com.symmetrylabs.slstudio.model.StripsTopology.Sign;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.StripsTopologyComponents;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TopoTestPattern<T extends Strip> extends SLPattern<StripsModel<T>> {
    public enum Mode {
        DIRS,
        ITER,
        CCS,
        BUNDLE,
    }

    private EnumParameter<Mode> modeParam = new EnumParameter<>("mode", Mode.DIRS);
    private DiscreteParameter bundleParam;
    private float elapsed = 0;
    private int i = 0;
    List<StripsTopologyComponents.ConnectedComponent> components;

    public TopoTestPattern(LX lx) {
        super(lx);
        addParameter(modeParam);

        if (model.getTopology() != null) {
            StripsTopologyComponents stc = new StripsTopologyComponents(model.getTopology());
            components = stc.getComponents();
            bundleParam = new DiscreteParameter("bundle", 0, 0, model.getTopology().bundles.size());
            addParameter(bundleParam);
        } else {
            components = new ArrayList<>();
            SLStudio.setWarning("TopoTestPattern", "no topology on model");
        }
    }

    private void setStripColor(Strip s, int color) {
        for (LXPoint p : s.points) {
            colors[p.index] = color;
        }
    }

    private void setBundleColor(Bundle e, int color) {
        for (int strip : e.strips) {
            setStripColor(model.getStripByIndex(strip), color);
        }
    }

    private void paintBundleNeighbors(Bundle b) {
        float h = 0;
        for (Sign end : StripsTopology.Sign.values()) {
            for (Sign s : StripsTopology.Sign.values()) {
                for (Dir d : StripsTopology.Dir.values()) {
                    Bundle n = b.get(end).get(d, s);
                    if (n != null) {
                        setBundleColor(n, LXColor.hsb(h, 100, 100));
                    }
                    h += 30;
                }
            }
        }
        setBundleColor(b, LXColor.rgb(255, 255, 255));
    }

    private String index(Bundle b) {
        if (b == null) {
            return "-";
        }
        return Integer.toString(b.index);
    }

    @Override
    public String getCaption() {
        if (model.getTopology() == null) {
            return null;
        }
        Bundle b = null;
        switch (modeParam.getEnum()) {
            case ITER:
                b = model.getTopology().bundles.get(i);
                break;
            case BUNDLE:
                b = model.getTopology().bundles.get(bundleParam.getValuei());
                break;
            case CCS:
                return String.format("%d components", components.size());
            default:
                return null;
        }

        return String.format(
            "%s / PXP %s PXN %s PYP %s PYN %s PZP %s PZN %s NXP %s NXN %s NYP %s NYN %s NZP %s NZN %s",
            index(b),
            index(b.get(Sign.POS).get(Dir.X, Sign.POS)),
            index(b.get(Sign.POS).get(Dir.X, Sign.NEG)),
            index(b.get(Sign.POS).get(Dir.Y, Sign.POS)),
            index(b.get(Sign.POS).get(Dir.Y, Sign.NEG)),
            index(b.get(Sign.POS).get(Dir.Z, Sign.POS)),
            index(b.get(Sign.POS).get(Dir.Z, Sign.NEG)),
            index(b.get(Sign.NEG).get(Dir.X, Sign.POS)),
            index(b.get(Sign.NEG).get(Dir.X, Sign.NEG)),
            index(b.get(Sign.NEG).get(Dir.Y, Sign.POS)),
            index(b.get(Sign.NEG).get(Dir.Y, Sign.NEG)),
            index(b.get(Sign.NEG).get(Dir.Z, Sign.POS)),
            index(b.get(Sign.NEG).get(Dir.Z, Sign.NEG)));
    }

    @Override
    public void run(double deltaMs) {
        if (model.getTopology() == null) {
            return;
        }

        Arrays.fill(colors, 0);

        switch (modeParam.getEnum()) {
            case DIRS: {
                for (Bundle e : model.getTopology().bundles) {
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

            case ITER: {
                elapsed += deltaMs;
                if (elapsed > 500) {
                    elapsed = 0;
                    i = (i + 1) % model.getTopology().bundles.size();
                }
                Bundle b = model.getTopology().bundles.get(i);
                paintBundleNeighbors(b);
                break;
            }

            case CCS: {
                float hstep = 330f / components.size();
                int h = 0;
                for (StripsTopologyComponents.ConnectedComponent cc : components) {
                    int c = LXColor.hsb(h, 100, 100);
                    h += hstep;
                    for (Junction j : cc.junctions) {
                        for (Bundle b : j.getBundles()) {
                            setBundleColor(b, c);
                        }
                    }
                }
                break;
            }

            case BUNDLE: {
                paintBundleNeighbors(model.getTopology().bundles.get(bundleParam.getValuei()));
                break;
            }
        }
    }
}
