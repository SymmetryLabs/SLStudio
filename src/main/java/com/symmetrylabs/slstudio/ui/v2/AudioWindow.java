package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXChannel;
import heronarts.lx.LXEffect;
import heronarts.lx.LXPattern;
import heronarts.lx.warp.LXWarp;
import java.util.ArrayList;
import java.util.List;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.awt.EventQueue;
import com.symmetrylabs.util.Utils;
import java.nio.file.Path;

public class AudioWindow extends CloseableWindow {
    private final LX lx;

    public AudioWindow(LX lx) {
        super("Audio");
        this.lx = lx;
    }

    @Override
    protected void windowSetup() {
        UI.setNextWindowDefaults(25, 500, UI.DEFAULT_WIDTH, 300);
    }

    @Override
    protected void drawContents() {
        ParameterUI.draw(lx, lx.engine.audio.enabled);
        UI.CollapseResult section;

        section = UI.collapsibleSection("Input", false);
        if (section.isOpen) {
        }

        section = UI.collapsibleSection("Output", false);
        if (section.isOpen) {
            ParameterUI.draw(lx, lx.engine.audio.output.play);
            ParameterUI.draw(lx, lx.engine.audio.output.looping);
            ParameterUI.draw(lx, lx.engine.audio.output.trigger);

            if (UI.button("Pick")) {
                EventQueue.invokeLater(() -> {
                        FileDialog dialog = new FileDialog(
                            (Frame) null, "Open WAV file", FileDialog.LOAD);
                        dialog.setVisible(true);
                        String fname = dialog.getFile();
                        if (fname == null) {
                            return;
                        }
                        final File file = new File(dialog.getDirectory(), fname);
                        Path path = new File(Utils.sketchPath()).toPath().relativize(file.toPath());
                        lx.engine.addTask(() -> {
                                lx.engine.audio.output.file.setValue(path.toString());
                            });
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
