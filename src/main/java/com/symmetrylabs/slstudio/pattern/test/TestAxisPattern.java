package com.symmetrylabs.slstudio.pattern;
import static processing.core.PApplet.*;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.util.MathUtils;



public class TestAxisPattern extends LXPattern {

    final CompoundParameter x = new CompoundParameter("XPOS", 0.3);
    final CompoundParameter y = new CompoundParameter("YPOS", 0.3);
    final CompoundParameter z = new CompoundParameter("ZPOS", 0.3);
    final CompoundParameter xw = new CompoundParameter("XWID", 0.3);
    final CompoundParameter yw = new CompoundParameter("YWID", 0.3);
    final CompoundParameter zw = new CompoundParameter("ZWID", 0.3);
    final CompoundParameter xl = new CompoundParameter("XLEV", 1);
    final CompoundParameter yl = new CompoundParameter("YLEV", 1);
    final CompoundParameter zl = new CompoundParameter("ZLEV", 0.5);
    float xv, yv, zv;

    final SinLFO lfo = new SinLFO("Stuff", 0, 1, 2000);

    public TestAxisPattern(LX lx) {
        super(lx);
        addParameter(x);
        addParameter(y);
        addParameter(z);
        addParameter(xw);
        addParameter(yw);
        addParameter(zw);
        addParameter(xl);
        addParameter(yl);
        addParameter(zl);
        startModulator(lfo);
    }

    protected void updateXYZVals() {
        xv = x.getValuef() * model.xRange;
        yv = y.getValuef() * model.yRange;
        zv = z.getValuef() * model.zRange;
    }


    public void run(double deltaMs) {
        updateXYZVals();

        float xlv = 100*xl.getValuef();
        float ylv = 100*yl.getValuef();
        float zlv = 100*zl.getValuef();

        float xwv = 100.0f / (10 + 40*xw.getValuef());
        float ywv = 100.0f / (10 + 40*yw.getValuef());
        float zwv = 100.0f / (10 + 40*zw.getValuef());

        for (LXPoint p : model.points) {
            int c = 0;
            c = LXColor.blend(c, lx.hsb(
                (lx.palette.getHuef() + p.x/10 + p.y/3) % 360,
                MathUtils.constrain((float)(140 - 1.1*Math.abs(p.x - model.xMax/2.)), 0, 100),
                max(0, xlv - xwv*abs(p.x - xv))
            ), LXColor.Blend.ADD);
            c = LXColor.blend(c, lx.hsb(
                (lx.palette.getHuef() + 80 + p.y/10) % 360,
                MathUtils.constrain((float)(140 - 2.2*Math.abs(p.y - model.yMax/2.)), 0, 100),
                max(0, ylv - ywv*abs(p.y - yv))
            ), LXColor.Blend.ADD);
            c = LXColor.blend(c, lx.hsb(
                (lx.palette.getHuef() + 160 + p.z / 10 + p.y/2) % 360,
                MathUtils.constrain((float)(140 - 2.2*Math.abs(p.z - model.zMax/2.)), 0, 100),
                max(0, zlv - zwv*abs(p.z - zv))
            ), LXColor.Blend.ADD);
            colors[p.index] = c;
        }
    }
}
