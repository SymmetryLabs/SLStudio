package com.symmetrylabs.shows.cubes;

import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.ui.v2.CloseableWindow;
import com.symmetrylabs.slstudio.ui.v2.ParameterUI;
import com.symmetrylabs.slstudio.ui.v2.UI;
import heronarts.lx.LX;
import com.symmetrylabs.slstudio.ApplicationState;
import java.util.Collection;

public class CubeOutputWindow extends CloseableWindow {
    private final LX lx;
    private final CubesShow show;
    private final String[] featureIdBuffer = new String[32];

    public CubeOutputWindow(LX lx, CubesShow show) {
        super("Cube output");
        this.lx = lx;
        this.show = show;
    }

    @Override
    protected void drawContents() {
        ParameterUI.draw(lx, ApplicationState.outputControl().enabled);
        ParameterUI.draw(lx, ApplicationState.outputControl().testBroadcast);
        ParameterUI.draw(lx, ApplicationState.outputControl().controllerResetModule.enabled);

        UI.separator();

        Collection<CubesController> ccs = show.getSortedControllers();
        UI.text("%d controllers", ccs.size());
        boolean expand = UI.button("expand all");
        UI.sameLine();
        boolean collapse = UI.button("collapse all");

        for (CubesController cc : ccs) {
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }
            UI.CollapseResult cr = UI.collapsibleSection(cc.id, false);
            if (cr.isOpen) {
                ParameterUI.draw(lx, cc.enabled);
                NetworkDevice nd = cc.networkDevice;
                if (nd == null) {
                    UI.text("(no network device)");
                } else {
                    String version = nd.versionId;
                    if (version.isEmpty()) {
                        version = String.format("%d*", nd.version.get());
                    }
                    UI.labelText("version", version);
                    UI.labelText("ip", nd.ipAddress.toString());
                    UI.labelText("product", nd.productId);
                    UI.labelText("device", nd.deviceId);
                    UI.labelText("features", String.join(",", nd.featureIds));
                }
            }
        }
    }
}
