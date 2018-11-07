package com.symmetrylabs.shows.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;

import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.StringParameter;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.output.LXDatagramOutput;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.output.ArtNetDatagram;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.slstudio.output.PointsGrouping;


public class AssignablePixlite extends LXOutputGroup {

    private final int NUM_DATALINES = 16;

    public final String ipAddress;

    public final List<Port> ports = new ArrayList<>();
    public final HashMap<Integer, Dataline> datalines = new HashMap<>();

    public AssignablePixlite(LX lx, String ipAddress) {
        super(lx);
        this.ipAddress = ipAddress;

        try {
            int portIndex = 1;
            for (int i = 1; i < NUM_DATALINES+1; i = i+2) {
                Dataline datalineA = new Dataline(lx, ipAddress, i);
                datalines.put(i, datalineA);

                Dataline datalineB = new Dataline(lx, ipAddress, i+1);
                datalines.put(i+1, datalineB);

                Port port = new Port(lx, datalineA, datalineB, portIndex++);
                ports.add(port);
                addChild(port);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        //System.out.println("/*- Pixlite (" + ipAddress + ") ---------------------------------------------------*/");
//        System.out.println("NUM PORTS: " + ports.size());
//        System.out.println("NUM DATALINES: " + datalines.size());

//        for (int i = 0; i < ports.size(); i++) {
//            System.out.println("----------------------------");
//            System.out.println("Pixlite Port (physical): " + ports.get(i).index);
//            System.out.println("-Dataline A (virtual port): " + ports.get(i).datalineA.index);
//            for (AssignableArtNetDatagram datagram : ports.get(0).datalineA.artNetDatagrams) {
//                System.out.println("--Artnet Datagram: ip: " + datagram.ipAddress + ", universe: " + datagram.universe);
//            }
//
//            System.out.println("-dataline B (virtual port): " + ports.get(i).datalineB.index);
//            for (AssignableArtNetDatagram datagram : ports.get(0).datalineB.artNetDatagrams) {
//                System.out.println("--Artnet Datagram: ip: " + datagram.ipAddress + ", universe: " + datagram.universe);
//            }
//            System.out.println("");
//        }


        final TreeModelingTool.BranchManipulator manipulator = TreeModelingTool.getInstance(lx).branchManipulator;

        manipulator.ipAddress.addListener(parameter -> {
            String ip = ((StringParameter)parameter).getString();
            if (this.ipAddress.equals(ip)) {
                int portIndex = manipulator.channel.getValuei()-1;
                ports.get(portIndex).setBranch(TreeModelingTool.getInstance(lx).getSelectedBranch());
            }
        });

        manipulator.channel.addListener(parameter -> {
            String ip = manipulator.ipAddress.getString();
            if (this.ipAddress.equals(ip)) {
                int portIndex = ((DiscreteParameter)parameter).getValuei()-1;
                ports.get(portIndex).setBranch(TreeModelingTool.getInstance(lx).getSelectedBranch());
            }
        });
    }

    public class Port extends LXOutputGroup {
        public final AssignablePixlite.Dataline datalineA;
        public final AssignablePixlite.Dataline datalineB;
        public final String ipAddress;
        public final int index;

        public Port(LX lx, AssignablePixlite.Dataline datalineA, AssignablePixlite.Dataline datalineB, int index) {
            super(lx);
            this.datalineA = datalineA;
            addChild(datalineA);

            this.datalineB = datalineB;
            addChild(datalineB);

            this.ipAddress = datalineA.ipAddress;
            this.index = index;

            enabled.setValue(false);
            //enabled.setValue(true);
        }

        public void setBranch(TreeModel.Branch branch) {
            List<TreeModel.Twig> twigsA = new ArrayList<>();
            twigsA.add(branch.getTwigByWiringIndex(1));
            twigsA.add(branch.getTwigByWiringIndex(2));
            twigsA.add(branch.getTwigByWiringIndex(3));
            twigsA.add(branch.getTwigByWiringIndex(4));

            List<LXPoint> pointsA = new ArrayList<>();
            for (TreeModel.Twig twig : twigsA) {
                if (twig != null) {
                    pointsA.addAll(twig.getPoints());
                } else {
                    pointsA.addAll(branch.getTwigByWiringIndex(8).getPoints()); // null
                }
            }
            datalineA.setPoints(pointsA);

            List<TreeModel.Twig> twigsB = new ArrayList<>();
            twigsB.add(branch.getTwigByWiringIndex(5));
            twigsB.add(branch.getTwigByWiringIndex(6));
            twigsB.add(branch.getTwigByWiringIndex(7));
            twigsB.add(branch.getTwigByWiringIndex(8));

            List<LXPoint> pointsB = new ArrayList<>();
            for (TreeModel.Twig twig : twigsB) {
                if (twig != null) {
                    pointsB.addAll(twig.getPoints());
                } else {
                    pointsB.addAll(branch.getTwigByWiringIndex(8).getPoints()); // null
                }
            }
            datalineB.setPoints(pointsB);

            //enabled.setValue(false);
            enabled.setValue(true);
        }
    }

    public class Dataline extends LXDatagramOutput {

        private final int DATALINE_NUM_POINTS = 600; // arbitrary
        private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;

        private final List<AssignableArtNetDatagram> artNetDatagrams = new ArrayList<>();

        public final String ipAddress;
        public final int index;
        private final int numUniverses;

        public Dataline(LX lx, String ipAddress, int index) throws SocketException {
            super(lx);
            this.ipAddress = ipAddress;
            this.index = index;
            this.numUniverses = (DATALINE_NUM_POINTS / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
            createDatagrams(lx, index*10);
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
                    indices[j] = 0;
                }
                AssignableArtNetDatagram datagram = new AssignableArtNetDatagram(lx, ipAddress, indices, indices.length*3, startUniverse++ - 1);
                artNetDatagrams.add(datagram);
                addDatagram(datagram);
            }
        }

    }
}
