package com.symmetrylabs.slstudio.pixlites;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;

import java.net.SocketException;


public class PixliteOutput extends LXDatagramOutput {
    private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
    private final int outputIndex;
    private final int firstUniverseOnOutput;

    public PixliteOutput(LX lx, String ipAddress, PointsGrouping pointsGrouping) throws SocketException {
        super(lx);
        this.outputIndex = Integer.parseInt(pointsGrouping.id);
        this.firstUniverseOnOutput = outputIndex * 10;
        setupDatagrams(ipAddress, pointsGrouping);
    }

    private void setupDatagrams(String ipAddress, PointsGrouping pointsGrouping) {
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
            addDatagram(new ArtNetDatagram(ipAddress, indices, universe - 1));
        }
    }
}
