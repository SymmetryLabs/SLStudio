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

    private static BranchConfig branchFromPointPair(
        double x1, double y1, double z1, double dx, double dy, double dz, TwigConfig[] twigs) {

        Vector3D d = new Vector3D(dx, dz, dy);
        d.normalize();

        Rotation rot = new Rotation(new Vector3D(0, 1, 0), d);
        double[] angles = rot.getAngles(RotationOrder.ZXY, RotationConvention.VECTOR_OPERATOR);

        double az = 180 * angles[0] / Math.PI;
        double elev = 180 * angles[2] / Math.PI;
        double tilt = 180 * angles[1] / Math.PI;

        return new BranchConfig(
            false,
            (float) x1, (float) z1, (float) y1,
            (float) az, (float) elev, (float) tilt,
            twigs);
    }


    // female
    final TwigConfig[] BRANCH_TYPE_A = new TwigConfig[]{
        new TwigConfig( 0.0f,  37.4f, 0.0f, -32.4f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(-16.3f, 36.5f, 0.0f,   6.8f, 0.0f, 0.0f, 2), // 7
        new TwigConfig(-29.8f, 25.9f, 0.0f,  28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig(-37.4f, 10.5f, 0.0f,  28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig( -5.7f,  9.5f, 0.0f,  14.4f, 0.0f, 0.0f, 5), // 4
        new TwigConfig( 14.4f, 19.2f, 0.0f,  18.0f, 0.0f, 0.0f, 6), // 3
        new TwigConfig( 24.5f, 16.3f, 0.0f, -36.0f, 0.0f, 0.0f, 7), // 2
        new TwigConfig( 16.3f,  0.0f, 0.0f, -61.2f, 0.0f, 0.0f, 8), // 1
    };

    // male
    final TwigConfig[] BRANCH_TYPE_B = new TwigConfig[] {
        new TwigConfig( 14.2f,  3.8f, 0.0f, -57.0f, 0.0f, 0.0f, 1), // 8
        new TwigConfig(  4.0f, 15.4f, 0.0f,  50.4f, 0.0f, 0.0f, 2), // 7
        new TwigConfig( 20.2f, 19.3f, 0.0f, -28.8f, 0.0f, 0.0f, 3), // 6
        new TwigConfig( 11.5f, 24.0f, 0.0f, -28.8f, 0.0f, 0.0f, 4), // 5
        new TwigConfig(-32.0f,  9.5f, 0.0f,  34.5f, 0.0f, 0.0f, 5), // 1
        new TwigConfig(-25.0f, 13.4f, 0.0f,   7.2f, 0.0f, 0.0f, 6), // 2
        new TwigConfig(-18.4f, 34.5f, 0.0f,  39.5f, 0.0f, 0.0f, 7), // 3
        new TwigConfig(  0.0f, 37.5f, 0.0f,   0.0f, 0.0f, 0.0f, 8)  // 4
    };

    final BranchConfig[] LIMB_TYPE_A = new BranchConfig[] {
        branchFromPointPair(-13.177, 8.079, 45.477, -11, 4.622, 6.35, BRANCH_TYPE_A),
        branchFromPointPair(-31.761, 8.38, 89.426, -10.748, 1.895, 6.301, BRANCH_TYPE_A),
        branchFromPointPair(35.117, 0, 71.654, 10.342, 0, 5.971, BRANCH_TYPE_A),
        branchFromPointPair(27.428, 0, 102.280, 9.261, 0, 9.261, BRANCH_TYPE_A),
        branchFromPointPair(0.405, 0, 108.301, -6.426, 0, 11.130, BRANCH_TYPE_A),
        /*
        branchFromPointPair(120.48f, 139.12f, 40.55f, 122.46f, 138.8f, 43.62f, BRANCH_TYPE_A),
        branchFromPointPair(150.29f, 149.348f, 32.465f, 152.963f, 149.371f, 34.965f, BRANCH_TYPE_A),
        branchFromPointPair(154.58f, 159.012f, 1.772f, 157.52f, 160.29f, 0.004f, BRANCH_TYPE_A),
        branchFromPointPair(134.951f, 153.771f, -32.962f, 136.648f, 154.512f, -36.121f, BRANCH_TYPE_A),
        branchFromPointPair(93.754f, 137.258f, -14.98f, 95.529f, 137.28f, -18.182f, BRANCH_TYPE_A),
        */
    };

    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        TreeConfig.createLimbType("Type A", LIMB_TYPE_A);
        TreeConfig.createBranchType("Type A", BRANCH_TYPE_A);
        TreeConfig.createBranchType("Type B", BRANCH_TYPE_B);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
                new LimbConfig(false, 0, 0, 0, 0, 0, LIMB_TYPE_A),
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
