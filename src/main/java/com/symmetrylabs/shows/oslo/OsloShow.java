package com.symmetrylabs.shows.oslo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.net.SocketException;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.LXLoopTask;
import heronarts.lx.transform.LXMatrix;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.shows.treeV2.*;
import heronarts.p3lx.ui.UI;

import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;


public class OsloShow extends TreeShow {
    public static final String SHOW_NAME = "oslo";

    // put in to json?
    final LimbConfig[] LIMB_CONFIGS = new LimbConfig[] {
        new LimbConfig(
            new BranchConfig[] {
                new BranchConfig("L8-2-B2", "A000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                new BranchConfig("L8-2-B2", "A000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 50, 0, 1})),
                new BranchConfig("L8-2-B2", "A000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 100, 0, 1})),
            }
        ),
        new LimbConfig(
            new LimbConfig[] {
                new LimbConfig(
                    new BranchConfig[] {
                        //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                        //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                        //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                    }
                ),
                new LimbConfig(
                    new BranchConfig[] {
                        //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                        //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                        //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
                    }
                )
            }
        ),
        new LimbConfig(
            new BranchConfig[] {
                //new BranchConfig("L8-2-B2", "a000", LXMatrix.createFromColumnMajor(new float[] {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1})),
            }
        )
    };

    public TreeModel buildModel() {
        return new TreeModel(new TreeConfig(LIMB_CONFIGS));
    }

    public void setupLx(final LX lx) {
        //super.setupLx(lx);
        // TreeModel tree = (TreeModel) (lx.model);
        // TreeModelingTool modeler = TreeModelingTool.getInstance(lx);

        // System.out.println("Number of branches: " + tree.getBranches().size());

        // lx.engine.addLoopTask(new LXLoopTask() {
        //     @Override    
        //     public void loop(double v) {
        //         if (lx.engine.framesPerSecond.getValuef() != 60) {
        //             lx.engine.framesPerSecond.setValue(60);
        //         }
        //     }
        // });

        // try {
        //     for (TreeModel.Branch branch : tree.getBranches()) {
        //         AssignableTenereController controller = new AssignableTenereController(lx, branch);
        //         controller.brightness.setValue(0.7);
        //         controllers.put(branch, controller);
        //         lx.addOutput(controller);
        //     }
        // } catch (SocketException e) { }

        // modeler.branchManipulator.ipAddress.addListener(param -> {
        //     AssignableTenereController controller = controllers.get(modeler.getSelectedBranch());
        //     controller.setIpAddress(modeler.branchManipulator.ipAddress.getString());
        // });
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        //super.setupUi(lx, ui);
        //ui.preview.addComponent(new UITreeStructure((TreeModel) lx.model));

        //new UITenereControllers(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.model);
    }
}
