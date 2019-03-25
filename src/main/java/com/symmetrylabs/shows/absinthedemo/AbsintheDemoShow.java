package com.symmetrylabs.shows.absinthedemo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;


import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import com.symmetrylabs.slstudio.output.TenereDatagram;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.OPCDatagram;


public class AbsintheDemoShow extends TreeShow {
    public static final String SHOW_NAME = "absinthedemo";

    final int OPC_PORT = 1337;

    public final Map<String, AssignablePixlite> pixlites = new HashMap<>();
    public final List<AssignablePixlite.Port> pixlitePorts = new ArrayList<>();


    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
      new TwigConfig( -15.36f,  16.32f, 0.0f,  43.20f, 0.0f, 0.0f, 1),
      new TwigConfig( -13.56f,  24.00f, 0.0f, -54.40f, 0.0f, 0.0f, 2),
      new TwigConfig( -15.36f,  33.60f, 0.0f,  36.00f, 0.0f, 0.0f, 3),
      new TwigConfig(  -8.64f,  44.12f, 0.0f,  39.60f, 0.0f, 0.0f, 4),
      new TwigConfig(    0.0f,  48.96f, 0.0f,    0.0f, 0.0f, 0.0f, 5),
      new TwigConfig(  12.48f,  43.20f, 0.0f, -43.20f, 0.0f, 0.0f, 6),
      new TwigConfig(  13.44f,  27.84f, 0.0f, -32.40f, 0.0f, 0.0f, 7),
      new TwigConfig(  16.32f,  20.16f, 0.0f, -64.80f, 0.0f, 0.0f, 8),
    };

    // final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
    //     new BranchConfig(false,  66, 12, 0, 52, 0, 172, BRANCH_TYPE_A),
    //     new BranchConfig(false,  40, 21, 0, 46, 0, 171, BRANCH_TYPE_A),
    //     new BranchConfig(false,  14, 33, 0, 29, 0, 171, BRANCH_TYPE_A),
    //     new BranchConfig(false,  -10, 42, 0, 7.2f, 0, 172, BRANCH_TYPE_B),
    //     new BranchConfig(false,  -32, 27, 0, -8, 0, 176, BRANCH_TYPE_B),
    //     new BranchConfig(false,  -46, 14, 0, -35, 0, 172, BRANCH_TYPE_B),
    //     new BranchConfig(false,  21, 20, 0, 14, 13, 176, BRANCH_TYPE_B),
    //     new BranchConfig(false,  -6.5f, 0, -6.48f, -5.4f, 20.88f, 176, BRANCH_TYPE_A)
    // };

    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        //TreeConfig.createLimbType("Type A", LIMB_TYPE_A);
        TreeConfig.createBranchType("Type A", BRANCH_TYPE_A);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // bottom
            new LimbConfig(false, 50, 13*FEET, -210.0f, -90, 0, new BranchConfig[] {

              // from bottom left and arching over to the right
              new BranchConfig(false, "10.200.1.70", 0, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),
              new BranchConfig(false, "10.200.1.74", 0, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),
              new BranchConfig(false, "10.200.1.72", 0, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),
              new BranchConfig(false, "10.200.1.117", 0, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),
              new BranchConfig(false, "10.200.1.189", 0, 0, 0, 0, 0, 0, 0, BRANCH_TYPE_A),
            })
        });

        return new TreeModel(config);
    }

    public void setupLx(SLStudioLX lx) {
        super.setupLx(lx);

        lx.engine.framesPerSecond.setValue(40);

        TreeModel tree = (TreeModel) (lx.model);

        List<TenereDatagram> datagrams = new ArrayList<TenereDatagram>();

        try {
          for (TreeModel.Branch branch : tree.getBranches()) {
              int pointsPerPacket = TreeModel.Twig.NUM_LEDS * 2;
              int[] channels12 = new int[pointsPerPacket];
              int[] channels34 = new int[pointsPerPacket];
              int[] channels56 = new int[pointsPerPacket];
              int[] channels78 = new int[pointsPerPacket];

              for (int i = 0; i < pointsPerPacket; ++i) {
                // Initialize to nothing
                channels12[i] = channels34[i] = channels56[i] = channels78[i] = -1;
              }

              TreeModel.Twig twig1 = branch.getTwigs().get(3); // 4
              TreeModel.Twig twig2 = branch.getTwigs().get(4); // 5
              TreeModel.Twig twig3 = branch.getTwigs().get(5); // 6
              TreeModel.Twig twig4 = branch.getTwigs().get(6); // 7
              TreeModel.Twig twig5 = branch.getTwigs().get(7); // 8
              TreeModel.Twig twig6 = branch.getTwigs().get(0); // 1
              TreeModel.Twig twig7 = branch.getTwigs().get(2); // 3
              TreeModel.Twig twig8 = branch.getTwigs().get(1); // 2


              int i12 = 0;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels12[i12++] = twig1.points[i].index;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels12[i12++] = twig2.points[i].index;

              int i34 = 0;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels34[i34++] = twig3.points[i].index;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels34[i34++] = twig4.points[i].index;

              int i56 = 0;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels56[i56++] = twig5.points[i].index;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels56[i56++] = twig6.points[i].index;

              int i78 = 0;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels78[i78++] = twig7.points[i].index;
              for (int i = 0; i < TreeModel.Twig.NUM_LEDS; i++) channels78[i78++] = twig8.points[i].index;


              // Add the datagrams
              TenereDatagram datagram1 = (TenereDatagram) new TenereDatagram(lx, channels12, (byte) 0x00).setAddress(branch.getConfig().ipAddress).setPort(OPC_PORT);
              TenereDatagram datagram2 = (TenereDatagram) new TenereDatagram(lx, channels34, (byte) 0x02).setAddress(branch.getConfig().ipAddress).setPort(OPC_PORT);
              TenereDatagram datagram3 = (TenereDatagram) new TenereDatagram(lx, channels56, (byte) 0x04).setAddress(branch.getConfig().ipAddress).setPort(OPC_PORT);
              TenereDatagram datagram4 = (TenereDatagram) new TenereDatagram(lx, channels78, (byte) 0x06).setAddress(branch.getConfig().ipAddress).setPort(OPC_PORT);
              datagrams.add(datagram1);
              datagrams.add(datagram2);
              datagrams.add(datagram3);
              datagrams.add(datagram4);
          }

          // Create an LXDatagramOutput to own these packets
          LXDatagramOutput datagramOutput = new LXDatagramOutput(lx);
          for (OPCDatagram datagram : datagrams) {
            datagramOutput.addDatagram(datagram);
          }

          // Add to the output
          lx.engine.output.addChild(datagramOutput);

        } catch (Exception x) {
          System.out.println("Failed to construct UDP output: " + x);
        }
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        //ui.preview.addComponent(new UIRocoBuilding());
        ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));
        // UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeLeaves);
        // new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
    }
}
