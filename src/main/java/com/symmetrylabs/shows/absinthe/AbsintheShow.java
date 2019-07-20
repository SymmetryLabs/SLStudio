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

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LXLoopTask;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.config.*;
import com.symmetrylabs.shows.tree.ui.*;
import com.symmetrylabs.shows.tree.ui.UITenereControllers;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dComponent;
import processing.core.PGraphics;


public class AbsintheShow extends TreeShow {
    public static final String SHOW_NAME = "absinthe";

    final TwigConfig[] BRANCH = new TwigConfig[]{
        new TwigConfig( -15.36f,  16.32f, 0.0f,  57.20f, 180.0f, 0.0f, 4),
        new TwigConfig(  -9.56f,  24.00f, 0.0f, -54.40f, 180.0f, 0.0f, 2),
        new TwigConfig( -15.36f,  33.60f, 0.0f,  36.00f, 180.0f, 0.0f, 3),
        new TwigConfig(  -8.64f,  44.12f, 0.0f,  39.60f, 180.0f, 0.0f, 1),
        new TwigConfig(    0.0f,  48.96f, 0.0f,    0.0f, 180.0f, 0.0f, 0),
        new TwigConfig(  12.48f,  43.20f, 0.0f, -43.20f, 180.0f, 0.0f, 7),
        new TwigConfig(  13.44f,  27.84f, 0.0f, -26.40f, 180.0f, 0.0f, 6),
        new TwigConfig(  16.32f,  20.16f, 0.0f, -64.80f, 180.0f, 0.0f, 5),
    };

    final BranchConfig[] LIMB_TYPE_L1 = new BranchConfig[] {
        new BranchConfig(false, -30.61f,  87.01f,  -0.44f,  60.0f, 0, 1.0f, BRANCH, true),
        new BranchConfig(false, -20.52f,  120.40f,  3.15f,  47.5f, 0, 20f,  BRANCH, true),
        new BranchConfig(false, -5.86f,   125.31f,  0.44f,  -30.0f,  0, 3.0f, BRANCH, true),
        new BranchConfig(false, 29.11f, 105.08f,  3.72f,  -61.1f,  0, 7.0f, BRANCH, true),
        new BranchConfig(false, 8.6f,   59.68f,   -3.37f, -60.0f,  0, 8.5f, BRANCH, true),
    };

    final BranchConfig[] LIMB_TYPE_L2 = new BranchConfig[] {
        new BranchConfig(false, 0.25f,   49.67f,  -10.52f, 0,  29,  15,  BRANCH, false),
        new BranchConfig(false, -8.62f,  61.81f,  -3.37f, 61,  5,   15,  BRANCH, false),
        new BranchConfig(false, -30.71f, 103.38f, 3.41f,  61,  -3,  -6,  BRANCH, false),
        new BranchConfig(false, 4.03f,   118.11f, 0.20f,  30,  0,   3,   BRANCH, false),
        new BranchConfig(false, 20.44f,  121.61f, 0.76f,  -45, 10,  20,  BRANCH, false),
        new BranchConfig(false, 29.94f,  87.55f,  -1.34f, -60, 10,  20,  BRANCH, false),

    };

    final BranchConfig[] LIMB_TYPE_L3 = new BranchConfig[] {
        new BranchConfig(false, -6.93f, 67.40f, -0.49f, 50,  5,  -5,  BRANCH, false),
        new BranchConfig(false, 16.84f, 92.66f, 2.95f,  15,  -7, -10, BRANCH, false),
        new BranchConfig(false, 31.99f, 88.76f, 0.56f,  -59, 10,  0,  BRANCH, false),
    };

