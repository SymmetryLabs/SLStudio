package com.symmetrylabs.shows.absinthe;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;


import heronarts.lx.model.LXPoint;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import com.symmetrylabs.shows.tree.ui.UIScheduler;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import static com.symmetrylabs.util.DistanceConstants.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;
import static com.symmetrylabs.util.DistanceConstants.*;


public class AbsintheShow extends TreeShow {
    public static final String SHOW_NAME = "absinthe";

    public final Map<String, AssignablePixlite> pixlites = new HashMap<>();
    public final List<AssignablePixlite.Port> pixlitePorts = new ArrayList<>();

    final TwigConfig[] BRANCH = new TwigConfig[]{
        new TwigConfig( -15.36f,  16.32f, 0.0f,  43.20f, 0.0f, 0.0f, 1),
        new TwigConfig( -13.56f,  24.00f, 0.0f, -54.40f, 0.0f, 0.0f, 2),
        new TwigConfig( -15.36f,  33.60f, 0.0f,  36.00f, 0.0f, 0.0f, 3),
        new TwigConfig(  -8.64f,  44.12f, 0.0f,  39.60f, 0.0f, 0.0f, 4),
        new TwigConfig(    0.0f,  48.96f, 0.0f,    0.0f, 0.0f, 0.0f, 5),
        new TwigConfig(  12.48f,  43.20f, 0.0f, -43.20f, 0.0f, 0.0f, 6),
        new TwigConfig(  13.44f,  27.84f, 0.0f, -32.40f, 0.0f, 0.0f, 7),
        new TwigConfig(  16.32f,  20.16f, 0.0f, -64.80f, 0.0f, 0.0f, 8),
    };

    final BranchConfig[] LIMB_TYPE_L1 = new BranchConfig[] {
        new BranchConfig(false, 30.61f,  87.01f,  -0.44f,  -60.0f, 0, 1.0f, BRANCH),
        new BranchConfig(false, -29.11f, 105.08f,  3.72f,  61.1f,  0, 7.0f, BRANCH),
        new BranchConfig(false, 5.86f,   125.31f,  0.44f,  30.0f,  0, 3.0f, BRANCH),
        new BranchConfig(false, -8.6f,   59.68f,   -3.37f, 60.0f,  0, 8.5f, BRANCH),
        new BranchConfig(false, 20.52f,  120.40f,  3.15f,  -47.5f, 0, 20f,  BRANCH),
    };

    final BranchConfig[] LIMB_TYPE_L2 = new BranchConfig[] {
        new BranchConfig(false, 29.94f,  87.55f,  -1.34f, -60, 10,  20,  BRANCH),
        new BranchConfig(false, -30.71f, 103.38f, 3.41f,  61,  -3,  -6,  BRANCH),
        new BranchConfig(false, 4.03f,   118.11f, 0.20f,  30,  0,   3,   BRANCH),
        new BranchConfig(false, -8.62f,  61.81f,  -3.37f, 61,  5,   15,  BRANCH),
        new BranchConfig(false, 20.44f,  121.61f, 0.76f,  -45, 10,  20,  BRANCH),
        new BranchConfig(false, 0.25f,   49.67f,  -10.52f, 0,  29,  15,  BRANCH),
    };

    final BranchConfig[] LIMB_TYPE_L3 = new BranchConfig[] {
        new BranchConfig(false, 16.84f, 92.66f, 2.95f,  15,  -7, -10, BRANCH),
        new BranchConfig(false, -6.93f, 67.40f, -0.49f, 50,  5,  -5,  BRANCH),
        new BranchConfig(false, 31.99f, 88.76f, 0.56f,  -59, 10,  0,  BRANCH),
    };

    final BranchConfig[] LIMB_TYPE_L4 = new BranchConfig[] {
        new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH),
    };

    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        TreeConfig.createLimbType("Type L1", LIMB_TYPE_L1);
        TreeConfig.createLimbType("Type L2", LIMB_TYPE_L2);
        TreeConfig.createLimbType("Type L3", LIMB_TYPE_L3);
        TreeConfig.createLimbType("Type L4", LIMB_TYPE_L4);
        TreeConfig.createBranchType("Type A", BRANCH);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
                // L7
                new LimbConfig(false, 15, 130, 0,   -90 + 13.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 15, 130, 90,  -90 + 13.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 15, 130, 180, -90 + 13.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 15, 130, -90, -90 + 13.5f, 0, LIMB_TYPE_L1),

                // L6
                new LimbConfig(false, 13.5f, 170, 45,   -90 + 18.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 13.5f, 170, 135,  -90 + 18.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 13.5f, 170, -135, -90 + 18.5f, 0, LIMB_TYPE_L1),
                new LimbConfig(false, 13.5f, 170, -45,  -90 + 18.5f, 0, LIMB_TYPE_L1),

                // L5
                new LimbConfig(false, 11.5f, 210, 0,   90 - 23.5f, 0, LIMB_TYPE_L2),
                new LimbConfig(false, 11.5f, 210, 90,  90 - 23.5f, 0, LIMB_TYPE_L2),
                new LimbConfig(false, 11.5f, 210, 180, 90 - 23.5f, 0, LIMB_TYPE_L2),
                new LimbConfig(false, 11.5f, 210, -90, 90 - 23.5f, 0, LIMB_TYPE_L2),

                // L4
                new LimbConfig(false, 9.5f, 250, 45,   90 - 23.5f, 0, LIMB_TYPE_L2),
                new LimbConfig(false, 9.5f, 250, 135,  90 - 23.5f, 0, LIMB_TYPE_L2),
                new LimbConfig(false, 9.5f, 250, -135, 90 - 23.5f, 0, LIMB_TYPE_L2),
                new LimbConfig(false, 9.5f, 250, -45,  90 - 23.5f, 0, LIMB_TYPE_L2),
            });
        return new TreeModel(config);
    }

    public void setupLx(SLStudioLX lx) {
        super.setupLx(lx);
        //lx.engine.framesPerSecond.setValue(30);

        lx.engine.registerComponent("scheduleControls", ScheduleControls.getInstance(lx));
        lx.engine.addLoopTask(ScheduleControls.getInstance(lx));

        final String[] ipAddresses = new String[] {
            "10.200.1.100", "10.200.1.101", "10.200.1.102", "10.200.1.103", "10.200.1.104",
            "10.200.1.105", "10.200.1.106", "10.200.1.107", "10.200.1.108", "10.200.1.109"
        };

        for (int i = 0; i < ipAddresses.length; i++) {
            AssignablePixlite pixlite = new AssignablePixlite(lx, ipAddresses[i]);
            pixlites.put(ipAddresses[i], pixlite);
            pixlitePorts.addAll(pixlite.ports);
            lx.addOutput(pixlite);
        }

        //System.out.println("------------------------------");
        for (AssignablePixlite.Port port : pixlitePorts) {
            for (TreeModel.Branch branch : ((TreeModel)lx.model).getBranches()) {
                if (port.ipAddress.equals(branch.getConfig().ipAddress)
                    && port.index == branch.getConfig().channel) {
                    //System.out.println(port.index + " - " + branch.getConfig().channel);
                    port.setBranch(branch);
                }
            }
        }
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        //ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));
        // UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, (TreeModel) lx.model);
        // ui.preview.addComponent(uiTreeLeaves);
        // new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
        new UIScheduler(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.utility);
    }
}
