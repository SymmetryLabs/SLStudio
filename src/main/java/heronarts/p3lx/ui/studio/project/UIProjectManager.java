/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.studio.project;

import heronarts.lx.LX.ProjectListener;
import heronarts.lx.LX;
import heronarts.lx.data.Project;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import heronarts.p3lx.ui.studio.UICollapsibleSection;
import java.io.File;
import processing.core.PConstants;

public class UIProjectManager extends UICollapsibleSection {

    private final LX lx;
    private final UILabel fileLabel;
    private Project project;
    private final UIButton saveButton;
    private final UIButton saveAsButton;
    private final UIButton openButton;

    public static final float HEIGHT = 118;
    private static final String DEFAULT_PROJECT_NAME = "project.lxp";

    public UIProjectManager(final UI ui, final LX lx, float x, float y, float w) {
        super(ui, x, y, w, HEIGHT);
        this.lx = lx;
        this.project = lx.getProject();
        setTitle("PROJECT");

        this.fileLabel = new UILabel(0, 0, getContentWidth(), 16);
        this.fileLabel
        .setLabel(project != null ? project.getName() : "<New Project>")
        .setBackgroundColor(UI.BLACK)
        .setBorderRounding(4)
        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
        .addToContainer(this);

        lx.addProjectListener(new LX.ProjectListener() {
            public void projectChanged(Project project, ProjectListener.Change change) {
                UIProjectManager.this.project = project;
                fileLabel.setLabel(project != null ? project.getName() : "<New Project>");
            }
        });

        this.saveButton = (UIButton) new UIButton(0, 20, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    if (project != null) {
                        lx.saveProject(project);
                    } else {
                        ui.applet.selectOutput("Select a file to save to:", "onSave", ui.applet.saveFile(DEFAULT_PROJECT_NAME), UIProjectManager.this);
                    }
                }
            }
        }
        .setLabel("Save")
        .setMomentary(true)
        .addToContainer(this);

        this.saveAsButton = (UIButton) new UIButton(0, 40, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    ui.applet.selectOutput(
                        "Select a file to save to:", "onSave",
                        ui.applet.saveFile(project != null ? project.getRoot().toString() : DEFAULT_PROJECT_NAME),
                        UIProjectManager.this);
                }
            }
        }
        .setLabel("Save As...")
        .setMomentary(true)
        .addToContainer(this);

        this.openButton = (UIButton) new UIButton(0, 60, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    lx.newProject();
                }
            }
        }
        .setLabel("New...")
        .setMomentary(true)
        .addToContainer(this);

        new UIButton(0, 80, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    ui.applet.selectInput("Select a file to open:", "onOpen", ui.applet.saveFile("default.lxp"), UIProjectManager.this);
                }
            }
        }
        .setLabel("Open...")
        .setMomentary(true)
        .addToContainer(this);
    }

    public void onSave(final File saveFile) {
        this.saveButton.setActive(false);
        this.saveAsButton.setActive(false);
        if (saveFile != null) {
            lx.engine.addTask(new Runnable() {
                public void run() {
                    lx.saveProject(Project.createLegacyProject(saveFile, P3LX.RUNTIME_VERSION));
                }
            });
        }
    }

    public void onOpen(final File openFile) {
        this.openButton.setActive(false);
        if (openFile != null) {
            lx.engine.addTask(new Runnable() {
                public void run() {
                    lx.openProject(Project.createLegacyProject(openFile, P3LX.RUNTIME_VERSION));
                }
            });
        }
    }
}
