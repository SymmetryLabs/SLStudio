package com.symmetrylabs.slstudio.performance;

import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import javax.swing.*;

public class UIStartPerformanceMode extends UICollapsibleSection {

    private final UI ui;
    private final PerformanceManager pm;
    private final UIButton button;
    private static int h = 43;

    private boolean inPerformanceMode() {
        return pm.performanceModeInitialized.getValueb();
    }

    public UIStartPerformanceMode(UI ui, PerformanceManager pm, float x, float y, float w) {
        super(ui, x, y, w, h);
        this.ui = ui;
        this.pm = pm;

        setTitle("Performance Mode");

        int border = 3;
        button = new UIButton(0, 0, w - 8, 20) {
            public void onToggle(boolean on) {
                if (!on) {
                    return;
                }

                String destinationMode = inPerformanceMode() ? "Composition" : "Performance";
                String title = String.format("Convert to %s Mode?", destinationMode);
                String message = String.format("Do you want to convert this project to %s Mode? Once you do, there's no going back.", destinationMode);


                int response = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    if (inPerformanceMode()) {
                        pm.teardownPerformanceMode();
                    } else {
                        pm.initializePerformanceMode();

                    }
                }
            }
        };
        pm.performanceModeInitialized.addListener(new LXParameterListener() {
            @Override
            public void onParameterChanged(LXParameter lxParameter) {
                setButtonLabel();
            }
        });
        setButtonLabel();
        button.setMomentary(true);
        button.addToContainer(this);
    }

    public void setButtonLabel() {
        String label = inPerformanceMode() ? "Convert to Composition Mode" : "Convert to Performance Mode";
        button.setLabel(label);
    }
}
