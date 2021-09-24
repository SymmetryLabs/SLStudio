package com.symmetrylabs.slstudio.effect;

import java.util.List;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import com.symmetrylabs.shows.cubes.CubesModel;


public class CubeThreshold extends SLEffect<CubesModel> {

    final CompoundParameter thresholdParam;

    public CubeThreshold(LX lx) {
        super(lx);

        addParameter(thresholdParam = new CompoundParameter("threshold", 0, 1));
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (CubesModel.Cube cube : model.getCubes()) {
            float totalBrightness = 0;
            for (LXPoint p : cube.getPoints()) {
                totalBrightness += LXColor.b(colors[p.index]);
            }
            int numPoints = cube.getPoints().size();
            float avgBrightness = totalBrightness / numPoints;
            int c = avgBrightness > numPoints * thresholdParam.getValuef() ? LXColor.WHITE : LXColor.BLACK;
            setColor(cube, c);
        }
    }
}