    final BranchConfig[] LIMB_TYPE_SINGLE_BRANCH = new BranchConfig[] {
        new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH, true),
    };

    public SLModel buildModel() {
        TwigConfig.setZEnabled(false);
        TwigConfig.setElevationEnabled(false);
        TwigConfig.setTiltEnabled(false);

        TreeConfig.createLimbType("Limb/L1", LIMB_TYPE_L1);
        TreeConfig.createLimbType("Limb/L2", LIMB_TYPE_L2);
        TreeConfig.createLimbType("Limb/L3", LIMB_TYPE_L3);
        TreeConfig.createLimbType("Limb/Single Branch", LIMB_TYPE_SINGLE_BRANCH);
        TreeConfig.createBranchType("Branch", BRANCH);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
//                // L7
//                new LimbConfig(false, 15, 130, 0,   -90 + 13.5f, 0, LIMB_TYPE_L1),
//                new LimbConfig(false, 15, 130, 90,  -90 + 13.5f, 0, LIMB_TYPE_L1),
//                new LimbConfig(false, 15, 130, 180, -90 + 13.5f, 0, LIMB_TYPE_L1),
//                new LimbConfig(false, 15, 130, -90, -90 + 13.5f, 0, LIMB_TYPE_L1),
//
//                // L6
//                new LimbConfig(false, 13.5f, 170, 45,   -90 + 18.5f, 0, LIMB_TYPE_L1),
//                new LimbConfig(false, 13.5f, 170, 135,  -90 + 18.5f, 0, LIMB_TYPE_L1),
//                new LimbConfig(false, 13.5f, 170, -135, -90 + 18.5f, 0, LIMB_TYPE_L1),
//                new LimbConfig(false, 13.5f, 170, -45,  -90 + 18.5f, 0, LIMB_TYPE_L1),
//
//                // L5
//                new LimbConfig(false, 11.5f, 210, 0,   90 - 23.5f, 0, LIMB_TYPE_L2),
//                new LimbConfig(false, 11.5f, 210, 90,  90 - 23.5f, 0, LIMB_TYPE_L2),
//                new LimbConfig(false, 11.5f, 210, 180, 90 - 23.5f, 0, LIMB_TYPE_L2),
//                new LimbConfig(false, 11.5f, 210, -90, 90 - 23.5f, 0, LIMB_TYPE_L2),
//
//                // L4
//                new LimbConfig(false, 9.5f, 250, 45,   90 - 23.5f, 0, LIMB_TYPE_L2),
//                new LimbConfig(false, 9.5f, 250, 135,  90 - 23.5f, 0, LIMB_TYPE_L2),
//                new LimbConfig(false, 9.5f, 250, -135, 90 - 23.5f, 0, LIMB_TYPE_L2),
//                new LimbConfig(false, 9.5f, 250, -45,  90 - 23.5f, 0, LIMB_TYPE_L2),
//
//                // L3
//                new LimbConfig(false, 7.5f, 290, 0,   90 - 17f, 0, LIMB_TYPE_L3),
//                new LimbConfig(false, 7.5f, 290, 90,  90 - 17f, 0, LIMB_TYPE_L3),
//                new LimbConfig(false, 7.5f, 290, 180, 90 - 17f, 0, LIMB_TYPE_L3),
//                new LimbConfig(false, 7.5f, 290, -90, 90 - 17f, 0, LIMB_TYPE_L3),
//
//                // L2
//                new LimbConfig(false, 24, 320, 45,   90 - 25,  0, LIMB_TYPE_SINGLE_BRANCH),
//                new LimbConfig(false, 24, 320, 135,  90 - 25,  0, LIMB_TYPE_SINGLE_BRANCH),
//                new LimbConfig(false, 24, 320, -135, 90 - 25,  0, LIMB_TYPE_SINGLE_BRANCH),
//                new LimbConfig(false, 24, 320, -45,  90 - 25,  0, LIMB_TYPE_SINGLE_BRANCH),

                // L1
                new LimbConfig(false, 14, 345, 0,    90 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
                new LimbConfig(false, 14, 345, 90,   90 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
                new LimbConfig(false, 14, 345, 180,  90 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
            new LimbConfig(false, 14, 240, 75,  80 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
            new LimbConfig(false, 14, 345, 180,  70 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
//                new LimbConfig(false, 14, 345, -90,  90 - 15,  0, LIMB_TYPE_SINGLE_BRANCH),
            });

        TreeModel tree = new TreeModel(SHOW_NAME, config);
//        tree.rotateY(35);

        return tree;
    }

    public void setupLx(final LX lx) {
        super.setupLx(lx);
        TreeModel tree = (TreeModel) (lx.model);
        TreeModelingTool modeler = TreeModelingTool.getInstance(lx);

        System.out.println("Number of branches: " + tree.getBranches().size());

        lx.engine.addLoopTask(new LXLoopTask() {
            @Override
            public void loop(double v) {
                if (lx.engine.framesPerSecond.getValuef() != 60) {
                    lx.engine.framesPerSecond.setValue(60);
                }
            }
        });

        try {
            for (TreeModel.Branch branch : tree.getBranches()) {
                AssignableTenereController controller = new AssignableTenereController(lx, branch);
                controller.brightness.setValue(0.7);
                controllers.put(branch, controller);
                lx.addOutput(controller);
            }
        } catch (SocketException e) { }

        modeler.branchManipulator.ipAddress.addListener(param -> {
            AssignableTenereController controller = controllers.get(modeler.getSelectedBranch());
            controller.setIpAddress(modeler.branchManipulator.ipAddress.getString());
        });
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));
        ui.preview.addComponent(new UITent(0, 60 * FEET));

        new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
}

class UITent extends UI3dComponent {

    private final static int NUM_SIDES = 24;
    private final static float RADIUS = 35 * FEET;

    private float xPosition;
    private float zPosition;

    public UITent(float x, float z) {
        this.xPosition = x;
        this.zPosition = z;
    }

    protected void onDraw(UI ui, PGraphics pg) {
        float angle = 360 / NUM_SIDES;

        pg.beginShape();
        pg.stroke(0xffff0000);
        pg.fill(0xff222222);

        for (int i = 0; i < NUM_SIDES; i++) {
            float x = cos(radians(i * angle)) * RADIUS;
            float z = sin(radians(i * angle)) * RADIUS;
            pg.vertex(xPosition + x, 0, zPosition + z);
        }
        pg.endShape();
    }
}
