package com.symmetrylabs.patterns;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.parameter.*;
import heronarts.lx.modulator.*;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;

public class CrossSections extends LXPattern {

    final SinLFO x = new SinLFO(model.xMin, model.xMax, 5000);
    final SinLFO y = new SinLFO(model.yMin, model.yMax, 6000);
    final SinLFO z = new SinLFO(model.zMin, model.zMax, 7000);

    final CompoundParameter xw = new CompoundParameter("XWID", 0.3);
    final CompoundParameter yw = new CompoundParameter("YWID", 0.3);
    final CompoundParameter zw = new CompoundParameter("ZWID", 0.3);
    final CompoundParameter xr = new CompoundParameter("XRAT", 0.7);
    final CompoundParameter yr = new CompoundParameter("YRAT", 0.6);
    final CompoundParameter zr = new CompoundParameter("ZRAT", 0.5);
    final CompoundParameter xl = new CompoundParameter("XLEV", 1);
    final CompoundParameter yl = new CompoundParameter("YLEV", 1);
    final CompoundParameter zl = new CompoundParameter("ZLEV", 0.5);

    public CrossSections(LX lx) {
        super(lx);
        addModulator(x).trigger();
        addModulator(y).trigger();
        addModulator(z).trigger();
        addParams();
    }

    protected void addParams() {
        addParameter(xr);
        addParameter(yr);
        addParameter(zr);
        addParameter(xw);
        addParameter(xl);
        addParameter(yl);
        addParameter(zl);
        addParameter(yw);
        addParameter(zw);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == xr) {
            x.setPeriod(10000 - 8800*p.getValuef());
        } else if (p == yr) {
            y.setPeriod(10000 - 9000*p.getValuef());
        } else if (p == zr) {
            z.setPeriod(10000 - 9000*p.getValuef());
        }
    }

    float xv, yv, zv;

    protected void updateXYZVals() {
        xv = x.getValuef();
        yv = y.getValuef();
        zv = z.getValuef();
    }

    public void run(double deltaMs) {
        updateXYZVals();

        float xlv = 100 * xl.getValuef();
        float ylv = 100 * yl.getValuef();
        float zlv = 100 * zl.getValuef();

        float xwv = 100f / (10 + 40 * xw.getValuef());
        float ywv = 100f / (10 + 40 * yw.getValuef());
        float zwv = 100f / (10 + 40 * zw.getValuef());

        for (LXPoint p : model.points) {
            int c = 0;
            c = LXColor.blend(c, lx.hsb(
                palette.getHuef() + p.x/10 + p.y/3,
                MathUtils.constrain(140 - 1.1f * (float)Math.abs(p.x - model.xMax / 2f), 0, 100),
                Math.max(0, xlv - xwv * (float)Math.abs(p.x - xv))
            ), LXColor.Blend.ADD);
            c = LXColor.blend(c, lx.hsb(
                palette.getHuef() + 80 + p.y/10,
                MathUtils.constrain(140 - 2.2f * (float)Math.abs(p.y - model.yMax / 2f), 0, 100),
                Math.max(0, ylv - ywv * (float)Math.abs(p.y - yv))
            ), LXColor.Blend.ADD);
            c = LXColor.blend(c, lx.hsb(
                palette.getHuef() + 160 + p.z / 10 + p.y/2,
                MathUtils.constrain(140 - 2.2f * (float)Math.abs(p.z - model.zMax / 2f), 0, 100),
                Math.max(0, zlv - zwv * (float)Math.abs(p.z - zv))
            ), LXColor.Blend.ADD);
            colors[p.index] = c;
        }
    }
}
