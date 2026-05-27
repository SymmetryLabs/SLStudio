package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.output.LXDatagramOutput;

import java.net.SocketException;

public class SimplePixlite extends ArtNetOutput {
    private LX lx;

    public SimplePixlite(LX lx, String ipAddress) {
        super(lx, ipAddress);
        this.lx = lx;
    }

    public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
        try {
            addChild(new SimplePixliteOutput(pointsGrouping));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SimplePixlite addPixliteOutput(String id, PointsGrouping pointsGrouping) {
        pointsGrouping.id = id;
        addPixliteOutput(pointsGrouping);
        return this;
    }

    /**
     * Add an output that spans multiple universes (up to 54).
     * Each universe gets 170 pixels. Universe numbering starts at 0.
     */
    public SimplePixlite addPixliteOutputMultiUniverse(PointsGrouping pointsGrouping, int firstUniverse, int maxUniverses) {
        try {
            addChild(new SimplePixliteOutputMultiUniverse(pointsGrouping, firstUniverse, maxUniverses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    protected class SimplePixliteOutput extends LXDatagramOutput {
        private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
        private final int outputIndex;
        private final int firstUniverseOnOutput;

        public SimplePixliteOutput(PointsGrouping pointsGrouping) throws SocketException {
            super(lx);
            this.outputIndex = Integer.parseInt(pointsGrouping.id);
            this.firstUniverseOnOutput = outputIndex * 10;
            setupDatagrams(pointsGrouping);
        }

        private void setupDatagrams(PointsGrouping pointsGrouping) {
    // Each output gets 1 universes, each universe 170 pixels
    int numPoints = pointsGrouping.size();
    int universesPerOutput = 1;
    int pixelsPerUniverse = MAX_NUM_POINTS_PER_UNIVERSE;
    int counter = 0;
    // outputIndex is 0-based, universes start at 1
    int firstUniverse = outputIndex - 1;
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

    /**
     * Output class that can handle multiple universes (up to 54).
     * Each universe gets 170 pixels.
     */
    protected class SimplePixliteOutputMultiUniverse extends LXDatagramOutput {
        private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
        private final int firstUniverse;
        private final int maxUniverses;

        public SimplePixliteOutputMultiUniverse(PointsGrouping pointsGrouping, int firstUniverse, int maxUniverses) throws SocketException {
            super(lx);
            this.firstUniverse = firstUniverse;
            this.maxUniverses = maxUniverses;
            setupDatagrams(pointsGrouping);
        }

        private void setupDatagrams(PointsGrouping pointsGrouping) {
            int numPoints = pointsGrouping.size();
            int pixelsPerUniverse = MAX_NUM_POINTS_PER_UNIVERSE;
            int counter = 0;

            for (int u = 0; u < maxUniverses; u++) {
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
