package com.symmetrylabs.shows.cubes.patterns;

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
        setBundleColor(b, LXColor.rgb(255, 255, 255));
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
    }

    private String index(Bundle b) {
        if (b == null) {
            return "-";
        }
        return Integer.toString(b.index);
    }

    @Override
    public String getCaption() {
        Bundle b = null;
        if (modeParam.getEnum() == Mode.ITER) {
            b = model.getTopology().bundles.get(i);
        } else if (modeParam.getEnum() == Mode.BUNDLE) {
            b = model.getTopology().bundles.get(bundleParam.getValuei());
        } else {
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
            SLStudio.setWarning("TopoTestPattern", "no topology on model");
            return;
        }
        SLStudio.setWarning("TopoTestPattern", null);

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
