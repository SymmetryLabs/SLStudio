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
    private static final float STRIP_LENGTH = 3 * METER;
    private static final int LED_PER_STRIP = 30;

    // map virtual output ("circle") to Pixlite index and output number (starting with 1)
    private static final int[][] pixliteOutputMapping = {
        {0, 1}, {0, 2}, {0, 3}, {0, 4},
        {1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {1, 7}, {1, 8},
        {1, 9}, {1, 10}, {1, 11}, {1, 12}, {1, 13}, {1, 14}, {1, 15}, {1, 16},
        {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}, {2, 7}, {2, 8},
        {2, 9}, {2, 10}, {2, 11}, {2, 12}, {2, 13}, {2, 14}, {2, 15}, {2, 16},
        {3, 1}, {3, 2}, {3, 3}, {3, 4},
    };

    String[] pixliteIpAddresses = {
        "10.200.1.3",
        "10.200.1.4",
        "10.200.1.5",
        "10.200.1.6",
    };

    public StripsModel<DoubleStrip> buildModel() {

        DoubleStrip.Metrics stripMetrics = new DoubleStrip.Metrics(
                LED_PER_STRIP, STRIP_LENGTH / LED_PER_STRIP, 1, 0); // 1" front/back gap

        CirclesBuilder<DoubleStrip> builder = new CirclesBuilder<>(
                (String id, LXTransform t) -> new DoubleStrip(id, stripMetrics, t));

        float scale = 1.0f;
        float circleRadius = 10;
        // angle between trusses
        float segmentAngle = 30;
        int arcSegments = 7;
        float arcSweepAngle = arcSegments * segmentAngle;
        float arcStartAngle = 180 - (arcSweepAngle - 180) / 2;

        int startOutputs = 3;
        int endOutputs = 3;
        float middleStartAngle = arcStartAngle + segmentAngle;
        float middleSweepAngle = (arcSegments - 2) * segmentAngle;
        int[] middleOutputCounts = {1, 11, 5, 5, 11, 1};
        int[] middleOutputDirs = {1, -1, 1, -1, 1, -1};

        int middleOutputs = 0;
        for (int i = 0; i < middleOutputCounts.length; ++i) {
            middleOutputs += middleOutputCounts[i];
        }

        // 1 "strip" is 3 strings daisy chained, up and down, back to back
        // we're assuming 2 "strips" per output
        // start outputs, from truss 2 -> 1 clockwise
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(2).withDegreeOffset(middleStartAngle).withDegreeSweep(-segmentAngle)
            .build();

        // middle outputs
        float curAngle = middleStartAngle;
        float perOutputSweep = middleSweepAngle / middleOutputs;
        for (int i = 0; i < middleOutputCounts.length; ++i) {
            int dir = middleOutputDirs[i];
            float sweep = middleOutputCounts[i] * perOutputSweep;
            builder.addCircle().withRadius(circleRadius * METER * scale)
                .addStrips(2).withDegreeOffset(dir > 0 ? curAngle : curAngle + sweep).withDegreeSweep(dir * sweep)
                .build();
            curAngle += sweep;
        }

        // end outputs, from truss 7 -> 8 counterclockwise
        builder.addCircle().withRadius(circleRadius * METER * scale)
            .addStrips(2).withDegreeOffset(middleStartAngle + 180).withDegreeSweep(segmentAngle)
            .build();

        return builder.build();
    }

    public void setupLx(SLStudioLX lx) {
        SimplePixlite[] pixlites = new SimplePixlite[pixliteIpAddresses.length];
        for (int i = 0; i < pixliteIpAddresses.length; ++i) {
            pixlites[i] = new SimplePixlite(lx, pixliteIpAddresses[i]);
        }

        int i = 0;
        for (CirclesModel.Circle<DoubleStrip> circle : ((CirclesModel<DoubleStrip>)lx.model).getCircles()) {
            int pixliteIndex = pixliteOutputMapping[i][0];
            int outputNumber = pixliteOutputMapping[i][1];
            pixlites[pixliteIndex].addPixliteOutput(new PointsGrouping(outputNumber+"").addPoints(circle.getPoints()));
            ++i;
        }

        for (SimplePixlite pixlite : pixlites) {
            lx.addOutput(pixlite);
        }
    }
}
