package com.symmetrylabs.shows.summerbbq;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import com.symmetrylabs.slstudio.model.CirclesModel;
import com.symmetrylabs.slstudio.model.CirclesBuilder;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;

public class SummerBBQShow implements Show {
    private static final float METER = 39.37008f;
    private static final float STRIP_LENGTH = 2 * METER;
    private static final int LED_PER_STRIP = 20;

    public StripsModel<DoubleStrip> buildModel() {

        DoubleStrip.Metrics stripMetrics = new DoubleStrip.Metrics(
                LED_PER_STRIP, STRIP_LENGTH / LED_PER_STRIP, 1, -STRIP_LENGTH / LED_PER_STRIP / 2); // 1" front/back gap

        CirclesBuilder<DoubleStrip> builder = new CirclesBuilder<>(
                (String id, LXTransform t) -> new DoubleStrip(id, stripMetrics, t));

        float scale = 1.0f;

        // circle radii in meters
        float[] circleRadii = { 2f, 2.2f };
        float[] bladeRadii = { 2f, 2.2f };
        float bladeStartAngle = -45f;
        int bladeCount = 5;
        float bladeOffsetRadius = 1.4f;

        for (float radius : circleRadii) {
            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(15).withDegreeOffset(90).withDegreeSweep(-360 / 5)
                .build();

            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(15).withDegreeOffset(90 - 360 / 5).withDegreeSweep(-360 / 5)
                .build();

            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(15).withDegreeOffset(90).withDegreeSweep(360 / 5)
                .build();

            builder.addCircle().withRadius(radius * METER * scale)
                .addStrips(15).withDegreeOffset(90 + 360 / 5).withDegreeSweep(360 / 5)
                .build();
        }

        for (int i = 0; i < bladeCount; ++i) {
            for (float radius : bladeRadii) {
                float angle = bladeStartAngle + i * 360f / bladeCount;
                builder.addCircle().withRadius(radius * METER * scale)
                    .withCenter((float)(Math.cos(Math.toRadians(angle)) * bladeOffsetRadius * METER * scale),
                            0, (float)(Math.sin(Math.toRadians(angle)) * bladeOffsetRadius * METER * scale))
                    .addStrips(15).withDegreeOffset(bladeStartAngle + 90 + i * 360f / bladeCount).withDegreeSweep(-360 / 5)
                    .build();
            }
        }

        return builder.build();
    }

    public void setupLx(SLStudioLX lx) {
        SimplePixlite pixlite = new SimplePixlite(lx, "10.200.1.128");

        int i = 1;
        for (CirclesModel.Circle<DoubleStrip> circle : ((CirclesModel<DoubleStrip>)lx.model).getCircles()) {
            pixlite.addPixliteOutput(new PointsGrouping(i+"").addPoints(circle.getPoints()));
            ++i;
        }

        lx.addOutput(pixlite);
    }
}
