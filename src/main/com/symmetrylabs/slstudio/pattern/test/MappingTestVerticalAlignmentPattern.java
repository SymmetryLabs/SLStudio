package com.symmetrylabs.slstudio.pattern.test;

import com.symmetrylabs.slstudio.model.Sun;
import com.symmetrylabs.slstudio.model.SunsModel;
import com.symmetrylabs.slstudio.pattern.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.FunctionalParameter;

import static com.symmetrylabs.slstudio.model.Slice.PIXEL_PITCH;
import static com.symmetrylabs.slstudio.util.MathUtils.abs;

public class MappingTestVerticalAlignmentPattern extends SLPattern implements SLTestPattern {

    private static final float DEFAULT_LINE_SIZE = 0.5f * PIXEL_PITCH;

    private final SunsModel model;

    private final BoundedParameter thickness = new BoundedParameter("thickness", DEFAULT_LINE_SIZE, 4 * PIXEL_PITCH);
    private final BoundedParameter distance = new BoundedParameter("distance", 10 * PIXEL_PITCH, 40 * PIXEL_PITCH);
    private final FunctionalParameter lowerBound = new FunctionalParameter() {
        @Override
        public double getValue() {
            return -distance.getValue();
        }
    };
    private final BoundedParameter periodMs = new BoundedParameter("period", 2000, 1000, 30000);

    private final TriangleLFO yLFO = new TriangleLFO(lowerBound, distance, periodMs);

    public MappingTestVerticalAlignmentPattern(LX lx) {
        super(lx);
        model = (SunsModel) lx.model;

        addParameter(thickness);
        addParameter(distance);
        addParameter(periodMs);

        addModulator(yLFO).start();
    }

    @Override
    protected void run(double deltaMs) {
        float brightnessModifier = 255f / thickness.getValuef();
        for (Sun sun : model.getSuns()) {
            float centerY = sun.center.y;
            float lineY = centerY + yLFO.getValuef();

            for (LXPoint point : sun.points) {
                float brightness = getPointDist(point.y, lineY);
                int gray = (int) (brightness * brightnessModifier);
                colors[point.index] = LXColor.rgb(gray, gray, gray);
            }
        }
    }

    private float getPointDist(float y, float center) {
        float distance = abs(y - center);
        return distance < thickness.getValuef() ? thickness.getValuef() - distance : 0;
    }
}
