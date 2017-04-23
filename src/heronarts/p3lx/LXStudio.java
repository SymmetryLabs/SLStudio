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

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.LXSerializable;
import heronarts.lx.model.LXModel;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIEventHandler;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIGLPointCloud;
import heronarts.p3lx.ui.studio.UIBottomTray;
import heronarts.p3lx.ui.studio.UIContextualHelpBar;
import heronarts.p3lx.ui.studio.UILeftPane;
import heronarts.p3lx.ui.studio.UIRightPane;
import heronarts.p3lx.ui.studio.modulation.UIModulator;
import processing.core.PApplet;
import processing.event.KeyEvent;

public class LXStudio extends P3LX {

    public static final String COPYRIGHT = "LXStudio: Designed + Developed by Mark C. Slee. Copyright 2017 Heron Arts LLC.";

    public class UI extends heronarts.p3lx.ui.UI implements LXSerializable {

        public final PreviewWindow preview;
        public final UILeftPane leftPane;
        public final UIRightPane rightPane;
        public final UIBottomTray bottomTray;
        public final UIContextualHelpBar helpBar;

        private boolean toggleHelpBar = false;

        public class PreviewWindow extends UI3dContext {
            PreviewWindow(UI ui, P3LX lx, int x, int y, int w, int h) {
                super(ui, x, y, w, h);
                addComponent(new UIGLPointCloud(lx).setPointSize(3));
                setCenter(lx.model.cx, lx.model.cy, lx.model.cz);
                setRadius(lx.model.rMax * 1.5f);
                setDescription("Preview Window: Displays the main output, or the channels/groups with CUE enabled");
            }

            @Override
            protected void onUIResize(heronarts.p3lx.ui.UI ui) {
                onHelpBarToggle(ui);
            }

            void onHelpBarToggle(heronarts.p3lx.ui.UI ui) {
                float availableHeight = ui.getHeight() - UIBottomTray.HEIGHT;
                if (helpBar.isVisible()) {
                    availableHeight -= UIContextualHelpBar.HEIGHT;
                }
                setSize(
                    Math.max(100, ui.getWidth() - UILeftPane.WIDTH - UIRightPane.WIDTH),
                    Math.max(100, availableHeight)
                );
            }
        }

        UI(final LXStudio lx) {
            super(lx);
            initialize(lx, this);
            setBackgroundColor(this.theme.getDarkBackgroundColor());

            this.preview = new PreviewWindow(this, lx, UILeftPane.WIDTH, 0, this.applet.width - UILeftPane.WIDTH - UIRightPane.WIDTH, this.applet.height - UIBottomTray.HEIGHT - UIContextualHelpBar.HEIGHT);
            this.leftPane = new UILeftPane(this, lx);
            this.rightPane = new UIRightPane(this, lx);
            this.bottomTray = new UIBottomTray(this, lx);
            this.helpBar = new UIContextualHelpBar(this);

            addLayer(this.preview);
            addLayer(this.leftPane);
            addLayer(this.rightPane);
            addLayer(this.bottomTray);
            addLayer(this.helpBar);

            setTopLevelKeyEventHandler(new UIEventHandler() {
                @Override
                protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                    if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                        lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                    } else if (keyChar == '?' && keyEvent.isShiftDown()) {
                        toggleHelpBar = true;
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

        @Override
        protected void beforeDraw() {
            if (toggleHelpBar) {
                toggleHelpBar = false;
                toggleHelpBar();
            }
        }

        private void toggleHelpBar() {
            this.helpBar.toggleVisible();
            this.bottomTray.onHelpBarToggle(this);
            this.leftPane.onHelpBarToggle(this);
            this.rightPane.onHelpBarToggle(this);
            this.preview.onHelpBarToggle(this);
        }

        private static final String KEY_AUDIO_EXPANDED = "audioExpanded";
        private static final String KEY_PALETTE_EXPANDED = "paletteExpanded";
        private static final String KEY_MODULATORS_EXPANDED = "modulatorExpanded";

        @Override
        public void save(LX lx, JsonObject object) {
            object.addProperty(KEY_AUDIO_EXPANDED, this.leftPane.audio.isExpanded());
            object.addProperty(KEY_PALETTE_EXPANDED, this.leftPane.palette.isExpanded());
            JsonObject modulatorObj = new JsonObject();
            for (UIObject child : this.rightPane.modulation) {
                if (child instanceof UIModulator) {
                    UIModulator uiModulator = (UIModulator) child;
                    modulatorObj.addProperty(uiModulator.getIdentifier(), uiModulator.isExpanded());
                }
            }
            object.add(KEY_MODULATORS_EXPANDED, modulatorObj);
        }

        @Override
        public void load(LX lx, JsonObject object) {
            if (object.has(KEY_AUDIO_EXPANDED)) {
                this.leftPane.audio.setExpanded(object.get(KEY_AUDIO_EXPANDED).getAsBoolean());
            }
            if (object.has(KEY_PALETTE_EXPANDED)) {
                this.leftPane.palette.setExpanded(object.get(KEY_PALETTE_EXPANDED).getAsBoolean());
            }
            if (object.has(KEY_MODULATORS_EXPANDED)) {
                JsonObject modulatorObj = object.getAsJsonObject(KEY_MODULATORS_EXPANDED);
                for (UIObject child : this.rightPane.modulation) {
                    if (child instanceof UIModulator) {
                        UIModulator uiModulator = (UIModulator) child;
                        String identifier = uiModulator.getIdentifier();
                        if (modulatorObj.has(identifier)) {
                            uiModulator.setExpanded(modulatorObj.get(identifier).getAsBoolean());
                        }
                    }
                }
            }
        }
    }

    private static final String DEFAULT_FILE_NAME = "default.lxp";
    private static final String KEY_UI = "ui";

    public final UI ui;

    public LXStudio(PApplet applet, LXModel model) {
        super(applet, model);
        this.ui = (UI) super.ui;
        onUIReady(this, this.ui);
        registerExternal(KEY_UI, this.ui);

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
