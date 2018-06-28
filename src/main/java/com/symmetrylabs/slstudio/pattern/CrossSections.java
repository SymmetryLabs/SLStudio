package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.color.Ops8;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import static com.symmetrylabs.util.MathUtils.*;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

public class CrossSections extends LXPattern {

    final SinLFO x = new SinLFO(model.xMin, model.xMax, 5000);
    final SinLFO y = new SinLFO(model.yMin, model.yMax, 6000);
    final SinLFO z = new SinLFO(model.zMin, model.zMax, 7000);

    final CompoundParameter xl = new CompoundParameter("xLvl", 1);
    final CompoundParameter yl = new CompoundParameter("yLvl", 1);
    final CompoundParameter zl = new CompoundParameter("zLvl", 0.5);

    final CompoundParameter xr = new CompoundParameter("xSpd", 0.7);
    final CompoundParameter yr = new CompoundParameter("ySpd", 0.6);
    final CompoundParameter zr = new CompoundParameter("zSpd", 0.5);

    final CompoundParameter xw = new CompoundParameter("xSize", 0.3);
    final CompoundParameter yw = new CompoundParameter("ySize", 0.3);
    final CompoundParameter zw = new CompoundParameter("zSize", 0.3);


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

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);

        updateXYZVals();

        float xlv = 100 * xl.getValuef();
        float ylv = 100 * yl.getValuef();
        float zlv = 100 * zl.getValuef();

        float xwv = 100f / (10 + 40 * xw.getValuef());
        float ywv = 100f / (10 + 40 * yw.getValuef());
        float zwv = 100f / (10 + 40 * zw.getValuef());

        for (LXVector p : getVectorList()) {
                int c = 0;
                c = Ops8.add(c, LXColor.hsb(
                        palette.getHuef() + p.x / 10 + p.y / 3,
                        constrain(140 - 1.1f * abs(p.x - model.xMax / 2f), 0, 100),
                        max(0, xlv - xwv * abs(p.x - xv))
                ));
                c = Ops8.add(c, LXColor.hsb(
                        palette.getHuef() + 80 + p.y / 10,
                        constrain(140 - 2.2f * abs(p.y - model.yMax / 2f), 0, 100),
                        max(0, ylv - ywv * abs(p.y - yv))
                ));
                c = Ops8.add(c, LXColor.hsb(
                        palette.getHuef() + 160 + p.z / 10 + p.y / 2,
                        constrain(140 - 2.2f * abs(p.z - model.zMax / 2f), 0, 100),
                        max(0, zlv - zwv * abs(p.z - zv))
                ));
                colors[p.index] = c;
        }
        markModified(SRGB8);
    }
}
