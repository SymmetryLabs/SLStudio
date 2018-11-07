package com.symmetrylabs.slstudio.ui.gdx;

import heronarts.lx.LX;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

public class MainMenu implements Window {
    private final LX lx;

    public MainMenu(LX lx) {
        this.lx = lx;
    }

    public void draw() {
        if (!UI.beginMainMenuBar()) {
            return;
        }
        if (UI.beginMenu("Project")) {
            if (UI.menuItem("New")) {
                lx.engine.addTask(() -> lx.newProject());
            }
            if (UI.menuItem("Open...")) {
                EventQueue.invokeLater(() -> {
                        FileDialog dialog = new FileDialog(
                            (Frame) null, "Open project", FileDialog.LOAD);
                        dialog.setVisible(true);
                        String fname = dialog.getFile();
                        if (fname == null) {
                            return;
                        }

                        final File project = new File(dialog.getDirectory(), fname);
                        WindowManager.get().disableUI();
                        lx.engine.addTask(() -> {
                                lx.openProject(project);
                                WindowManager.get().enableUI();
                            });
                    });
            }
            if (UI.menuItem("Save")) {
                if (lx.getProject() == null) {
                    runSaveAs();
                } else {
                    lx.saveProject();
                }
            }
            if (UI.menuItem("Save As...")) {
                runSaveAs();
            }
            UI.endMenu();
        }
        UI.endMainMenuBar();
    }

    private void runSaveAs() {
        EventQueue.invokeLater(() -> {
                FileDialog dialog = new FileDialog(
                    (Frame) null, "Save project", FileDialog.SAVE);
                dialog.setVisible(true);
                String fname = dialog.getFile();
                if (fname == null) {
                    return;
                }

                final File project = new File(dialog.getDirectory(), fname);
                lx.engine.addTask(() -> lx.saveProject(project));
            });
    }
}
