package com.symmetrylabs.layouts.tree;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.output.LXDatagramOutput;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.layouts.tree.*;
import com.symmetrylabs.layouts.tree.config.*;
import com.symmetrylabs.slstudio.output.ArtNetDatagram;
import static com.symmetrylabs.util.DistanceConstants.*;


public class KalpaLayout extends TreeLayout implements Layout {

    //public final Map<TreeModel.Branch, PixliteOutput> pixliteOutputs = new HashMap<>();

    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
        new TwigConfig(  16.3f,  0.0f, 0.0f, -61.2f, 0.0f, 0.0f),
        new TwigConfig(  24.5f, 16.3f, 0.0f, -36.0f, 0.0f, 0.0f),
        new TwigConfig(  14.4f, 19.2f, 0.0f,  18.0f, 0.0f, 0.0f),
        new TwigConfig(  -5.7f,  9.5f, 0.0f,  14.4f, 0.0f, 0.0f),
        new TwigConfig( -37.4f, 10.5f, 0.0f,  28.8f, 0.0f, 0.0f),
        new TwigConfig( -29.8f, 25.9f, 0.0f,  28.8f, 0.0f, 0.0f),
        new TwigConfig( -16.3f, 36.5f, 0.0f,   6.8f, 0.0f, 0.0f),
        new TwigConfig(  0.0f,  37.4f, 0.0f, -32.4f, 0.0f, 0.0f)
    };

    final TwigConfig[] BRANCH_TYPE_B = new TwigConfig[] {
        new TwigConfig( 14.2f,  3.8f, 0.0f, -57.0f, 0.0f, 0.0f),
        new TwigConfig(  4.0f, 15.4f, 0.0f,  50.4f, 0.0f, 0.0f),
        new TwigConfig( 20.2f, 19.3f, 0.0f, -28.8f, 0.0f, 0.0f),
        new TwigConfig( 11.5f, 24.0f, 0.0f, -28.8f, 0.0f, 0.0f),
        new TwigConfig(-32.0f,  9.5f, 0.0f,  34.5f, 0.0f, 0.0f),
        new TwigConfig(-25.0f, 13.4f, 0.0f,   7.2f, 0.0f, 0.0f),
        new TwigConfig(-18.4f, 34.5f, 0.0f,  39.5f, 0.0f, 0.0f),
        new TwigConfig(  0.0f, 37.5f, 0.0f,   0.0f, 0.0f, 0.0f)
    };

    final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
        new BranchConfig(-46.8f, 33.0f, -10.8f,  32.4f,  0.0f, -10.8f, BRANCH_TYPE_A),
        new BranchConfig(-72.0f,  0.0f,   0.0f,  46.8f,  0.0f, -14.4f, BRANCH_TYPE_B),
        new BranchConfig(  0.0f, 57.0f, -14.4f,  21.6f,  0.0f, -14.4f, BRANCH_TYPE_A),
        new BranchConfig( 50.4f, 61.5f, -17.3f, -21.6f,  0.0f, -14.4f, BRANCH_TYPE_B),
        new BranchConfig(104.4f, 45.0f, -14.4f, -36.0f, -7.2f, -14.4f, BRANCH_TYPE_A),
        new BranchConfig(  0.0f,  9.0f,   0.0f,  25.2f,  0.0f,   7.2f, BRANCH_TYPE_B),
        new BranchConfig( 46.8f,  9.0f,   0.0f, -18.0f,  0.0f,  14.4f, BRANCH_TYPE_A)
    };

    @Override
    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        TreeConfig.createLimbType("Type A", LIMB_TYPE_A);
        TreeConfig.createBranchType("Type A", BRANCH_TYPE_A);
        TreeConfig.createBranchType("Type B", BRANCH_TYPE_B);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // bottom
            new LimbConfig(0, 6*FEET, 1*45, -90, 0, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 2*45, -90, 0, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 3*45, -90, 0, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 4*45, -90, 0, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 5*45, -90, 0, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 6*45, -90, 0, LIMB_TYPE_A),
            new LimbConfig(0, 6*FEET, 7*45, -90, 0, LIMB_TYPE_A)
            // // middle
            // new LimbConfig(0, 6*FEET, 0*120, -65, LIMB_TYPE_A),
            // new LimbConfig(0, 6*FEET, 1*120, -65, LIMB_TYPE_A),
            // new LimbConfig(0, 6*FEET, 2*120, -65, LIMB_TYPE_A),

            // // top
            // new LimbConfig(0, 5*FEET, 0*120+60, -35, LIMB_TYPE_B),
            // new LimbConfig(0, 5*FEET, 1*120+60, -35, LIMB_TYPE_B),
            // new LimbConfig(0, 5*FEET, 2*120+60, -35, LIMB_TYPE_B),

            // new LimbConfig(0, 5*FEET, 0*120, -25, LIMB_TYPE_B),
            // new LimbConfig(0, 5*FEET, 1*120, -25, LIMB_TYPE_B),
            // new LimbConfig(0, 5*FEET, 2*120, -25, LIMB_TYPE_B),
        });

        return new TreeModel(config);
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        //TreeModelingTool.BranchManipulator manipulator = SLStudio.applet.treeModelingTool.branchManipulator;

