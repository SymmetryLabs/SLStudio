package com.symmetrylabs.layouts.crystals;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.util.MathConstants.PI;

public class CrystalLayout implements Layout {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 90;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

//    @Override
    public SLModel buildModel() {
        CrystalLayout layout = new CrystalLayout();
        float PANEL_LENGTH = 44.0f;

        // global transforms
        LXTransform transform = new LXTransform();
        transform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        transform.rotateX(globalRotationX * PI / 180.);
        transform.rotateY(globalRotationY * PI / 180.);
        transform.rotateZ(globalRotationZ * PI / 180.);

        List<Panel> panels = new ArrayList<>();
//        for (int iPanel = 0; iPanel < 9; iPanel++) {
//            SubPanel subpanel = new SubPanel("Panel_" + iPanel+1, t.x(), t.y(), t.z(), 0, 0, 0, stringLengths,  t);
//            subpanels.add(subpanel);
//
//            for (LXPoint point : subpanel.points){
//
//                points.add(point);
//                this.points.add(point);
//
//            }
//            t.translate(SUBPANEL_LENGTH, 0, 0);
//            if (iPanel % 3 == 2) {
//                t.translate(0, SUBPANEL_LENGTH, 0);
//                t.rotateX(180);
//
//            }

            //subpanels.add(subpanel);
//        }
        panels.add(createPanel("panel_1", 0, 0, 0, 0, 0, 0, transform));
        panels.add(createPanel("panel_2", PANEL_LENGTH, 0, 0, 0, 0, 0, transform));
        panels.add(createPanel("panel_3", PANEL_LENGTH*2, 0, 0, 0, 0, 0, transform));
        panels.add(createPanel("panel_4", PANEL_LENGTH*1, PANEL_LENGTH, 0, 0, 180, 0, transform));
        panels.add(createPanel("panel_5", PANEL_LENGTH*2, PANEL_LENGTH, 0, 0, 180, 0, transform));
        panels.add(createPanel("panel_6", PANEL_LENGTH*3, PANEL_LENGTH, 0, 0, 180, 0, transform));
        panels.add(createPanel("panel_7", 0, PANEL_LENGTH*2, 0, 0, 0, 0, transform));
        panels.add(createPanel("panel_8", PANEL_LENGTH, PANEL_LENGTH*2, 0, 0, 0, 0, transform));
        panels.add(createPanel("panel_9", PANEL_LENGTH*2, PANEL_LENGTH*2, 0, 0, 0, 0, transform));
        return new CrystalModel(panels);

    }
    private static Panel createPanel(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform transform) {

        return new Panel(id, x, y, z, rx, ry, rz, transform);

    }
}
