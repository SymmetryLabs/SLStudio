package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.shows.base.SLShow;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.SLController;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;

import java.util.Collection;

public class TwigShutOffWindow extends CloseableWindow {
    private final LX lx;
    private final SLModel model;
    private final SLShow show;
    private final ParameterUI pui;
    private final String[] featureIdBuffer = new String[32];
    private float editBrightness = 1.f;
    private float twigs = 0;



    public final BooleanParameter twig1off = (BooleanParameter)
        new BooleanParameter("1", false)
        .setDescription("Twig 1 On/Off");

    public final BooleanParameter twig2off = (BooleanParameter)
        new BooleanParameter("2", false)
            .setDescription("Twig 2 On/Off");

    public final BooleanParameter twig3off = (BooleanParameter)
        new BooleanParameter("3", false)
            .setDescription("Twig 3 On/Off");

    public final BooleanParameter twig4off = (BooleanParameter)
        new BooleanParameter("4", false)
            .setDescription("Twig 4 On/Off");

    public final BooleanParameter twig5off = (BooleanParameter)
        new BooleanParameter("5", false)
            .setDescription("Twig 1 On/Off");

    public final BooleanParameter twig6off = (BooleanParameter)
        new BooleanParameter("6", false)
            .setDescription("Twig 6 On/Off");

    public final BooleanParameter twig7off = (BooleanParameter)
        new BooleanParameter("7", false)
            .setDescription("Twig 7 On/Off");

    public final BooleanParameter twig8off = (BooleanParameter)
        new BooleanParameter("8", false)
            .setDescription("Twig 8 On/Off");



    public TwigShutOffWindow(LX lx, SLShow show) {
        super("Twig ShutOff");
        this.lx = lx;
        this.show = show;
        this.model = (SLModel) lx.model;
        this.pui = ParameterUI.getDefault(lx);
    }

    @Override
    protected void drawContents() {
//        pui.draw(ApplicationState.outputControl().enabled);
//        pui.draw(ApplicationState.outputControl().testBroadcast);
//        pui.draw(ApplicationState.outputControl().controllerResetModule.enabled);


        Collection<SLController> ccs = show.getSortedControllers();
        UI.text("%d branches", ccs.size());

//        if (UI.collapsibleSection("Edit all")) {
//            editBrightness = UI.sliderFloat("Brightness", editBrightness, 0, 1);
//            if (UI.button("Set")) {
//                for (SLController cc : ccs) {
//                    cc.brightness.setValue(editBrightness);
//                }
//            }
//        }


        twigs = UI.sliderFloat("twig to shut off", twigs, 1, 1000);
        boolean expand = UI.button("expand all");
        UI.sameLine();
        boolean collapse = UI.button("collapse all");
//        UI.text("alt-click any controller to send test pattern");

//        boolean dump = UI.button("dump metadata to file");

//        if (dump){
//            System.out.println("[");
//        }
        for (SLController cc : ccs) {
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }
//            if (dump){
//                System.out.println( "\"" + cc.networkDevice.ipAddress.toString().split("/")[1] + "\"" + ",");
//            }

//            boolean mapped = model.mapping.lookUpByControllerId(cc.id) != null;
            if (false) {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.BLUE_HOVER);
            } else {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.RED_HOVER);
            }
            NetworkDevice nd = cc.networkDevice;

            UI.CollapseResult cr = UI.collapsibleSection(nd.ipAddress.toString(), false);
            if (nd.ipAddress.toString() != null && UI.beginDragDropSource()) {
                UI.setDragDropPayload("SL.CubeMacAddress", nd.ipAddress.toString());
                UI.endDragDropSource();
            }

            UI.popColor(3);
            // TODO:: impliment this
//            cc.sendTestPattern = UI.isItemClicked(true) && UI.isAltDown();

            if (!cr.isOpen) {
                continue;
            }
//            new ComponentUI(lx, cc, pui).draw();
            // todo the mapping
//            UI.labelText("Status", mapped ? "mapped" : "unmapped");
//            NetworkDevice nd = cc.networkDevice;
            if (nd == null) {
                UI.text("(no network device)");
            } else {
                String version = nd.versionId;
                if (version.isEmpty()) {
                    version = String.format("%d*", nd.version.get());
                }
//

                pui.draw(twig1off);
                if (twig1off.isOn()){

                }

                pui.draw(twig2off);
                if (twig2off.isOn()){

                }
                pui.draw(twig3off);
                if (twig1off.isOn()){

                }
                pui.draw(twig4off);
                if (twig1off.isOn()){

                }
                pui.draw(twig5off);
                if (twig1off.isOn()){

                }
                pui.draw(twig6off);
                if (twig1off.isOn()){

                }
                pui.draw(twig7off);
                if (twig1off.isOn()){

                }
                pui.draw(twig8off);
                if (twig1off.isOn()){

                }
//                UI.labelText("Version", version);
//                UI.labelText("IP", nd.ipAddress.toString());
//                UI.labelText("Product", nd.productId);
//                UI.labelText("Device", nd.deviceId);
//                UI.labelText("Features", String.join(",", nd.featureIds));
            }
        }
//        if (dump){
//            System.out.println("]");
//        }
    }
}

