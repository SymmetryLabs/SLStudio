package com.symmetrylabs.slstudio.effect;

import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;

import com.symmetrylabs.util.ColorUtils;

public class DropHue extends LXEffect {
    public enum DropMode {
        BRIGHTNESS,
        SATURATION,
    }

    public final CompoundParameter hue = new CompoundParameter("hue", 0, 360);
    public final CompoundParameter width = new CompoundParameter("width", 0, 180);
    public final CompoundParameter ramp = new CompoundParameter("ramp", 0, 180);
    public final EnumParameter<DropMode> mode = new EnumParameter<>("mode", DropMode.BRIGHTNESS);

    public DropHue(LX lx) {
        super(lx);

        addParameter(hue);
        addParameter(width);
        addParameter(ramp);
        addParameter(mode);
    }

    @Override
    public void run(double deltaMs, double amount) {
        float center = hue.getValuef();
        float w = width.getValuef();
        float r = ramp.getValuef();
        DropMode m = mode.getEnum();

        for (LXVector p : getVectors()) {
            int c = colors[p.index];
            float h = LXColor.h(c);
            float dist = Math.abs(h - center);
            if (dist > 180) {
                dist = 360 - dist;
            }
            if (dist > w + r) {
                colors[p.index] = c;
            } else {
                float scale = dist < w ? 0 : (dist - w) / r;
                float s = LXColor.s(c);
                float b = LXColor.b(c);
                switch (m) {
                    case BRIGHTNESS: b *= scale; break;
                    case SATURATION: s *= scale; break;
                }
                colors[p.index] = LXColor.hsb(h, s, b);
            }
        }
    }
}
