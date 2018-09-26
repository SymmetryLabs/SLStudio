package com.symmetrylabs.slstudio.effect;

import heronarts.lx.transform.LXVector;
import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.util.ColorUtils;

public class RGBScale extends LXEffect {
    public final CompoundParameter rScale = new CompoundParameter("rScale", 1, 0, 10);
    public final CompoundParameter gScale = new CompoundParameter("gScale", 1, 0, 10);
    public final CompoundParameter bScale = new CompoundParameter("bScale", 1, 0, 10);

    public RGBScale(LX lx) {
        super(lx);

        addParameter(rScale);
        addParameter(gScale);
        addParameter(bScale);
    }

    @Override
    public void run(double deltaMs, double amount) {
        double rs = rScale.getValue();
        double gs = gScale.getValue();
        double bs = bScale.getValue();
        for (LXVector p : getVectors()) {
            int c = colors[p.index];
            int r = (int) Math.round(rs * LXColor.red(c));
            int b = (int) Math.round(gs * LXColor.green(c));
            int g = (int) Math.round(bs * LXColor.blue(c));
            colors[p.index] = LXColor.rgb(r, g, b);
        }
    }
}
