package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer.Space;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;

public class StripFilter extends ModelSpecificEffect<StripsModel<? extends Strip>> {
    public final BooleanParameter xParam = new BooleanParameter("X", true);
    public final BooleanParameter yParam = new BooleanParameter("Y", true);
    public final BooleanParameter zParam = new BooleanParameter("Z", true);
    public final BooleanParameter soloParam = new BooleanParameter("Solo", false);

    @Override
    protected StripsModel createEmptyModel() {
        return new StripsModel();
    }

    public StripFilter(LX lx) {
        super(lx);

        addParameter(xParam);
        addParameter(yParam);
        addParameter(zParam);
        addParameter(soloParam);
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p instanceof BooleanParameter && soloParam.getValueb()) {
            BooleanParameter param = (BooleanParameter) p;
            if (param.getValueb()) {
                if (param == xParam) {
                    yParam.setValue(false);
                    zParam.setValue(false);
                }
                if (param == yParam) {
                    xParam.setValue(false);
                    zParam.setValue(false);
                }
                if (param == zParam) {
                    xParam.setValue(false);
                    yParam.setValue(false);
                }
            }
        }
    }

    @Override
    public void run(double deltaMs, double enabledAmount, Space preferredSpace) {
        if (enabledAmount == 0) return;
        double alphaFactor = Spaces.cie_lightness_to_luminance(1 - enabledAmount);

        long[] colors = (long[]) getArray(Space.RGB16);
        for (Strip strip : model.getStrips()) {
            if (!filterAllows(strip)) {
                for (LXPoint point : strip.points) {
                    long c = colors[point.index];
                    colors[point.index] = (alphaFactor == 0) ? 0 :
                            Ops16.rgba(
                                    Ops16.red(c), Ops16.green(c), Ops16.blue(c),
                                    ((int) (Ops16.alpha(c) * alphaFactor + 0.5))
                            );
                }
            }
        }
        markModified(Space.RGB16);
    }

    public boolean filterAllows(Strip strip) {
        if (strip.xRange > strip.yRange && strip.xRange > strip.zRange) {
            return xParam.getValueb();
        }
        if (strip.yRange > strip.zRange) {
            return yParam.getValueb();
        }
        return zParam.getValueb();
    }
}
