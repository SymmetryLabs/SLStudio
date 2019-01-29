package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.color.Ops16;
import com.symmetrylabs.color.Spaces;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer.Space;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;

import java.util.ArrayList;

public class StripFilterAll extends SLEffect<StripsModel<? extends Strip>> {
    public final ArrayList<BooleanParameter> stripSelectas = new ArrayList<>();

    public StripFilterAll(LX lx) {
        super(lx);

        for (int i = 0; i < 12; i++){
            stripSelectas.add(new BooleanParameter(i+"", true));
            addParameter(stripSelectas.get(i));
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
//        if (p instanceof BooleanParameter) {
//            BooleanParameter param = (BooleanParameter) p;
//            Integer.parseInt(param.getLabel());
//            if (param.getValueb()) {
//                if (param == xParam) {
//                    yParam.setValue(false);
//                    zParam.setValue(false);
//                }
//                if (param == yParam) {
//                    xParam.setValue(false);
//                    zParam.setValue(false);
//                }
//                if (param == zParam) {
//                    xParam.setValue(false);
//                    yParam.setValue(false);
//                }
//            }
//        }
    }

    @Override
    public void run(double deltaMs, double enabledAmount, Space preferredSpace) {
        if (enabledAmount == 0) return;
        double alphaFactor = Spaces.cie_lightness_to_luminance(1 - enabledAmount);

        long[] colors = (long[]) getArray(Space.RGB16);
        CubesModel cubeModel = (CubesModel)lx.model;
        for (CubesModel.Cube cube : cubeModel.getCubes()){
            int i = 0;
            for(CubesModel.CubesStrip strip : cube.getStrips()){
                if (!filterAllows(strip, i)) {
                    for (LXPoint point : strip.points) {
                        long c = colors[point.index];
                        colors[point.index] = (alphaFactor == 0) ? 0 :
                            Ops16.rgba(
                                Ops16.red(c), Ops16.green(c), Ops16.blue(c),
                                ((int) (Ops16.alpha(c) * alphaFactor + 0.5))
                            );
                    }
                }
                i++;
            }
        }
        markModified(Space.RGB16);
    }

    public boolean filterAllows(Strip strip, int i) {
        return stripSelectas.get(i).getValueb();
    }
}
