package com.symmetrylabs.slstudio.palettes;

import heronarts.lx.*;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.LXParameter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SwatchLibrary implements Iterable<SwatchLibrary.Swatch> {
    public static class Swatch {
        public final ColorParameter color;
        public final int index;

        public Swatch(int index, float h, float s, float b) {
            this.index = index;
            color = new ColorParameter("Swatch" + index);
            color.hue.setValue(h);
            color.saturation.setValue(s);
            color.brightness.setValue(b);
        }

        public void apply(LXLook look) {
            for (LXChannel chan : look.channels) {
                for (LXPattern pat : chan.getPatterns()) {
                    apply(pat);
                }
                for (LXEffect eff : chan.getEffects()) {
                    apply(eff);
                }
            }
        }

        public void apply(LXComponent c) {
            for (LXParameter param : c.getParameters()) {
                if (param instanceof ColorParameter) {
                    ColorParameter cp = (ColorParameter) param;
                    cp.hue.setValue(color.hue.getValue());
                    cp.saturation.setValue(color.saturation.getValue());
                    cp.brightness.setValue(color.brightness.getValue());
                } else if (param.getLabel().toLowerCase().equals("hue")) {
                    param.setValue(color.hue.getValue());
                } else if (param.getLabel().toLowerCase().equals("saturation")) {
                    param.setValue(color.saturation.getValue());
                } else if (param.getLabel().toLowerCase().equals("sat")) {
                    param.setValue(color.saturation.getValue());
                } else if (param.getLabel().toLowerCase().equals("color")) {
                    param.setValue(color.hue.getValue());
                }
            }
        }
    }

    public final List<Swatch> swatches;

    public SwatchLibrary() {
        swatches = new ArrayList<>();
    }

    @NotNull
    @Override
    public Iterator<Swatch> iterator() {
        return swatches.iterator();
    }

    public static SwatchLibrary getDefault() {
        SwatchLibrary sl = new SwatchLibrary();
        sl.swatches.add(new Swatch(0, 0, 100, 100));
        sl.swatches.add(new Swatch(1, 90, 100, 100));
        sl.swatches.add(new Swatch(2, 180, 100, 100));
        sl.swatches.add(new Swatch(3, 270, 100, 100));
        return sl;
    }
}
