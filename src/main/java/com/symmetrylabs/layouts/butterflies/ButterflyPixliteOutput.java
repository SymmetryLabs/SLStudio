package com.symmetrylabs.layouts.butterflies;

import java.net.SocketException;

import heronarts.lx.LX;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutput;
import heronarts.lx.output.LXOutputGroup;

import com.symmetrylabs.slstudio.pixlites.ArtNetDatagram;

public class ButterflyPixliteOutput extends LXDatagramOutput {
    private final LX lx;
    private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
    private final int outputIndex;
    private final int firstUniverseOnOutput;

    public ButterflyPixliteOutput(LX lx, String ipAddress, ButterflyPointsGrouping pointsGrouping) throws SocketException {
        super(lx);
        this.lx = lx;
        this.outputIndex = Integer.parseInt(pointsGrouping.id);
        this.firstUniverseOnOutput = outputIndex * 10;
        setupDatagrams(ipAddress, pointsGrouping);
    }

    private void setupDatagrams(String ipAddress, ButterflyPointsGrouping pointsGrouping) {
        // the points for one pixlite output have to be spread across multiple universes
        int numPoints = pointsGrouping.size();
        int numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
        int counter = 0;

        for (int i = 0; i < numUniverses; i++) {
            int universe = firstUniverseOnOutput + i;
            int numIndices = ((i+1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE) : MAX_NUM_POINTS_PER_UNIVERSE;
            int[] indices = new int[numIndices];
            for (int i1 = 0; i1 < numIndices; i1++) {
                indices[i1] = pointsGrouping.getPoint(counter++).index;
            }
            addDatagram(new ArtNetDatagram(lx, ipAddress, indices, universe-1));
        }
    }
}