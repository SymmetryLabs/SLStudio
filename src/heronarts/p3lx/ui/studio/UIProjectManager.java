/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.studio;

import java.io.File;

import heronarts.lx.LX;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dCollapsibleSection;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UILabel;
import processing.core.PConstants;

public class UIProjectManager extends UI2dCollapsibleSection {

    private final LX lx;
    private final UILabel fileLabel;
    private File file;

    public UIProjectManager(final UI ui, final LX lx, float x, float y, float w, float h) {
        super(ui, x, y, w, h);
        this.lx = lx;
        this.file = lx.getProject();
        if (this.file == null) {
            this.file = ui.applet.saveFile("default.lxp");
        }

        setTitle("PROJECT");
        setExpanded(false);

        this.fileLabel = new UILabel(0, 0, getContentWidth(), 16);
        this.fileLabel
        .setLabel(this.file.getName())
        .setBackgroundColor(UI.BLACK)
        .setBorderRounding(4)
        .setTextAlignment(PConstants.CENTER, PConstants.CENTER)
        .addToContainer(this);

        lx.addProjectListener(new LX.ProjectListener() {
            public void projectChanged(File file) {
                UIProjectManager.this.file = file;
                fileLabel.setLabel(file.getName());
            }
        });

        new UIButton(0, 20, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    lx.saveProject(file);
                }
            }
        }
        .setLabel("Save")
        .setMomentary(true)
        .addToContainer(this);

        new UIButton(0, 40, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    ui.applet.selectOutput("Select a file to save to:", "onSave", ui.applet.saveFile("default.lxp"), UIProjectManager.this);
                }
            }
        }
        .setLabel("Save As...")
        .setMomentary(true)
        .addToContainer(this);

        new UIButton(0, 60, getContentWidth(), 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    ui.applet.selectInput("Select a file to load:", "onLoad", ui.applet.saveFile("default.lxp"), UIProjectManager.this);
                }
            }
        }
        .setLabel("Load...")
        .setMomentary(true)
        .addToContainer(this);
    }

    public void onSave(final File saveFile) {
        if (saveFile != null) {
            lx.engine.addTask(new Runnable() {
                public void run() {
                    lx.saveProject(saveFile);
                }
            });
        }
    }

    public void onLoad(final File loadFile) {
        if (loadFile != null) {
            lx.engine.addTask(new Runnable() {
                public void run() {
                    lx.loadProject(loadFile);
                }
            });
        }
    }
}
