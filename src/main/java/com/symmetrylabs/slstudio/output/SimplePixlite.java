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
        private final int outputIndex;
        private final int firstUniverseOnOutput;

        public SimplePixliteOutput(PointsGrouping pointsGrouping) throws SocketException {
            super(lx);
            this.outputIndex = Integer.parseInt(pointsGrouping.id);
            this.firstUniverseOnOutput = outputIndex * 10;
            setupDatagrams(pointsGrouping);
        }

        private int maxPixelsPerUniverse(PointsGrouping pointsGrouping) {
            return pointsGrouping.rgbw ? 128 : 170;
        }

        private void setupDatagrams(PointsGrouping pointsGrouping) {
            int[] allIndices = pointsGrouping.getIndices();
            int firstUniverse = outputIndex;
            int maxPixels = maxPixelsPerUniverse(pointsGrouping);

            // Check if we have per-strip segments with individual GRB settings
            List<PointsGrouping.StripSegment> segments = pointsGrouping.getStripSegments();
            if (!segments.isEmpty()) {
                // Build a boolean array indicating GRB for each pixel
                boolean[] grbFlags = new boolean[allIndices.length];
                for (PointsGrouping.StripSegment seg : segments) {
                    for (int i = seg.startIndex; i < seg.endIndex && i < grbFlags.length; i++) {
                        grbFlags[i] = seg.grbSwap;
                    }
                }

                // Split into multiple universes if needed, preserving per-pixel GRB flags
                int numPoints = allIndices.length;
                int counter = 0;
                for (int u = 0; counter < numPoints; u++) {
                    int universe = firstUniverse + u;
                    int numIndices = Math.min(maxPixels, numPoints - counter);
                    int[] indices = new int[numIndices];
                    boolean[] chunkGrbFlags = new boolean[numIndices];
                    for (int i = 0; i < numIndices; i++) {
                        indices[i] = allIndices[counter];
                        chunkGrbFlags[i] = grbFlags[counter];
                        counter++;
                    }
                    int dataLength = pointsGrouping.rgbw ? 4 * numIndices : 3 * numIndices;
                    ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, indices, dataLength, universe);
                    dmxDatagram.setGrbFlags(chunkGrbFlags);
                    dmxDatagram.setRgbw(pointsGrouping.rgbw);
                    addDatagram(dmxDatagram);
                }

            } else {
                // Original behavior: one byte order for all points
                int numPoints = allIndices.length;
                int counter = 0;
                for (int u = 0; counter < numPoints; u++) {
                    int universe = firstUniverse + u;
                    int numIndices = Math.min(maxPixels, numPoints - counter);
                    int[] indices = new int[numIndices];
                    for (int i = 0; i < numIndices; i++) {
                        indices[i] = allIndices[counter++];
                    }
                    int dataLength = pointsGrouping.rgbw ? 4 * numIndices : 3 * numIndices;
                    ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, indices, dataLength, universe);
                    dmxDatagram.setRgbw(pointsGrouping.rgbw);
                    if (pointsGrouping.grbSwap) {
                        dmxDatagram.setByteOrder(heronarts.lx.output.LXDatagram.ByteOrder.GRB);
                    }
                    addDatagram(dmxDatagram);
                }
            }
        }
    }

    /**
     * Output class that can handle multiple universes (up to 54).
     * Each universe gets 170 RGB pixels or 128 RGBW pixels.
     */
    protected class SimplePixliteOutputMultiUniverse extends LXDatagramOutput {
        private final int firstUniverse;
        private final int maxUniverses;

        public SimplePixliteOutputMultiUniverse(PointsGrouping pointsGrouping, int firstUniverse, int maxUniverses) throws SocketException {
            super(lx);
            this.firstUniverse = firstUniverse;
            this.maxUniverses = maxUniverses;
            setupDatagrams(pointsGrouping);
        }

        private int maxPixelsPerUniverse(PointsGrouping pointsGrouping) {
            return pointsGrouping.rgbw ? 128 : 170;
        }

        private void setupDatagrams(PointsGrouping pointsGrouping) {
            int numPoints = pointsGrouping.size();
            int pixelsPerUniverse = maxPixelsPerUniverse(pointsGrouping);
            int counter = 0;

            for (int u = 0; u < maxUniverses; u++) {
                int universe = firstUniverse + u;
                int numIndices = Math.min(pixelsPerUniverse, numPoints - counter);
                if (numIndices <= 0) break;
                int[] indices = new int[numIndices];
                for (int i = 0; i < numIndices; i++) {
                    indices[i] = pointsGrouping.getPoint(counter++).index;
                }
                int dataLength = pointsGrouping.rgbw ? 4 * numIndices : 3 * numIndices;
                ArtNetDmxDatagram dmxDatagram = new ArtNetDmxDatagram(lx, ipAddress, indices, dataLength, universe);
                dmxDatagram.setRgbw(pointsGrouping.rgbw);
                // Apply GRB color swap if enabled for this strip
                if (pointsGrouping.grbSwap) {
                    dmxDatagram.setByteOrder(heronarts.lx.output.LXDatagram.ByteOrder.GRB);
                }
                addDatagram(dmxDatagram);
            }
        }
    }

}
