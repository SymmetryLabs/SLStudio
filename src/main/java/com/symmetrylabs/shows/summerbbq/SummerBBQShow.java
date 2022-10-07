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
    private static final float FOOT = 12f;
    private static final float STRIP_LENGTH = 2 * METER;
    private static final int LED_PER_STRIP = 20;

    public StripsModel<DoubleStrip> buildModel() {

        DoubleStrip.Metrics stripMetrics = new DoubleStrip.Metrics(
                LED_PER_STRIP, STRIP_LENGTH / LED_PER_STRIP, 1, 0); // 1" front/back gap

        CirclesBuilder<DoubleStrip> builder = new CirclesBuilder<>(
                (String id, LXTransform t) -> new DoubleStrip(id, stripMetrics, t));

        float scale = 1.0f;
        float circleRadius = 10;

        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(30).withDegreeOffset(180 + 30).withDegreeSweep(-30)
            .build();
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(30).withDegreeOffset(180 + 30).withDegreeSweep(30)
            .build();
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(30).withDegreeOffset(-90).withDegreeSweep(-30)
            .build();
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(30).withDegreeOffset(-90).withDegreeSweep(30)
            .build();
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(30).withDegreeOffset(-30).withDegreeSweep(-30)
            .build();
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(30).withDegreeOffset(-30).withDegreeSweep(30)
            .build();

        builder.addCircle().withRadius(circleRadius * METER * scale).withCenter(0, 0, 5 * FOOT)
            .addStrips(30).withDegreeOffset(45).withDegreeSweep(90)
            .build();

        return builder.build();
    }

    public void setupLx(SLStudioLX lx) {
        SimplePixlite pixlite = new SimplePixlite(lx, "10.200.1.2");

        int i = 1;
        for (CirclesModel.Circle<DoubleStrip> circle : ((CirclesModel<DoubleStrip>)lx.model).getCircles()) {
            pixlite.addPixliteOutput(new PointsGrouping(i+"").addPoints(circle.getPoints()));
            ++i;
        }

        lx.addOutput(pixlite);
    }
}
