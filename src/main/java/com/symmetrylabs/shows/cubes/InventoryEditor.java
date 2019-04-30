package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.FontLoader;
import com.symmetrylabs.slstudio.ui.v2.UI;
import com.symmetrylabs.slstudio.ui.v2.UIConstants;
import com.symmetrylabs.util.CubeInventory;
import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;
import java.util.Iterator;
import java.util.List;


public class InventoryEditor extends CloseableWindow {
    protected final LX lx;
    protected final CubeInventory inventory;

    public InventoryEditor(LX lx, CubeInventory inventory) {
        super("Inventory editor");
        this.lx = lx;
        this.inventory = inventory;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(300, 50, 400, 800);
    }

    @Override
    protected void drawContents() {
        Iterator<CharSequence> errIter = inventory.getErrors();
        if (errIter.hasNext()) {
            UI.pushFont(FontLoader.DEFAULT_FONT_L);
            UI.text("Inventory file errors");
            UI.popFont();
            while (errIter.hasNext()) {
                UI.textWrapped(errIter.next().toString());
            }
            UI.spacing(10, 10);
        }

        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        UI.text("Inventory");
        UI.popFont();

        if (UI.button("Add")) {
            CubeInventory.PhysicalCube pc = new CubeInventory.PhysicalCube();
            inventory.allCubes.add(pc);
        }
        if (UI.isItemHovered()) {
            UI.beginTooltip();
            UI.text("Add a cube to the inventory. You must hit Update after you fill in the details for the new cube.");
            UI.endTooltip();
        }

        UI.sameLine();
        if (UI.button("Update")) {
            inventory.rebuild();
        }
        if (UI.isItemHovered()) {
            UI.beginTooltip();
            UI.text("Update the inventory in this session and reloads the current model, but does not save changes to disk.");
            UI.endTooltip();
        }

        UI.sameLine();
        if (UI.button("Save")) {
            inventory.rebuild();
            if (!inventory.save()) {
                UI.openPopup("saveFailed");
            }
        }
        if (UI.isItemHovered()) {
            UI.beginTooltip();
            UI.text("Update the inventory in this session and saves the results to disk.");
            UI.endTooltip();
        }
        if (UI.beginPopup("saveFailed", true)) {
            UI.text("Failed to save cube inventory, check logs for details");
            if (UI.button("Close")) {
                UI.closePopup();
            }
            UI.endPopup();
        }

        UI.beginChild("cubeList", true, 0);
        int i = 0;
        for (CubeInventory.PhysicalCube pc : inventory.allCubes) {
            UI.separator();
            pc.idA = UI.inputText("id A##" + i, pc.idA);
            pc.addrA = UI.inputText("addr A##" + i, pc.addrA);
            pc.idB = UI.inputText("id B##" + i, pc.idB == null ? "" : pc.idB);
            if (pc.idB.length() == 0) pc.idB = null;
            pc.addrB = UI.inputText("addr B##" + i, pc.addrB == null ? "" : pc.addrB);
            if (pc.addrB.length() == 0) pc.addrB = null;
            if (pc.imported) {
                UI.textWrapped("(imported from the old physid table)");
            }
            i++;
            UI.spacing(10, 10);
        }
        UI.endChild();
    }
}
