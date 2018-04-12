package com.symmetrylabs.layouts.dollywood;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXMatrix;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import processing.core.PApplet;

import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.layouts.oslo.UITreeControls;
import com.symmetrylabs.layouts.oslo.UITreeGround;
import com.symmetrylabs.layouts.oslo.UITreeLeaves;
import com.symmetrylabs.layouts.oslo.UITreeStructure;
import com.symmetrylabs.layouts.dollywood.UIButterflies;

import com.symmetrylabs.util.MathUtils;

public class DollywoodLayout implements Layout {
    private final PApplet applet;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final ButterflyConfig[] BUTTERFLY_CONFIG = {
        new ButterflyConfig("butterfly1_", DollywoodModel.Butterfly.Type.LARGE, new float[] {-30, 70, 110}, new float[] {0, 0, 45}),
        new ButterflyConfig("butterfly2_", DollywoodModel.Butterfly.Type.SMALL, new float[] {-20, 82, 110}, new float[] {0, 0, 0}),
        new ButterflyConfig("butterfly3_", DollywoodModel.Butterfly.Type.LARGE, new float[] {-10, 70, 110}, new float[] {0, 0, -45}),

        new ButterflyConfig("butterfly4_", DollywoodModel.Butterfly.Type.SHARP_CURVY, new float[] {-60, 70, 110}, new float[] {0, 0, 0}),
        new ButterflyConfig("butterfly5_", DollywoodModel.Butterfly.Type.CURVY, new float[] {-90, 70, 110}, new float[] {0, 0, 0}),
    };

    public DollywoodLayout(PApplet applet) {
        this.applet = applet;
    }

    static class ButterflyConfig {
        String id;
        DollywoodModel.Butterfly.Type type;
        float x;
        float y;
        float z;
        float xRot;
        float yRot;
        float zRot;

        ButterflyConfig(String id, DollywoodModel.Butterfly.Type type, float[] coordinates, float[] rotations) {
            this.id = id;
            this.type = type;
            this.x = coordinates[0];
            this.y = coordinates[1];
            this.z = coordinates[2];
            this.xRot = rotations[0];
            this.yRot = rotations[1];
            this.zRot = rotations[2];
        }
    }

    // private LXMatrix getRandomButterflyTransform(TreeModel treeModel) {
    //   return treeModel.leaves.get((int)random(treeModel.leaves.size()-1)).transform;
    // }

    @Override
    public SLModel buildModel() {
        List<DollywoodModel.Butterfly> butterflies = new ArrayList<>();

        // Any global transforms
        LXTransform transform = new LXTransform();

        TreeModel treeModel = new TreeModel(applet, TreeModel.ModelMode.MAJOR_LIMBS);

        // add the "hardcoded" mappings
        for (ButterflyConfig config : BUTTERFLY_CONFIG) {
            String id = config.id;
            DollywoodModel.Butterfly.Type type = config.type;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;

            transform.push();
            transform.translate(x, y, z);
            transform.rotateX(xRot * Math.PI / 180.);
            transform.rotateY(yRot * Math.PI / 180.);
            transform.rotateZ(zRot * Math.PI / 180.);

            DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly(id, type, transform);
            butterflies.add(butterfly);
            transform.pop();
        }

        // generate random mappings
        // for (int i = 0; i < 1000; i++) {
        //     LXTransform transform1 = new LXTransform(getRandomButterflyTransform(treeModel));
        //     DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly("0", DollywoodModel.Butterfly.Type.LARGE, transform1);
        //     butterflies.add(butterfly);
        // }

//        for (TreeModel.LeafAssemblage assemblage : treeModel.assemblages) {
//            LXTransform transform1 = new LXTransform(assemblage.transform);
//            transform1.translate(0, 14, 0);
//            DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly("0", DollywoodModel.Butterfly.Type.LARGE, transform1);
//            butterflies.add(butterfly);
//        }

        for (TreeModel.LeafAssemblage assemblage : treeModel.assemblages) {
            LXTransform transform1 = new LXTransform(assemblage.transform);
            transform1.translate(MathUtils.random(25), MathUtils.random(25), MathUtils.random(25));
            DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly("0", DollywoodModel.Butterfly.Type.LARGE, transform1);
            butterflies.add(butterfly);
        }


        // for (TreeModel.LeafAssemblage assemblage : treeModel.assemblages) {
        //     LXTransform transform1 = new LXTransform(assemblage.transform);
        //     transform1.translate(30, 30, 30);
        //     DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly("0", DollywoodModel.Butterfly.Type.LARGE, transform1);
        //     butterflies.add(butterfly);
        // }

        for (TreeModel.LeafAssemblage assemblage : treeModel.assemblages) {
            LXTransform transform1 = new LXTransform(assemblage.transform);
            transform1.translate(25, 25, 25);
            DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly("0", DollywoodModel.Butterfly.Type.LARGE, transform1);
            butterflies.add(butterfly);
        }

        for (TreeModel.LeafAssemblage assemblage : treeModel.assemblages) {
            LXTransform transform1 = new LXTransform(assemblage.transform);
            transform1.translate(20, 20, 20);
            DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly("0", DollywoodModel.Butterfly.Type.LARGE, transform1);
            butterflies.add(butterfly);
        }

        return new DollywoodModel(applet, treeModel, butterflies);
    }

    @Override
    public void setupLx(SLStudioLX lx) {
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        ui.preview.addComponent(new UIButterflies(lx, applet, ((DollywoodModel)lx.model).getButterflies()));

        ui.preview.addComponent(new UITreeGround(applet));
        UITreeStructure uiTreeStructure = new UITreeStructure(((DollywoodModel)lx.model).treeModel);
        ui.preview.addComponent(uiTreeStructure);
        UITreeLeaves uiTreeLeaves = new UITreeLeaves(lx, applet, ((DollywoodModel)lx.model).treeModel);
        ui.preview.addComponent(uiTreeLeaves);
        new UITreeControls(ui, uiTreeStructure, uiTreeLeaves).setExpanded(false).addToContainer(ui.leftPane.global);
    }
}
