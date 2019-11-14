package com.symmetrylabs.shows.base;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
import com.symmetrylabs.slstudio.ui.v2.*;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.Collection;

public class SLOutputWindow extends CloseableWindow {
    private final LX lx;
    private final SLModel model;
    private final SLShow show;
    private final ParameterUI pui;
    private final String[] featureIdBuffer = new String[32];

    private float editBrightness = 1.f;

    private BooleanParameter onlyUnmapped = new BooleanParameter("display unmapped only", false);
    private DiscreteParameter filterLessThanThreshhold = new DiscreteParameter("acceptable dark current draw threshhold", 200, 0, 4095);
    private BooleanParameter filterOnlyAboveAcceptableDarkCurrentThreshhold = new BooleanParameter("filter violations dark current", false);

    public SLOutputWindow(LX lx, SLShow show) {
        super("SL Outputs");
        this.lx = lx;
        this.show = show;
        this.model = (SLModel) lx.model;
        this.pui = ParameterUI.getDefault(lx);
    }

    @Override
    protected void drawContents() {
        pui.draw(ApplicationState.outputControl().enabled);
        pui.draw(ApplicationState.outputControl().testBroadcast);
        pui.draw(ApplicationState.outputControl().controllerResetModule.enabled);

        pui.draw(show.globalBlackoutPowerThreshhold);

        pui.draw(onlyUnmapped);

        UI.separator();

        Collection<AbstractSLControllerBase> ccs = show.getSortedControllers();
        UI.text("%d controllers", ccs.size());

        if (UI.collapsibleSection("Edit all")) {
            editBrightness = UI.sliderFloat("Brightness", editBrightness, 0, 1);
            if (UI.button("Set")) {
                for (AbstractSLControllerBase cc : ccs) {
                    cc.brightness.setValue(editBrightness);
                }
            }
        }

        boolean expand = UI.button("expand all");
        UI.sameLine();
        boolean collapse = UI.button("collapse all");
        UI.text("alt-click any controller to send test pattern");

        boolean blackout = UI.button("blackout procedure");

        boolean dump = UI.button("dump metadata to file");

        boolean broadcastPortPowerOn = UI.button("broadcast turn on port power");

        pui.draw(filterLessThanThreshhold);
        pui.draw(filterOnlyAboveAcceptableDarkCurrentThreshhold);

        if (dump){
            System.out.println("[");
        }
        for (AbstractSLControllerBase cc : ccs) {
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }
            if (dump){
                System.out.println( "\"" + cc.networkDevice.ipAddress.toString().split("/")[1] + "\"" + ",");
            }

            if (cc instanceof ControllerWithPowerFeedback){
                if ( filterOnlyAboveAcceptableDarkCurrentThreshhold.isOn() && ((ControllerWithPowerFeedback) cc).allPortsLessThanThreshholdDuringBlackout(filterLessThanThreshhold.getValuei()) ){
                    continue;
                }
                if (blackout){
                    ((ControllerWithPowerFeedback) cc).enableBlackoutProcedure(true);
                    ((ControllerWithPowerFeedback) cc).setBlackoutThreshhold(show.globalBlackoutPowerThreshhold.getValuei());
                    ((ControllerWithPowerFeedback) cc).killByThreshHold();
                }
            }

            boolean mapped = SLShow.mapping.lookUpByControllerId(cc.humanID) != null;
            if (mapped) {
                if (onlyUnmapped.isOn()){
                    continue;
                }
                UI.pushColor(UI.COLOR_HEADER, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.BLUE);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.BLUE_HOVER);
            } else {
                UI.pushColor(UI.COLOR_HEADER, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_ACTIVE, UIConstants.RED);
                UI.pushColor(UI.COLOR_HEADER_HOVERED, UIConstants.RED_HOVER);
            }
            UI.CollapseResult cr = UI.collapsibleSection(cc.humanID, false);
            if (cc.getMacAddress() != null && UI.beginDragDropSource()) {
                UI.setDragDropPayload("SL.CubeMacAddress", cc.getMacAddress());
                UI.endDragDropSource();
            }

            UI.popColor(3);
            // TODO:: impliment this
            cc.momentaryAltTestOutput.setValue(UI.isItemClicked(true) && UI.isAltDown());

            if (cr.isOpen) {
                new ComponentUI(lx, cc, pui).draw();
                // todo the mapping
//            UI.labelText("Status", mapped ? "mapped" : "unmapped");
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

                if(cc instanceof ControllerWithPowerFeedback){
                    if ( ((ControllerWithPowerFeedback) cc).getLastSample() != null ) {
                        for (int i = 0; i < 8; i++){
                            UI.intBox(Integer.toString(i), ((ControllerWithPowerFeedback) cc).getLastSample().analogSampleArray[i]);
                        }
//                        UI.sameLine();
//                        killSwitch[i] = UI.checkbox(Integer.toString(i), killSwitch[i]);
                        ((ControllerWithPowerFeedback) cc).killPortPower();
                    }
                }
            }

        }
        if (dump){
            System.out.println("]");
        }
    }
}
