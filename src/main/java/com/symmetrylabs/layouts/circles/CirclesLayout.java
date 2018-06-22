package com.symmetrylabs.layouts.circles;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.SLStudioLX;

public class CirclesLayout implements Layout {
    private static final float METER = 39.37008f;
    private static final float STRIP_LENGTH = 2 * METER;
    private static final int LED_PER_STRIP = 20;

    public StripsModel<DoubleStrip> buildModel() {

        DoubleStrip.Metrics stripMetrics = new DoubleStrip.Metrics(
                LED_PER_STRIP, STRIP_LENGTH / LED_PER_STRIP, 1); // 1" front/back gap

        CirclesBuilder<DoubleStrip> builder = new CirclesBuilder<>(
                (String id, LXTransform t) -> new DoubleStrip(id, stripMetrics, t));

        float scale = 1.0f;

        // circle radii in meters
        double[] innerCircleRadii = { 2, 2.2, 2.4 };
        double[] outerCircleRadii = { 3.9, 4.1, 4.3 };
        double[] wallRadii = { 2.775, 3.15, 3.525 };

        for (double radius : innerCircleRadii) {
            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(8).withDegreeOffset(55).withDegreeSweep(70)
                .addStrips(24).withDegreeOffset(155).withDegreeSweep(230)
                .build();
        }

        for (double radius : outerCircleRadii) {
            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(48).withDegreeOffset(-75).withDegreeSweep(330)
                .build();
        }

        for (double radius : wallRadii) {
            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(2).withDegreeOffset(55).withDegreeSpacing(5)
                .addStrips(2).withDegreeOffset(125).withDegreeSpacing(-5)
                .build();
        }

        return builder.build();
    }

    public void setupLx(SLStudioLX lx) {
    }
}
