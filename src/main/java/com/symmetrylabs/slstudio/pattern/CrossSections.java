package com.symmetrylabs.slstudio.pattern;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.blend.Ops16;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;
import heronarts.lx.PolyBuffer;

import static com.symmetrylabs.util.MathUtils.*;


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
        Object array = getArray(space);
        final int[] intColors = (space == PolyBuffer.Space.RGB8) ? (int[]) array : null;
        final long[] longColors = (space == PolyBuffer.Space.RGB16) ? (long[]) array : null;

        updateXYZVals();

        float xlv = 100 * xl.getValuef();
        float ylv = 100 * yl.getValuef();
        float zlv = 100 * zl.getValuef();

        float xwv = 100f / (10 + 40 * xw.getValuef());
        float ywv = 100f / (10 + 40 * yw.getValuef());
        float zwv = 100f / (10 + 40 * zw.getValuef());

        for (LXPoint p : model.points) {
            if (space == PolyBuffer.Space.RGB8) {
                int col8 = 0;
                col8 = LXColor.blend(col8, LXColor.hsb(
                    palette.getHuef() + p.x / 10 + p.y / 3,
                    constrain(140 - 1.1f * abs(p.x - model.xMax / 2f), 0, 100),
                    max(0, xlv - xwv * abs(p.x - xv))
                ), LXColor.Blend.ADD);
                col8 = LXColor.blend(col8, LXColor.hsb(
                    palette.getHuef() + 80 + p.y / 10,
                    constrain(140 - 2.2f * abs(p.y - model.yMax / 2f), 0, 100),
                    max(0, ylv - ywv * abs(p.y - yv))
                ), LXColor.Blend.ADD);
                col8 = LXColor.blend(col8, LXColor.hsb(
                    palette.getHuef() + 160 + p.z / 10 + p.y / 2,
                    constrain(140 - 2.2f * abs(p.z - model.zMax / 2f), 0, 100),
                    max(0, zlv - zwv * abs(p.z - zv))
                ), LXColor.Blend.ADD);
                intColors[p.index] = col8;
            }
            else if (space == PolyBuffer.Space.RGB16) {
                long col16 = 0;
                col16 = Ops16.add(col16, LXColor16.hsb(
                    palette.getHuef() + p.x / 10 + p.y / 3,
                    constrain(140 - 1.1f * abs(p.x - model.xMax / 2f), 0, 100),
                    max(0, xlv - xwv * abs(p.x - xv))
                ));
                col16 = Ops16.add(col16, LXColor16.hsb(
                    palette.getHuef() + 80 + p.y / 10,
                    constrain(140 - 2.2f * abs(p.y - model.yMax / 2f), 0, 100),
                    max(0, ylv - ywv * abs(p.y - yv))
                ));
                col16 = Ops16.add(col16, LXColor16.hsb(
                    palette.getHuef() + 160 + p.z / 10 + p.y / 2,
                    constrain(140 - 2.2f * abs(p.z - model.zMax / 2f), 0, 100),
                    max(0, zlv - zwv * abs(p.z - zv))
                ));
                longColors[p.index] = col16;
            }
        }
        markModified(space);
    }
}
