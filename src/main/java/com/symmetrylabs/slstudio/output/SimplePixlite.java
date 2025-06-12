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
            // Calculate universe number based on output index (1-8)
            // Each output gets 2 universes in H340 mode
            this.firstUniverseOnOutput = (outputIndex - 1) * 2;
            setupDatagrams(pointsGrouping);
        }

        private void setupDatagrams(PointsGrouping pointsGrouping) {
    // K8 ARTNET+DMX controller in H340 mode: 340 pixels per port, 2 universes per port
    int numPoints = pointsGrouping.size();
    int universesPerOutput = 2; // H340 mode = 2 universes per output
    int pixelsPerUniverse = MAX_NUM_POINTS_PER_UNIVERSE;
    int counter = 0;
    
    // For K8 controller, universes are sequentially assigned to each port
    // Each port gets 2 universes in H340 mode
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
