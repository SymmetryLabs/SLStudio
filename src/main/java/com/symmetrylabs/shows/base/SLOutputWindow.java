package com.symmetrylabs.shows.base;

import com.symmetrylabs.controllers.symmeTreeController.infrastructure.AllPortsPowerEnableMask;
import com.symmetrylabs.shows.oslo.OsloShow;
import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.mappings.SLSculptureControllerMapping;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.slstudio.ui.v2.*;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import java.io.IOException;
import java.util.Collection;

public class SLOutputWindow extends CloseableWindow {
    private final LX lx;
    private final SLModel model;
    private final SLShow show;
    private final ParameterUI pui;
    private final String[] featureIdBuffer = new String[32];
    private String modelID_filter = "";

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

        Collection<DiscoverableController> ccs = show.getSortedControllers();
        UI.text("%d controllers", ccs.size());

        if (UI.collapsibleSection("Edit all")) {
            editBrightness = UI.sliderFloat("Brightness", editBrightness, 0, 1);
            if (UI.button("Set")) {
                for (DiscoverableController cc : ccs) {
                    cc.brightness.setValue(editBrightness);
                }
            }
        }

        boolean expand = UI.button("expand all");
        UI.sameLine();
        boolean collapse = UI.button("collapse all");
        UI.text("alt-click any controller to send test pattern");
        UI.text("alt-shift-click any controller to momentarily blackout all outputs");

        boolean blackout = UI.button("blackout procedure");

        boolean dump = UI.button("dump metadata to file");

        boolean savePortPowerPreferencesToRAM = UI.button("Save port power to RAM"); // hmm.. should only have bottom two
        boolean writePortPowerPreferencesFromRAM = UI.button("Write port power from RAM"); // hmm.. should only have bottom two
        boolean savePortPowerPreferencesToDisk = UI.button("Save port power RAM to disk"); // all that's needed?
        boolean loadPortPowerPreferencesFromDisk = UI.button("Load port power from disk into RAM"); // all that's needed?

        boolean broadcastPortPowerOn = UI.button("broadcast turn on port power");

        modelID_filter = UI.inputText("filter by controllerId or modelId", modelID_filter);

        pui.draw(filterLessThanThreshhold);
        pui.draw(filterOnlyAboveAcceptableDarkCurrentThreshhold);

        if (savePortPowerPreferencesToRAM){
            if (show instanceof OsloShow){
                ((OsloShow) show).allPortsPowerEnableMask.loadControllerSetMaskStateTo_RAM(ccs);
            }
        }
        if (writePortPowerPreferencesFromRAM){
            if (show instanceof OsloShow){
                ((OsloShow) show).allPortsPowerEnableMask.RAM_ApplyMaskAllControllers(ccs);
            }
        }
        if (savePortPowerPreferencesToDisk){
            if (show instanceof OsloShow){
                try {
                    ((OsloShow) show).allPortsPowerEnableMask.saveToDisk();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (loadPortPowerPreferencesFromDisk){
            if (show instanceof OsloShow){
                ((OsloShow) show).allPortsPowerEnableMask = AllPortsPowerEnableMask.loadFromDisk();
            }
        }

        if (dump){
            System.out.println("[");
        }
        for (DiscoverableController dc : ccs) {
            if (expand) {
                UI.setNextTreeNodeOpen(true);
            } else if (collapse) {
                UI.setNextTreeNodeOpen(false);
            }
            if (dump){
                System.out.println( "\"" + dc.networkDevice.ipAddress.toString().split("/")[1] + "\"" + ",");
            }

            if (dc instanceof ControllerWithPowerFeedback){
                if (broadcastPortPowerOn){
                    ((ControllerWithPowerFeedback) dc).enableAllPorts();
                }
                if ( filterOnlyAboveAcceptableDarkCurrentThreshhold.isOn() && ((ControllerWithPowerFeedback) dc).allPortsLessThanThreshholdDuringBlackout(filterLessThanThreshhold.getValuei()) ){
                    continue;
                }
                if (blackout){
                    ((ControllerWithPowerFeedback) dc).enableBlackoutProcedure(true);
                    ((ControllerWithPowerFeedback) dc).setBlackoutThreshhold(show.globalBlackoutPowerThreshhold.getValuei());
                    ((ControllerWithPowerFeedback) dc).killByThreshHold();
                }
            }

            if (!modelID_filter.equals("")) {
                SLSculptureControllerMapping.PhysIdAssignment pia = SLShow.mapping.lookUpByControllerId(dc.humanID);
                if (pia != null){
                    String modelId = pia.modelId;
                    boolean modelIdMatch = modelId.contains(modelID_filter);
                    boolean ctrlIdMatch = pia.humanID != null && ((pia.humanID != null && pia.humanID.contains(modelID_filter)));
                    if (!(modelIdMatch || ctrlIdMatch )) {
                        continue;
                    }

                }
            }

            boolean mapped = SLShow.mapping.lookUpByControllerId(dc.humanID) != null;
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

            String displayName = SLShow.controllerInventory.getNameByMac(dc.networkDevice.deviceId);
//            String displayName = show.controllerInventory2.getNameByMac(dc.networkDevice.deviceId);
            UI.CollapseResult cr = UI.collapsibleSection(displayName, false);

            if (dc.getMacAddress() != null && UI.beginDragDropSource()) {
                UI.setDragDropPayload("SL.CubeMacAddress", dc.getMacAddress());
                UI.endDragDropSource();
            }

            UI.popColor(3);
            // TODO:: impliment this
            dc.momentaryAltTestOutput.setValue(UI.isItemClicked(true) && UI.isAltDown());
            dc.momentaryAltShiftTestBlackout.setValue(UI.isItemClicked(true) && UI.isAltDown() && UI.isShiftDown());


            if (cr.isOpen) {
                dc.newControllerID = UI.inputText("new ID " + dc.humanID, dc.newControllerID);
                if (UI.button("save " + dc.newControllerID)){
                    try {
                        show.controllerInventory2.indexController(dc.newControllerID, dc);
//                        show.controllerInventory2.validateNetwork();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // todo the mapping
//            UI.labelText("Status", mapped ? "mapped" : "unmapped");
                NetworkDevice nd = dc.networkDevice;
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

                ComponentUI componentUI = new ComponentUI(lx, dc, pui);
                if(dc instanceof ControllerWithPowerFeedback){
                    ControllerWithPowerFeedback pfc = ((ControllerWithPowerFeedback) dc);
                    UI.separator();
                    componentUI.blacklist(((ControllerWithPowerFeedback)dc).getPwrMaskParams());
                    if ( ((ControllerWithPowerFeedback) dc).getLastSample() != null ) {
                        for (int i = 0; i < 8; i++){
                            UI.intBox(Integer.toString(i), ((ControllerWithPowerFeedback) dc).getLastSample().analogSampleArray[i]);
                            UI.sameLine();
                            pui.draw(((ControllerWithPowerFeedback) dc).getPwrMaskParams()[i]);
                        }
//                        killSwitch[i] = UI.checkbox(Integer.toString(i), killSwitch[i]);
                        ((ControllerWithPowerFeedback) dc).writePortPowerMaskToController();
                        UI.separator();
                    }
                }
                componentUI.draw();
            }

        }
        if (dump){
            System.out.println("]");
        }
    }
}
