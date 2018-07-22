package com.symmetrylabs.layouts.tree;

import java.util.List;
import java.util.ArrayList;
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
import com.symmetrylabs.layouts.tree.config.*;


public class PixliteOutput extends LXOutputGroup {

    private final TreeModelingTool.BranchManipulator manipulator;
    private final BranchConfig config;
    public final ReceiverChannel channelA;
    public final ReceiverChannel channelB;

    PixliteOutput(LX lx, TreeModel.Branch branch) throws SocketException {
        super(lx);
        this.manipulator = SLStudio.applet.treeModelingTool.branchManipulator;
        this.config = branch.getConfig();

        this.channelA = new ReceiverChannel(lx, branch, ReceiverChannel.Type.A);
        addChild(channelA);

        this.channelB = new ReceiverChannel(lx, branch, ReceiverChannel.Type.B);
        addChild(channelB);

        manipulator.ipAddress.addListener(parameter -> {
            String ipAddress = ((StringParameter)parameter).getString();
            channelA.ipAddress.setValue(ipAddress);
            channelB.ipAddress.setValue(ipAddress);
        });

        manipulator.channel.addListener(parameter -> {
            // these become pixlite physical port indices
            channelA.pixlitePort.setValue(((DiscreteParameter)parameter).getValuei()*2-1);
            channelB.pixlitePort.setValue(((DiscreteParameter)parameter).getValuei()*2);
        });
    }

    public static class ReceiverChannel extends LXDatagramOutput {
        private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
        private final String DEFAULT_IP = "10.200.1.10";
        private final int DEFAULT_UNIVERSE = 0;

        // this is to be able to update points in a datagram and not have to resize buffer
        // (need to fix so we don't waste network traffic
        private final int DATAGRAM_NUM_POINTS = 500;

        public static enum Type { A, B }
        public final Type type;
        private final List<LXPoint> points;

        private final int numPoints;
        private final int numUniverses;

        public final List<ArtNetDatagram> artNetDatagrams = new ArrayList<>();

        public final StringParameter ipAddress = new StringParameter("ip", DEFAULT_IP) {
            public void onParameterChanged(LXParameter parameter) {
                for (ArtNetDatagram datagram : artNetDatagrams) {
                    try {
                        datagram.setAddress(InetAddress.getByName(ipAddress.getString()));
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        public final DiscreteParameter pixlitePort = new DiscreteParameter("port", 1, 9) {
            public void onParameterChanged(LXParameter parameter) {
                int firstUniverse = getFirstUniverse(pixlitePort.getValuei());
                int i = 0;
                for (ArtNetDatagram datagram : artNetDatagrams){
                    datagram.setUniverse(firstUniverse + i++);
                }
            }
        };

        private ReceiverChannel(LX lx, TreeModel.Branch branch, Type type) throws SocketException {
            super(lx);
            this.type = type;
            this.points = setPoints(branch);
            this.numPoints = points.size();
            this.numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
            createDatagrams(lx);
        }

        private List<LXPoint> setPoints(TreeModel.Branch branch) {
            List<TreeModel.Twig> twigs = new ArrayList<>();
            switch (type) {
                case A:
                    twigs.add(branch.getTwigByWiringIndex(1));
                    twigs.add(branch.getTwigByWiringIndex(2));
                    twigs.add(branch.getTwigByWiringIndex(3));
                    twigs.add(branch.getTwigByWiringIndex(4));
                    break;
                case B:
                    twigs.add(branch.getTwigByWiringIndex(5));
                    twigs.add(branch.getTwigByWiringIndex(6));
                    twigs.add(branch.getTwigByWiringIndex(7));
                    twigs.add(branch.getTwigByWiringIndex(8));
                    break;
            }

            List<LXPoint> points = new ArrayList<>();
            for (TreeModel.Twig twig : twigs) {
                if (twig != null) {
                    points.addAll(twig.getPoints());
                }
            }
            return points;
        }

        private int getFirstUniverse(int pixlitePort) {
            switch(type) {
                case A: return pixlitePort*10;
                case B: return (pixlitePort+1)*10;
            }
            return 0;
        }

        private void createDatagrams(LX lx) {
            int counter = 0;

            for (int i = 0; i < numUniverses; i++) {
                int universe = 0;

                int numIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints
                    ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE)
                    : MAX_NUM_POINTS_PER_UNIVERSE;

                int[] indices = new int[numIndices];
                for (int i1 = 0; i1 < numIndices; i1++) {
                    indices[i1] = points.get(counter++).index;
                }

                ArtNetDatagram datagram = new ArtNetDatagram(lx, DEFAULT_IP, indices, DATAGRAM_NUM_POINTS*3, universe - 1);
                artNetDatagrams.add(datagram);
                addDatagram(datagram);
            }
        }
    }
}
