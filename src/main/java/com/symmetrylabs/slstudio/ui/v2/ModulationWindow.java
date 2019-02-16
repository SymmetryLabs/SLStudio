package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.util.Utils;
import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.warp.LXWarp;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.MultiStageEnvelope;

public class ModulationWindow extends CloseableWindow {
    private final LX lx;

    public ModulationWindow(LX lx) {
        super("Modulation");
        this.lx = lx;
    }

    @Override
    protected void drawContents() {
        if (UI.button("+Env")) {
            WindowManager.runSafelyWithEngine(lx, () -> lx.engine.modulation.addModulator(new MultiStageEnvelope()));
        }
        for (LXModulator modulator : lx.engine.modulation.getModulators()) {
            if (modulator instanceof MultiStageEnvelope) {
                envelope((MultiStageEnvelope) modulator);
            }
        }
    }

    protected void envelope(MultiStageEnvelope env) {
        if (env.running.isOn()) {
            UI.pushColor(UI.COLOR_HEADER, UI.BLUE);
            UI.pushColor(UI.COLOR_HEADER_ACTIVE, UI.BLUE);
            UI.pushColor(UI.COLOR_HEADER_HOVERED, UI.BLUE_HOVER);
        }
        UI.CollapseResult cr = UI.collapsibleSection(env.getLabel(), true);
        if (env.running.isOn()) {
            UI.popColor(3);
        }
        if (cr.shouldRemove) {
            WindowManager.runSafelyWithEngine(lx, () -> lx.engine.modulation.removeModulator(env));
        }
        if (UI.isItemClicked() && UI.isAltDown()) {
            lx.engine.addTask(() -> env.trigger.setValue(true));
        }
        if (!cr.isOpen) {
            return;
        }
    }
}
