package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.output.LXDatagramOutput;

import java.net.SocketException;

public class SimpleArtnetController extends ArtNetOutput {
    private LX lx;

    public SimpleArtnetController(LX lx, String ipAddress) {
        super(lx, ipAddress);
        this.lx = lx;
    }

    public SimpleArtnetController addPixliteOutput(PointsGrouping pointsGrouping) {
        try {
            addChild(new SimpleArtnetControllerOutput(pointsGrouping));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SimpleArtnetController addPixliteOutput(String id, PointsGrouping pointsGrouping) {
        pointsGrouping.id = id;
        addPixliteOutput(pointsGrouping);
        return this;
    }

    protected class SimpleArtnetControllerOutput extends LXDatagramOutput {
        private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
        private final int outputIndex;
        private final int firstUniverseOnOutput;

        public SimpleArtnetControllerOutput(PointsGrouping pointsGrouping) throws SocketException {
            super(lx);
            this.outputIndex = Integer.parseInt(pointsGrouping.id);
            this.firstUniverseOnOutput = outputIndex * 10;
            setupDatagrams(pointsGrouping);
        }

        private void setupDatagrams(PointsGrouping pointsGrouping) {
    // Each output gets 4 universes, each universe 170 pixels
    int numPoints = pointsGrouping.size();
    int universesPerOutput = 4;
    int pixelsPerUniverse = MAX_NUM_POINTS_PER_UNIVERSE;
    int counter = 0;
    // outputIndex is 0-based, universes start at 1
    int firstUniverse = (outputIndex - 1) * universesPerOutput;
    for (int u = 0; u < universesPerOutput; u++) {
        int universe = firstUniverse + u;
        int numIndices = Math.min(pixelsPerUniverse, numPoints - counter);
        if (numIndices <= 0) break;
        int[] indices = new int[numIndices];
        for (int i = 0; i < numIndices; i++) {
            indices[i] = pointsGrouping.getPoint(counter++).index;
        }
        ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, indices, universe);
        addDatagram(dmxDatagram);
    }
}
    }

}
