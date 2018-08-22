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
        double[] circleRadii = { 2, 2.2 };

        for (double radius : circleRadii) {
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
