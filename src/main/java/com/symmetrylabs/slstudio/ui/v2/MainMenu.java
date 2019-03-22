package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.SLStudio;

import heronarts.lx.LX;
import heronarts.lx.data.Project;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiInput;
import heronarts.lx.midi.surface.LXMidiSurface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainMenu implements Window {
    private final LX lx;
    private final SLStudioGDX parent;

    public MainMenu(LX lx, SLStudioGDX parent) {
        this.lx = lx;
        this.parent = parent;
    }

    public void draw() {
        if (!UI.beginMainMenuBar()) {
            return;
        }
        if (UI.beginMenu("File")) {
            if (UI.menuItem("New")) {
                lx.engine.addTask(() -> lx.newProject());
            }
            if (UI.menuItem("Open...")) {
                FileDialog.open(
                    lx, "Open project", projectFile -> {
                        /* since this changes pretty much everything in
                           LXEngine, we make sure the UI isn't displayed while
                           the project is loading. Without this, we almost
                           always get a concurrent modification exception while
                           rendering the UI. */
                        WindowManager.runSafelyWithEngine(
                            lx, () -> lx.openProject(Project.createLegacyProject(projectFile, SLStudioGDX.RUNTIME_VERSION)));
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

        if (UI.beginMenu("MIDI")) {
            final LXMidiEngine midi = lx.engine.midi;

            UI.menuText("Inputs");
            if (midi.getInputs().isEmpty()) {
                UI.menuText("(no MIDI inputs detected)");
            }
            for (LXMidiInput input : midi.getInputs()) {
                if (UI.beginMenu(input.getDescription() + "##input")) {
                    UI.menuText(input.getName());
                    ParameterUI.menuItem(lx, input.channelEnabled, "Send events to channels");
                    ParameterUI.menuItem(lx, input.controlEnabled, "Allow control mapping");
                    ParameterUI.menuItem(lx, input.syncEnabled, "Use MIDI clock to set tempo");
                    UI.endMenu();
                }
            }

            UI.separator();
            UI.menuText("Auto-mapping");
            if (midi.surfaces.isEmpty()) {
                UI.menuText("(no MIDI surfaces detected)");
            }
            for (LXMidiSurface surface : midi.surfaces) {
                ParameterUI.menuItem(lx, surface.enabled, surface.getDescription() + "##surface");
            }
            UI.endMenu();
        }

        if (UI.beginMenu("Show")) {
            for (String showName : ShowRegistry.getNames()) {
                if (UI.menuItem(showName)) {
                    try {
                        Files.write(Paths.get(SLStudio.SHOW_FILE_NAME), showName.getBytes());
                        parent.loadShow(showName);
                        break;
                    } catch (IOException e) {
                        System.err.println("couldn't write new show: " + e.getMessage());
                    }
                }
            }
            UI.endMenu();
        }

        if (UI.beginMenu("Window")) {
            WindowManager wm = WindowManager.get();
            /* Iterate over entries to preserve order */
            for (WindowManager.PersistentWindow ws : wm.getSpecs()) {
                if (UI.menuItemToggle(ws.name, null, ws.current != null, true)) {
                    wm.showPersistent(ws);
                } else {
                    wm.hidePersistent(ws);
                }
            }
            UI.endMenu();
        }
        UI.endMainMenuBar();
    }

    private void runSaveAs() {
        FileDialog.save(
            lx, "Save project", projectFile -> lx.saveProject(Project.createLegacyProject(projectFile, SLStudioGDX.RUNTIME_VERSION)));
    }
}
