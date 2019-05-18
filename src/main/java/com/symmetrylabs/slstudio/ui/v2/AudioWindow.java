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

public class AudioWindow extends CloseableWindow {
    private final LX lx;
    private final ParameterUI pui;

    public AudioWindow(LX lx) {
        super("Audio");
        this.lx = lx;
        this.pui = ParameterUI.getDefault(lx);
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UIConstants.DEFAULT_WINDOW_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        UI.CollapseResult section;
        LXAudioEngine audio = lx.engine.audio;
        pui.draw(audio.enabled);

        UI.sameLine();
        if (UI.checkbox("InputMode", audio.mode.getEnum() == LXAudioEngine.Mode.INPUT)) {
            audio.mode.setValue(LXAudioEngine.Mode.INPUT);
        }
        UI.sameLine();
        if (UI.checkbox("OutputMode", audio.mode.getEnum() == LXAudioEngine.Mode.OUTPUT)) {
            audio.mode.setValue(LXAudioEngine.Mode.OUTPUT);
        }

        float[] hist = new float[audio.meter.bands.length];
        for (int i = 0; i < hist.length; i++) {
            hist[i] = audio.meter.bands[i].getNormalizedf();
        }
        UI.histogram("EQ", hist, 0, 1, 80);

        section = UI.collapsibleSection("EQ options", false);
        if (section.isOpen) {
            pui.draw(audio.meter.gain);
            pui.draw(audio.meter.slope);
            pui.draw(audio.meter.range);
            pui.draw(audio.meter.attack);
            pui.draw(audio.meter.release);
        }

        section = UI.collapsibleSection("Input", false);
        if (section.isOpen) {
            pui.draw(audio.input.device);
        }

        section = UI.collapsibleSection("Output", false);
        if (section.isOpen) {
            pui.draw(lx.engine.audio.output.play);
            pui.draw(lx.engine.audio.output.looping);
            pui.draw(lx.engine.audio.output.trigger);

            if (UI.button("Pick")) {
                FileDialog.open(
                    lx, "Open WAV file", file -> {
                        Path path = new File(Utils.sketchPath()).toPath()
                            .relativize(file.toPath());
                        lx.engine.audio.output.file.setValue(path.toString());
                    });
            }
            UI.sameLine();
            String label = lx.engine.audio.output.file.getString();
            if (label == null) {
                label = "(no file selected)";
            } else if (!lx.engine.audio.output.isPlayable()) {
                label = "unable to play file";
            }
            UI.text(label);
        }
    }
}
