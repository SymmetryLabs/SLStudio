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


public class MappingEditor extends CloseableWindow {
    protected final LX lx;
    protected final CubesModel model;

    public MappingEditor(LX lx, CubesModel model) {
        super("Mapping");
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

        UI.textWrapped(
            "Output should update as soon as you hit enter, but changes will not be saved to disk unless you press Save.");

        boolean expand = UI.button("Expand all");
        UI.sameLine();
        boolean collapse = UI.button("Collapse all");

        UI.sameLine();
        if (UI.button("Save")) {
            if (!model.mapping.save()) {
                UI.openPopup("saveFailed");
            }
        }
        if (UI.isItemHovered()) {
            UI.beginTooltip();
            UI.text("Saves the updated mapping to disk.");
            UI.endTooltip();
        }
        if (UI.beginPopup("saveFailed", true)) {
            UI.text("Failed to save mapping, check logs for details");
            if (UI.button("Close")) {
                UI.closePopup();
            }
            UI.endPopup();
        }

        boolean anyUpdated = false;
        for (int i = 0; i < cubes.size(); i++) {
            Cube c = cubes.get(i);
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }
            UI.CollapseResult cr = UI.collapsibleSection(c.modelId, false);
            if (UI.beginDragDropTarget()) {
                String physId = UI.acceptDragDropPayload("SL.CubePhysId", String.class);
                if (physId != null) {
                    model.mapping.setControllerAssignment(c.modelId, physId);
                    anyUpdated = true;
                }
            }

            if (!cr.isOpen) {
                continue;
            }
            UI.labelText("type", c.type.toString());

            UI.inputFloat3("position##" + i, new float[] {c.x, c.y, c.z}, UI.INPUT_TEXT_FLAG_READ_ONLY);

            CubeModelControllerMapping.PhysIdAssignment pia = model.mapping.lookUpModel(c.modelId);
            String oldPhysId = pia == null ? "" : pia.physicalId;
            String newPhysId = UI.inputText(String.format("physid##%d", i), oldPhysId);
            if (!oldPhysId.equals(newPhysId)) {
                model.mapping.setControllerAssignment(c.modelId, newPhysId);
                anyUpdated = true;
            }
        }

        if (anyUpdated) {
            model.update(true, true);
        }
    }
}
