package com.symmetrylabs.layouts.dollywood;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import heronarts.lx.transform.LXTransform;

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

public class DollywoodLayout implements Layout {
    private final PApplet applet;

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final ButterflyConfig[] BUTTERFLY_CONFIG = {
        new ButterflyConfig("butterfly1_", DollywoodModel.Butterfly.Type.LARGE, new float[] {-30, 70, 210}, new float[] {0, 0, 0}),
        new ButterflyConfig("butterfly2_", DollywoodModel.Butterfly.Type.SMALL, new float[] {-60, 70, 210}, new float[] {0, 0, 0})
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

    @Override
    public SLModel buildModel() {
        List<DollywoodModel.Butterfly> butterflies = new ArrayList<>();

        // Any global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateY(globalRotationY * Math.PI / 180.);
        transform.rotateX(globalRotationX * Math.PI / 180.);
        transform.rotateZ(globalRotationZ * Math.PI / 180.);

        for (ButterflyConfig config : BUTTERFLY_CONFIG) {
            String id = config.id;
            DollywoodModel.Butterfly.Type type = config.type;
            float x = config.x;
            float y = config.y;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;

            DollywoodModel.Butterfly butterfly = new DollywoodModel.Butterfly(id, type, x, y, z, xRot, yRot, zRot, transform);
            butterflies.add(butterfly);
        }

        DollywoodModel.Butterfly[] butterfliesArr = new DollywoodModel.Butterfly[butterflies.size()];
        for (int i = 0; i < butterfliesArr.length; i++) {
            butterfliesArr[i] = butterflies.get(i);
        }

        return new DollywoodModel(applet, butterflies, butterfliesArr);
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
