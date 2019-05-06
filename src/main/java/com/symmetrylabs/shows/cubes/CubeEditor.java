package com.symmetrylabs.shows.cubes;

import static com.symmetrylabs.shows.cubes.CubesModel.Cube;
import static com.symmetrylabs.shows.cubes.CubesModel.DoubleControllerCube;

import com.symmetrylabs.slstudio.output.CubeModelControllerMapping;
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

            CubeModelControllerMapping.PhysIdAssignment pia = model.controllers.lookUpModel(c.modelId);
            String oldPhysId = pia == null ? "" : pia.physicalId;
            String newPhysId = UI.inputText(String.format("physid##%d", i), oldPhysId);
            if (!oldPhysId.equals(newPhysId)) {
                if (pia != null) {
                    pia.physicalId = newPhysId;
                } else {
                    model.controllers.setControllerAssignment(c.modelId, newPhysId);
                }
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            model.update(true, true);
        }
    }
}
