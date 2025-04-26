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
            // the points for one pixlite output have to be spread across multiple universes
            int numPoints = pointsGrouping.size();
            int numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
            int counter = 0;

            for (int i = 0; i < numUniverses; i++) {
                int universe = firstUniverseOnOutput + i;
                int numIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints
                    ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE)
                    : MAX_NUM_POINTS_PER_UNIVERSE;
                int[] indices = new int[numIndices];
                for (int i1 = 0; i1 < numIndices; i1++) {
                    indices[i1] = pointsGrouping.getPoint(counter++).index;
                }
                ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, indices, universe - 1);
                addDatagram(dmxDatagram);
            }
        }
    }

}
