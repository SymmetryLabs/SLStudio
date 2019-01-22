package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.UI;
import static com.symmetrylabs.shows.cubes.CubesModel.Cube;
import heronarts.lx.transform.LXTransform;

public class CubeEditor extends CloseableWindow {
    protected final CubesModel model;

    public CubeEditor(CubesModel model) {
        super("Cube editor");
        this.model = model;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(300, 50, UI.DEFAULT_WIDTH, 500);
    }

    @Override
    protected void drawContents() {
        int i = 0;
        boolean anyUpdated = false;
        for (Cube c : model.getCubes()) {
            if (i != 0) {
                UI.separator();
            }
            i++;

            boolean updated = false;
            String id = UI.inputText("id", c.id);
            if (!id.equals(c.id)) {
                c.id = id;
                updated = true;
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
