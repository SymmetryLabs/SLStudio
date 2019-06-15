package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.ComponentUI;
import com.symmetrylabs.slstudio.ui.v2.ParameterUI;
import com.symmetrylabs.slstudio.ui.v2.UI;
import com.symmetrylabs.slstudio.ui.v2.UIConstants;
import heronarts.lx.LX;
import java.util.Collection;

public class CubeOutputWindow extends CloseableWindow {
    private final LX lx;
    private final CubesModel model;
    private final CubesShow show;
    private final ParameterUI pui;
    private final String[] featureIdBuffer = new String[32];

    private float editBrightness = 1.f;

    public CubeOutputWindow(LX lx, CubesShow show) {
        super("Cube output");
        this.lx = lx;
        this.show = show;
        this.model = (CubesModel) lx.model;
        this.pui = ParameterUI.getDefault(lx);
    }

    @Override
    protected void drawContents() {
        pui.draw(ApplicationState.outputControl().enabled);
        pui.draw(ApplicationState.outputControl().testBroadcast);
        pui.draw(ApplicationState.outputControl().controllerResetModule.enabled);

        UI.separator();

        Collection<CubesController> ccs = show.getSortedControllers();
        UI.text("%d controllers", ccs.size());

        if (UI.collapsibleSection("Edit all")) {
            editBrightness = UI.sliderFloat("Brightness", editBrightness, 0, 1);
            if (UI.button("Set")) {
                for (CubesController cc : ccs) {
                    cc.brightness.setValue(editBrightness);
                }
            }
        }

        boolean expand = UI.button("expand all");
        UI.sameLine();
        boolean collapse = UI.button("collapse all");
        UI.text("alt-click any controller to send test pattern");
        UI.text("alt-ctl-click any controller to send reset");

        for (CubesController cc : ccs) {
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }

            boolean mapped = model.mapping.lookUpByControllerId(cc.id) != null;
            if (mapped) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.BLUE_HOVER);
            } else {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.RED_HOVER);
            }
            UI.CollapseResult cr = UI.collapsibleSection(cc.id + " (" + cc.networkDevice.versionId + ")", false);
            if (cc.getMacAddress() != null && UI.beginDragDropSource()) {
                UI.setDragDropPayload("SL.CubeMacAddress", cc.getMacAddress());
                UI.endDragDropSource();
            }

            UI.popColor(3);
            cc.sendTestPattern = UI.isItemClicked(true) && UI.isAltDown();
            cc.sendReset = UI.isItemClicked(true) && UI.isAltDown() && UI.isCtrlDown();

            if (!cr.isOpen) {
                continue;
            }
            new ComponentUI(lx, cc, pui).draw();
            UI.labelText("Status", mapped ? "mapped" : "unmapped");
            NetworkDevice nd = cc.networkDevice;
            if (nd == null) {
                UI.text("(no network device)");
            } else {
                String version = nd.versionId;
                if (version.isEmpty()) {
                    version = String.format("%d*", nd.version.get());
                }
                UI.labelText("Version", version);
                UI.labelText("IP", nd.ipAddress.toString());
                UI.labelText("Product", nd.productId);
                UI.labelText("Device", nd.deviceId);
                UI.labelText("Features", String.join(",", nd.featureIds));
            }
        }
    }
}
