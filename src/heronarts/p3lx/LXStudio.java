/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx;

import java.io.File;

import heronarts.lx.LXMappingEngine;
import heronarts.lx.model.LXModel;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIEventHandler;
import heronarts.p3lx.ui.component.UIPointCloud;
import heronarts.p3lx.ui.studio.UIBottomTray;
import heronarts.p3lx.ui.studio.UILeftPane;
import heronarts.p3lx.ui.studio.UIRightPane;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class LXStudio extends P3LX {

    public class UI extends heronarts.p3lx.ui.UI {

        public final UI3dContext main;
        public final UILeftPane leftPane;
        public final UIRightPane rightPane;
        public final UIBottomTray bottomTray;

        UI(final LXStudio lx) {
            super(lx);

            initialize(lx, this);

            this.main = new UI3dContext(this, UILeftPane.WIDTH, 0, this.applet.width - UILeftPane.WIDTH - UIRightPane.WIDTH, this.applet.height - UIBottomTray.HEIGHT) {
                @Override
                protected void onUIResize(heronarts.p3lx.ui.UI ui) {
                    setSize(ui.getWidth() - UILeftPane.WIDTH - UIRightPane.WIDTH, ui.getHeight() - UIBottomTray.HEIGHT);
                }
            }
            .addComponent(new UIPointCloud(lx).setPointSize(3))
            .setCenter(lx.model.cx, lx.model.cy, lx.model.cz)
            .setRadius(lx.model.rMax * 1.5f);

            this.leftPane = new UILeftPane(this, lx);
            this.rightPane = new UIRightPane(this, lx);
            this.bottomTray = new UIBottomTray(this, lx);

            addLayer(this.main);
            addLayer(this.leftPane);
            addLayer(this.rightPane);
            addLayer(this.bottomTray);

            setTopLevelKeyEventHandler(new UIEventHandler() {
                @Override
                protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                    if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                        lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                    } else if (keyCode == java.awt.event.KeyEvent.VK_M && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
                        if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI) {
                            lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                        } else {
                            lx.engine.mapping.setMode(LXMappingEngine.Mode.MIDI);
                        }
                    } else if ((keyCode == java.awt.event.KeyEvent.VK_COMMA) && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
                        if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.MODULATION_SOURCE) {
                            lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                        } else {
                            lx.engine.mapping.setMode(LXMappingEngine.Mode.MODULATION_SOURCE);
                        }
                    }
                }
            });

            setResizable(true);
        }
    }

    private static final String DEFAULT_FILE_NAME = "default.lxp";

    public final UI ui;

    public LXStudio(PApplet applet, LXModel model) {
        super(applet, model);
        this.ui = (UI) super.ui;
        onUIReady(this, this.ui);

        File file = this.applet.saveFile(DEFAULT_FILE_NAME);
        if (file.exists()) {
            loadProject(file);
        }

        this.engine.setThreaded(true);
    }

    @Override
    protected heronarts.p3lx.ui.UI buildUI() {
        return new UI(this);
    }

    /**
     * Subclasses may override to register additional components before the UI is built
     */
    protected void initialize(LXStudio lx, LXStudio.UI ui) {}

    protected void onUIReady(LXStudio lx, LXStudio.UI ui) {}


}
