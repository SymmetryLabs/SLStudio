package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXAutomationRecorder;

public class AutomationWindow extends CloseableWindow {

    private final LXAutomationRecorder automation;

    private final LX lx;

    protected final ParameterUI pui;

    public AutomationWindow (LX lx, LXAutomationRecorder automation) {
        super("Automation Window");
        this.lx = lx;
        this.automation = automation;

        this.pui = ParameterUI.getDefault(lx);
    }

    @Override
    protected void drawContents() {
        automation.armRecord.setValue(UI.checkbox("Armed", automation.armRecord.isOn()));

        UI.sameLine();
        if (automation.armRecord.isOn()) {
            boolean wasRunning = automation.isRunning();
            if (wasRunning) {
                UI.pushColor(UI.COLOR_BUTTON, UIConstants.RED);
                UI.pushColor(UI.COLOR_BUTTON_ACTIVE, UIConstants.RED);
                UI.pushColor(UI.COLOR_BUTTON_HOVERED, UIConstants.RED_HOVER);
            }
            if (UI.button("Record")) {
                automation.trigger();
            }
            if (wasRunning) {
                UI.popColor(3);
            }
        } else {
            boolean wasRunning = automation.isRunning();
            if (wasRunning) {
                UI.pushColor(UI.COLOR_BUTTON, UIConstants.GREEN);
                UI.pushColor(UI.COLOR_BUTTON_ACTIVE, UIConstants.GREEN);
                UI.pushColor(UI.COLOR_BUTTON_HOVERED, UIConstants.GREEN_HOVER);
            }
            if (UI.button("Play")) {
                automation.trigger();
            }
            if (wasRunning) {
                UI.popColor(3);
            }
        }

        UI.sameLine();
        if (UI.button("Stop")) {
            automation.reset();
        }

        if (automation.isRunning()) {
            if (automation.armRecord.isOn()) {
                UI.text("Recording: " + automation.getPlayheadString());
            } else {
                if (automation.isEmpty()) {
                    UI.text("No Clip Recorded");
                } else {
                    UI.text("Running: " + automation.getPlayheadString());
                }
           }
        } else {
            if (automation.isEmpty()) {
                UI.text("No Clip Recorded");
            } else {
                UI.text("Clip Duration: " + automation.getDurationString());
            }
        }

        UI.spacing(1, 5);
        automation.triggerVezer.setValue(UI.checkbox("Trigger Vezer Sequence", automation.triggerVezer.isOn()));

        pui.draw(automation.vezerIpAddress);
        pui.draw(automation.vezerOscPort);
        pui.draw(automation.vezerSequence);


    }
}
    