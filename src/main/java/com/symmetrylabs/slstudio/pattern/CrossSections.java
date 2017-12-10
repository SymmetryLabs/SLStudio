package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.util.FastHSB;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import processing.core.PImage;

import static processing.core.PApplet.*;


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

    public void onParameterChanged(LXParameter p) {
        if (p == xr) {
            x.setPeriod(10000 - 8800 * p.getValuef());
        } else if (p == yr) {
            y.setPeriod(10000 - 9000 * p.getValuef());
        } else if (p == zr) {
            z.setPeriod(10000 - 9000 * p.getValuef());
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
            c = PImage.blendColor(c, FastHSB.hsb(
                palette.getHuef() + p.x / 10 + p.y / 3,
                constrain(140 - 1.1f * abs(p.x - model.xMax / 2f), 0, 100),
                max(0, xlv - xwv * abs(p.x - xv))
            ), ADD);
            c = PImage.blendColor(c, FastHSB.hsb(
                palette.getHuef() + 80 + p.y / 10,
                constrain(140 - 2.2f * abs(p.y - model.yMax / 2f), 0, 100),
                max(0, ylv - ywv * abs(p.y - yv))
            ), ADD);
            c = PImage.blendColor(c, FastHSB.hsb(
                palette.getHuef() + 160 + p.z / 10 + p.y / 2,
                constrain(140 - 2.2f * abs(p.z - model.zMax / 2f), 0, 100),
                max(0, zlv - zwv * abs(p.z - zv))
            ), ADD);
            colors[p.index] = c;
        }
    }
}
