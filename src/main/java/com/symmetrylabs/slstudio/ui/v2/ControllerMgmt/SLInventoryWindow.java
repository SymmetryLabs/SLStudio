package com.symmetrylabs.slstudio.ui.v2.ControllerMgmt;

import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.FontLoader;
import com.symmetrylabs.slstudio.ui.v2.UI;
import com.symmetrylabs.util.hardware.ControllerMetadata;
import com.symmetrylabs.util.hardware.CubeInventory;
import com.symmetrylabs.util.hardware.SLControllerInventory;
import heronarts.lx.LX;

import javax.naming.ldap.Control;
import java.util.Iterator;


public class SLInventoryWindow extends CloseableWindow {
    protected final LX lx;
    protected final SLControllerInventory inventory;
    protected String idFilter = "";
    protected String macFilter = "";

    public SLInventoryWindow(LX lx, SLControllerInventory inventory) {
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
            if (UI.collapsibleSection("Inventory file errors")) {
                while (errIter.hasNext()) {
                    UI.textWrapped(errIter.next().toString());
                }
                UI.spacing(10, 10);
            }
        }

        UI.pushFont(FontLoader.DEFAULT_FONT_L);
        UI.text(String.format("Inventory: %d controllers", inventory.allControllers.size()));
        UI.popFont();

        if (UI.button("Add")) {
            ControllerMetadata pc = new ControllerMetadata();
            inventory.allControllers.add(pc);
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

        UI.beginChild("controller list", true, 0);
        int i = 0;
        ControllerMetadata toRemove = null;
        for (ControllerMetadata meta : inventory.allControllers) {
            /* cubes with null ID/addr on A are always displayed */
            boolean match =
                (meta.getHumanID() == null || meta.getHumanID().contains(idFilter)) &&
                (meta.getMacAddr() == null || meta.getMacAddr().contains(macFilter));
            if (!match) {
                continue;
            }

            String humanID = meta.getHumanID();
            String macAddr = meta.getMacAddr();
            UI.CollapseResult cr = UI.collapsibleSection(
                (humanID == null ? "" : humanID) + "###" + meta.hashCode(), false);
            if (humanID != null && UI.beginDragDropSource()) {
                UI.setDragDropPayload("SL.ControllerHumanID", humanID);
                UI.endDragDropSource();
            }
            if (!cr.isOpen) {
                continue;
            }

            humanID = UI.inputText("humanID A##" + i, humanID == null ? "" : humanID);
            macAddr = UI.inputText("addr A##" + i, macAddr == null ? "" : macAddr);
            if (UI.beginDragDropTarget()) {
                String addr = UI.acceptDragDropPayload("SL.CubeMacAddress", String.class);
                if (addr != null) {
                    macAddr = addr;
                }
            }
            if (humanID.length() == 0) humanID = null;
            if (macAddr.length() == 0) macAddr = null;

            if (UI.button("Delete")) {
                toRemove = meta;
            }
            i++;
            UI.spacing(10, 10);
        }

        if (toRemove != null) {
            inventory.allControllers.remove(toRemove);
            inventory.rebuild();
        }

        UI.endChild();
    }
}
