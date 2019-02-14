package com.symmetrylabs.slstudio.ui.v2;

import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;

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
                    lx, "Open project", project -> {
                        /* since this changes pretty much everything in
                           LXEngine, we make sure the UI isn't displayed while
                           the project is loading. Without this, we almost
                           always get a concurrent modification exception while
                           rendering the UI. */
                        WindowManager.runSafelyWithEngine(lx, () -> lx.openProject(project));
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
                if (UI.checkbox(ws.name, ws.current != null)) {
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
        FileDialog.save(lx, "Save project", project -> lx.saveProject(project));
    }
}
