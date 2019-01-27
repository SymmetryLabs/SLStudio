package com.symmetrylabs.slstudio.output;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.output.ArtNetDmxDatagram;
import com.symmetrylabs.slstudio.output.PointsGrouping;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutputGroup;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssignablePixlite extends ArtNetOutput {
    private static final int DEFAULT_NUM_DATALINES = 16;

    private final List<Dataline> datalines = new ArrayList<>();

    public AssignablePixlite(LX lx, String ipAddress) {
        this(lx, ipAddress, DEFAULT_NUM_DATALINES);
    }

    public AssignablePixlite(LX lx, String ipAddress, int numDatalines) {
        this(lx, ipAddress, numDatalines, Dataline.DEFAULT_DATALINE_NUM_POINTS);
    }

    public AssignablePixlite(LX lx, String ipAddress, int numDatalines, int datalineNumPoints) {
        super(lx, ipAddress);

        try {
            int portIndex = 1;
            for (int i = 1; i < numDatalines + 1; i++) {
                Dataline dataline = new Dataline(lx, ipAddress, i, datalineNumPoints);
                datalines.add(dataline);
                addChild(dataline);
            }
        } catch (SocketException e) {
            e.printStackTrace();
            enabled.setValue(false);
        }
    }

    public Dataline get(int id) {
        return datalines.get(id - 1);
    }

    public class Dataline extends LXDatagramOutput {
        private static final int DEFAULT_DATALINE_NUM_POINTS = 300; // arbitrary
        private static final int MAX_NUM_POINTS_PER_UNIVERSE = 170;

        private final List<ArtNetDmxDatagram> artNetDatagrams = new ArrayList<>();
        public final String ipAddress;
        public final int index;
        private final int numUniverses;
        private final int datalineNumPoints;

        public Dataline(LX lx, String ipAddress, int index) throws SocketException {
            this(lx, ipAddress, index, DEFAULT_DATALINE_NUM_POINTS);
        }

        public Dataline(LX lx, String ipAddress, int index, int datalineNumPoints) throws SocketException {
            super(lx);
            this.ipAddress = ipAddress;
            this.index = index;
            this.datalineNumPoints = datalineNumPoints;
            this.numUniverses = (datalineNumPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
            createDatagrams(lx, index*10);
        }

        public List<ArtNetDmxDatagram> getArtNetDmxDatagrams() {
            return Collections.unmodifiableList(artNetDatagrams);
        }

        public void setPoints(List<LXPoint> points) {
            int[] indices = new int[points.size()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = points.get(i).index;
            }
            setIndices(indices);
        }

        public void setIndices(int[] indices) {
            int counter = 0;
            for (int i = 0; i < numUniverses; i++) {
                int numDatagramIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > indices.length
                    ? (indices.length % MAX_NUM_POINTS_PER_UNIVERSE)
                    : MAX_NUM_POINTS_PER_UNIVERSE;

                int[] datagramIndices = new int[numDatagramIndices];
                for (int i1 = 0; i1 < numDatagramIndices; i1++) {
                    datagramIndices[i1] = indices[counter++];
                }
                artNetDatagrams.get(i).setIndices(datagramIndices);
            }
        }

        private void createDatagrams(LX lx, int startUniverse) throws SocketException {
            for (int i = 0; i < numUniverses; i++) {
                int[] indices = new int[MAX_NUM_POINTS_PER_UNIVERSE];
                for (int j = 0; j < indices.length; j++) {
                    indices[j] = -1;
                }
                ArtNetDmxDatagram datagram =
                    new ArtNetDmxDatagram(
                        lx, ipAddress, indices, indices.length*3, startUniverse++ - 1);
                artNetDatagrams.add(datagram);
                addDatagram(datagram);
            }
        }

    }
}
