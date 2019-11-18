package com.symmetrylabs.shows.banyan;

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


public class BanyanShow extends TreeShow {
    public static final String SHOW_NAME = "banyan";

    public static final int NUM_BRANCHES = 80;

    final TwigConfig[] BRANCH = new TwigConfig[]{
        new TwigConfig( 0, 0, 0,0, 0, 0, 1),
        new TwigConfig( 0, 0, 0,0, 0, 0, 2),
        new TwigConfig( 0, 0, 0,0, 0, 0, 3),
        new TwigConfig( 0, 0, 0,0, 0, 0, 4),
        new TwigConfig( 0, 0, 0,0, 0, 0, 5),
        new TwigConfig( 0, 0, 0,0, 0, 0, 6),
        new TwigConfig( 0, 0, 0,0, 0, 0, 7),
        new TwigConfig( 0, 0, 0,0, 0, 0, 8),
    };

    BranchConfig[] LIMB_TYPE = new BranchConfig[NUM_BRANCHES];

    public SLModel buildModel() {
        for (int i = 0; i < NUM_BRANCHES; i++) {
            LIMB_TYPE[i] = new BranchConfig(false, 0, 0, 0, 0, 0, 0, BRANCH, true);
        }

        LimbConfig.lengthEnabled = false;
        LimbConfig.heightEnabled = false;
        LimbConfig.azimuthEnabled = false;
        LimbConfig.elevationEnabled = false;

        TreeConfig.createLimbType("Limb 1", LIMB_TYPE);
        TreeConfig.createBranchType("Branch", BRANCH);

        TreeConfig config = new TreeConfig(new LimbConfig[] {
            // just one limb
            new LimbConfig(false, 0, 0, 0, 0, 0, LIMB_TYPE),
        });

        TreeModel tree = new TreeModel(SHOW_NAME, config);

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
        //ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));

        new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
}
