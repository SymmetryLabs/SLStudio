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
    protected String idFilter = "";
    protected String macFilter = "";

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

        UI.sameLine();
        if (UI.button("Clear filters")) {
            idFilter = "";
            macFilter = "";
        }

        idFilter = UI.inputText("ID filter", idFilter);
        macFilter = UI.inputText("MAC filter", macFilter);

        UI.beginChild("cubeList", true, 0);
        int i = 0;
        CubeInventory.PhysicalCube toRemove = null;
        for (CubeInventory.PhysicalCube pc : inventory.allCubes) {
            /* cubes with null ID/addr on A are always displayed */
            boolean match =
                ((pc.idA == null || pc.idA.contains(idFilter)) || (pc.idB != null && pc.idB.contains(idFilter))) &&
                ((pc.addrA == null || pc.addrA.contains(macFilter)) || (pc.addrB != null && pc.addrB.contains(macFilter)));
            if (!match) {
                continue;
            }

            UI.CollapseResult cr = UI.collapsibleSection(
                (pc.idA == null ? "" : pc.idA) + "###" + pc.hashCode(), false);
            if (pc.getPhysicalId() != null && UI.beginDragDropSource()) {
                UI.setDragDropPayload("SL.CubePhysId", pc.getPhysicalId());
                UI.endDragDropSource();
            }
            if (!cr.isOpen) {
                continue;
            }

            pc.idA = UI.inputText("id A##" + i, pc.idA == null ? "" : pc.idA);
            pc.addrA = UI.inputText("addr A##" + i, pc.addrA == null ? "" : pc.addrA);
            if (UI.beginDragDropTarget()) {
                String addr = UI.acceptDragDropPayload("SL.CubeMacAddress", String.class);
                if (addr != null) {
                    pc.addrA = addr;
                }
            }
            pc.idB = UI.inputText("id B##" + i, pc.idB == null ? "" : pc.idB);
            pc.addrB = UI.inputText("addr B##" + i, pc.addrB == null ? "" : pc.addrB);
            if (UI.beginDragDropTarget()) {
                String addr = UI.acceptDragDropPayload("SL.CubeMacAddress", String.class);
                if (addr != null) {
                    pc.addrB = addr;
                }
            }

            if (pc.idA.length() == 0) pc.idA = null;
            if (pc.addrA.length() == 0) pc.addrA = null;
            if (pc.idB.length() == 0) pc.idB = null;
            if (pc.addrB.length() == 0) pc.addrB = null;

            if (pc.imported) {
                UI.textWrapped("(imported from the old physid table)");
            }
            if (UI.button("Delete")) {
                toRemove = pc;
            }
            i++;
            UI.spacing(10, 10);
        }

        if (toRemove != null) {
            inventory.allCubes.remove(toRemove);
            inventory.rebuild();
        }

        UI.endChild();
    }
}
