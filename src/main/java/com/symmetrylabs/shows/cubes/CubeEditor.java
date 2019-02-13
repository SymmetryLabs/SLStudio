package com.symmetrylabs.shows.cubes;

import static com.symmetrylabs.shows.cubes.CubesModel.Cube;
import static com.symmetrylabs.shows.cubes.CubesModel.DoubleControllerCube;

import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.UI;
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
        UI.setNextWindowDefaults(300, 50, UI.DEFAULT_WIDTH, 500);
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
            UI.CollapseResult cr = UI.collapsibleSection(String.format("Cube %d", i), false);
            if (!cr.isOpen) {
                continue;
            }
            UI.labelText("type", c.type.toString());

            boolean updated = false;
            if (c instanceof DoubleControllerCube) {
                DoubleControllerCube dcc = (DoubleControllerCube) c;
                UI.columnsStart(2, "cubeIds");
                String idA = UI.inputText(String.format("A##%d", i), dcc.idA);
                UI.nextColumn();
                String idB = UI.inputText(String.format("B##%d", i), dcc.idB);
                if (!idA.equals(dcc.idA) || !idB.equals(dcc.idB)) {
                    dcc.id = idA;
                    dcc.idA = idA;
                    dcc.idB = idB;
                    updated = true;
                }
            } else {
                String id = UI.inputText(String.format("id##%d", i), c.id);
                if (!id.equals(c.id)) {
                    c.id = id;
                    updated = true;
                }
            }
            UI.columnsStart(3, "cubeEditor");
            float x = UI.floatBox(String.format("x##%d", i), c.x);
            if (x != c.x) {
                c.x = x;
                updated = true;
            }
            UI.nextColumn();
            float y = UI.floatBox(String.format("y##%d", i), c.y);
            if (y != c.y) {
                c.y = y;
                updated = true;
            }
            UI.nextColumn();
            float z = UI.floatBox(String.format("z##%d", i), c.z);
            if (z != c.z) {
                c.z = z;
                updated = true;
            }
            UI.nextColumn();
            UI.columnsEnd();

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
