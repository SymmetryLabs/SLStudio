package com.symmetrylabs.shows.cubes;

import static com.symmetrylabs.shows.cubes.CubesModel.Cube;
import static com.symmetrylabs.shows.cubes.CubesModel.DoubleControllerCube;

import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.UI;
import com.symmetrylabs.slstudio.ui.v2.UIConstants;
import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;
import java.util.List;


public class CubeEditor extends CloseableWindow {
    protected final LX lx;
    protected final CubesModel model;

    public CubeEditor(LX lx, CubesModel model) {
        super("Cube editor");
        this.lx = lx;
        this.model = model;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(300, 50, UIConstants.DEFAULT_WINDOW_WIDTH, 500);
    }

    @Override
    protected void drawContents() {
        List<Cube> cubes = model.getCubes();
        UI.text("%d cubes", cubes.size());

        boolean expand = UI.button("Expand all");
        UI.sameLine();
        boolean collapse = UI.button("Collapse all");

        boolean anyUpdated = false;
        for (int i = 0; i < cubes.size(); i++) {
            Cube c = cubes.get(i);
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }
            UI.CollapseResult cr = UI.collapsibleSection(c.modelId, false);
            if (!cr.isOpen) {
                continue;
            }
            UI.labelText("type", c.type.toString());

            UI.inputFloat3("position##" + i, new float[] {c.x, c.y, c.z}, UI.INPUT_TEXT_FLAG_READ_ONLY);

            boolean updated = false;
            if (c instanceof DoubleControllerCube) {
                DoubleControllerCube dcc = (DoubleControllerCube) c;
                UI.beginTable(2, "cubeIds");
                String idA = UI.inputText(String.format("A##%d", i), dcc.controllerIdA);
                UI.nextCell();
                String idB = UI.inputText(String.format("B##%d", i), dcc.controllerIdB);
                if (!idA.equals(dcc.controllerIdA) || !idB.equals(dcc.controllerIdB)) {
                    dcc.controllerId = idA;
                    dcc.controllerIdA = idA;
                    dcc.controllerIdB = idB;
                    updated = true;
                }
                UI.endTable();
            } else {
                String id = UI.inputText(String.format("id##%d", i), c.controllerId);
                if (!id.equals(c.controllerId)) {
                    c.controllerId = id;
                    updated = true;
                }
            }

            if (updated) {
                c.updatePoints(new LXTransform());
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            model.update(true, true);
        }
    }
}