//        for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
//            try {
//                PixliteOutput output = new PixliteOutput(lx, branch, manipulator);
//                pixliteOutputs.put(branch, output);
//                lx.addOutput(output);
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        // ui.preview.addComponent(new UITreeGround(applet));
        // UITreeStructure uiTreeStructure = new UITreeStructure((TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeStructure);
        // UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeLeaves);
        // new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
    }
}

//class PixliteOutput extends LXOutputGroup {
//
//    private final TreeModel.Branch branch;
//    private final ReceiverChannel channelA;
//    private final ReceiverChannel channelB;
//
//    PixliteOutput(LX lx, TreeModel.Branch branch, TreeModelingTool.BranchManipulator manipulator) throws SocketException {
//        super(lx);
//        this.branch = branch;
//
//        this.channelA = new ReceiverChannel(lx, branch, ReceiverChannel.Channel.A);
//        addChild(channelA);
//
//        this.channelB = new ReceiverChannel(lx, branch, ReceiverChannel.Channel.B);
//        addChild(channelB);
//
//        manipulator.ipAddress.addListener(parameter -> {
//            channelA.setIpAddress(manipulator);
//            channelB.setIpAddress(manipulator);
//        });
//
//        manipulator.channel.addListener(parameter -> {
//            channelA.setUniverses(manipulator);
//            channelB.setUniverses(manipulator);
//        });
//    }
//
//    private static class ReceiverChannel extends LXDatagramOutput {
//        private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
//        private final String DEFAULT_IP = "10.200.1.1";
//
//        public static enum Channel { A, B }
//        public final Channel channel;
//        private final List<LXPoint> points;
//
//        private final int numPoints;
//        private final int numUniverses;
//
//        private final List<ArtNetDatagram> artNetDatagrams = new ArrayList<>();
//
//        private ReceiverChannel(LX lx, TreeModel.Branch branch, Channel channel) throws SocketException {
//            super(lx);
//            this.channel = channel;
//            this.points = setPoints(branch);
//            this.numPoints = points.size();
//            this.numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
//            createDatagrams(lx);
//        }
//
//        private List<LXPoint> setPoints(TreeModel.Branch branch) {
//            List<TreeModel.Twig> twigs = new ArrayList<>();
//            switch (channel) {
//                case A: twigs.addAll(branch.getTwigs().subList(0, 3));
//                case B: twigs.addAll(branch.getTwigs().subList(4, 7));
//            }
//
//            List<LXPoint> points = new ArrayList<>();
//            for (TreeModel.Twig twig : twigs) {
//                points.addAll(twig.getPoints());
//            }
//            return points;
//        }
//
//        public void setIpAddress(TreeModelingTool.BranchManipulator manipulator) {
//            for (ArtNetDatagram datagram : artNetDatagrams) {
//                try {
//                    datagram.setAddress(InetAddress.getByName(manipulator.ipAddress.getString()));
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        private int getStartUniverse(int output) {
//            switch(channel) {
//                case A: return output*10;
//                case B: return (output+1)*10;
//            }
//            return 0;
//        }
//
//        public void setUniverses(TreeModelingTool.BranchManipulator manipulator) {
//            int universe = getStartUniverse(manipulator.channel.getValuei());
//            int i = 0;
//            for (ArtNetDatagram datagram : artNetDatagrams){
//                datagram.setUniverse(universe + i++);
//            }
//        }
//
//        private void createDatagrams(LX lx) {
//            int counter = 0;
//
//            for (int i = 0; i < numUniverses; i++) {
//                int universe = 0;
//
//                int numIndices = ((i + 1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints
//                    ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE)
//                    : MAX_NUM_POINTS_PER_UNIVERSE;
//
//                int[] indices = new int[numIndices];
//                for (int i1 = 0; i1 < numIndices; i1++) {
//                    indices[i1] = points.get(counter++).index;
//                }
//
//                ArtNetDatagram datagram = new ArtNetDatagram(lx, DEFAULT_IP, indices, universe - 1);
//                artNetDatagrams.add(datagram);
//                addDatagram(datagram);
//            }
//        }
//    }
//}
