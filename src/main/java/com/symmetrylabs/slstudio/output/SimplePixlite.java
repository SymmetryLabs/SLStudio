package com.symmetrylabs.slstudio.output;

import heronarts.lx.LX;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.output.LXDatagramOutput;

import java.net.SocketException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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
            int[] allIndices = pointsGrouping.getIndices();
            int firstUniverse = outputIndex;

            // Check if we have per-strip segments with individual GRB settings
            List<PointsGrouping.StripSegment> segments = pointsGrouping.getStripSegments();
            if (!segments.isEmpty()) {
                // New approach: create ONE datagram per universe with per-pixel GRB info
                // Build a boolean array indicating GRB for each pixel
                boolean[] grbFlags = new boolean[allIndices.length];
                for (PointsGrouping.StripSegment seg : segments) {
                    for (int i = seg.startIndex; i < seg.endIndex && i < grbFlags.length; i++) {
                        grbFlags[i] = seg.grbSwap;
                    }
                }

                // Debug for universe 37
                boolean debugU37 = (firstUniverse == 37);
                if (debugU37) {
                    System.out.println("SimplePixlite U37: Creating single datagram with " + allIndices.length + " pixels");
                    System.out.println("  GRB flags: first 30 = " + java.util.Arrays.toString(java.util.Arrays.copyOfRange(grbFlags, 0, Math.min(30, grbFlags.length))));
                }

                // Create a single datagram with per-pixel GRB support
                ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, allIndices, firstUniverse);
                // Store GRB flags for use during copyPoints
                dmxDatagram.setGrbFlags(grbFlags);
                addDatagram(dmxDatagram);

            } else {
                // Original behavior: one byte order for all points
                int numPoints = allIndices.length;
                int counter = 0;
                for (int u = 0; counter < numPoints; u++) {
                    int universe = firstUniverse + u;
                    int numIndices = Math.min(MAX_NUM_POINTS_PER_UNIVERSE, numPoints - counter);
                    int[] indices = new int[numIndices];
                    for (int i = 0; i < numIndices; i++) {
                        indices[i] = allIndices[counter++];
                    }
                    ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, indices, universe);
                    if (pointsGrouping.grbSwap) {
                        dmxDatagram.setByteOrder(heronarts.lx.output.LXDatagram.ByteOrder.GRB);
                    }
                    addDatagram(dmxDatagram);
                }
            }
        }

        private class DatagramSpec {
            final int universe;
            final int startIdx;
            final int endIdx;
            final boolean grbSwap;
            DatagramSpec(int universe, int startIdx, int endIdx, boolean grbSwap) {
                this.universe = universe;
                this.startIdx = startIdx;
                this.endIdx = endIdx;
                this.grbSwap = grbSwap;
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
                // Apply GRB color swap if enabled for this strip
                if (pointsGrouping.grbSwap) {
                    dmxDatagram.setByteOrder(heronarts.lx.output.LXDatagram.ByteOrder.GRB);
                }
                addDatagram(dmxDatagram);
            }
        }
    }

}
