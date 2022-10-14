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
    public static final String SHOW_NAME = "summerbbq";

    private static final float METER = 39.37008f;
    private static final float FOOT = 12f;
    private static final float STRIP_LENGTH = 3 * METER;
    private static final int LED_PER_STRIP = 30;

    // map virtual output ("circle") to Pixlite index and output number (starting with 1)
    private static final int[][] pixliteOutputMapping = {
        {0, 1}, {0, 2}, {0, 3}, {0, 4},
        {1, 1}, {1, 2}, {1, 3}, {1, 4}, {1, 5}, {1, 6}, {1, 7}, {1, 8},
        {1, 9}, {1, 10}, {1, 11}, {1, 12}, {1, 13}, {1, 14}, {1, 15}, {1, 16},
        {2, 9}, {2, 10}, {2, 11}, {2, 12}, {2, 13}, {2, 14}, {2, 15}, {2, 16},
        {2, 1}, {2, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}, {2, 7}, {2, 8},
        {0, 5}, {0, 6}, {0, 7}, {0, 8},
    };

    String[] pixliteIpAddresses = {
        "10.200.1.2",
        "10.200.1.100",
        "10.200.1.200",

    };

    public StripsModel<DoubleStrip> buildModel() {

        DoubleStrip.Metrics stripMetrics = new DoubleStrip.Metrics(
                LED_PER_STRIP, STRIP_LENGTH / LED_PER_STRIP, 1, 0); // 1" front/back gap

        CirclesBuilder<DoubleStrip> builder = new CirclesBuilder<>(SHOW_NAME,
                (String id, LXTransform t) -> new DoubleStrip(id, stripMetrics, t));

        float scale = 1.0f;
        float circleRadius = 10;
        // angle between trusses
        float segmentAngle = 30;
        int arcSegments = 7;
        float arcSweepAngle = arcSegments * segmentAngle;
        float arcStartAngle = 180 - (arcSweepAngle - 180) / 2;

        int startOutputs = 4;
        int startOutputDir = -1;
        int startOutputStrandsPerOutput = 2;
        int endOutputs = 4;
        int endOutputDir = 1;
        int endOutputStrandsPerOutput = 2;
        float middleStartAngle = arcStartAngle + segmentAngle;
        float middleSweepAngle = (arcSegments - 2) * segmentAngle;
        int[] middleOutputCounts = {11, 5, 5, 11};
        int[] middleOutputDirs = {-1, 1, -1, 1};
        int[] middleOutputStrandsPerOutput = {2, 2, 2, 2};

        int middleOutputs = 0;
        for (int i = 0; i < middleOutputCounts.length; ++i) {
            middleOutputs += middleOutputCounts[i];
        }

        // 1 "strip" is 3 strings daisy chained, up and down, back to back
        // we're assuming 2 "strips" per output
        // start outputs, from truss 2 -> 1 clockwise
        float curAngle = middleStartAngle;
        float perOutputSweep = segmentAngle / startOutputs;
        for (int i = 0; i < startOutputs; ++i) {
            int dir = startOutputDir;
            builder.addCircle().withRadius(circleRadius * METER * scale)
                .addStrips(startOutputStrandsPerOutput).withDegreeOffset(curAngle).withDegreeSweep(dir * perOutputSweep / 2)
                .build();
            curAngle += dir * perOutputSweep;
        }

        // middle outputs
        curAngle = middleStartAngle;
        perOutputSweep = middleSweepAngle / middleOutputs;
        for (int i = 0; i < middleOutputCounts.length; ++i) {
            int dir = middleOutputDirs[i];
            int strandsPerOutput = middleOutputStrandsPerOutput[i];
            for (int j = 0; j < middleOutputCounts[i]; ++j) {
                float offset = curAngle + perOutputSweep * (dir > 0 ? j + 0.5f : middleOutputCounts[i] - j);
                builder.addCircle().withRadius(circleRadius * METER * scale)
                    .addStrips(strandsPerOutput).withDegreeOffset(offset).withDegreeSweep(dir * perOutputSweep / 2)
                    .build();
            }

            curAngle += middleOutputCounts[i] * perOutputSweep;
        }

        // end outputs, from truss 7 -> 8 counterclockwise
        perOutputSweep = segmentAngle / endOutputs;
        for (int i = 0; i < startOutputs; ++i) {
            int dir = endOutputDir;
            builder.addCircle().withRadius(circleRadius * METER * scale)
                .addStrips(endOutputStrandsPerOutput).withDegreeOffset(curAngle + perOutputSweep / 2).withDegreeSweep(dir * perOutputSweep / 2)
                .build();
            curAngle += dir * perOutputSweep;
        }

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
            System.out.println("Adding pixlite output: " + i + ", " + pixliteIndex + ", " + outputNumber + ", " + circle.getPoints().size());
            pixlites[pixliteIndex].addPixliteOutput(new PointsGrouping(outputNumber+"").addPoints(circle.getPoints()));
            ++i;
        }

        for (SimplePixlite pixlite : pixlites) {
            lx.addOutput(pixlite);
        }
    }
}
