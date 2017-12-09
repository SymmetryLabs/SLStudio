package com.symmetrylabs.p3lx;

import com.google.gson.JsonObject;
import com.symmetrylabs.SLStudio;
import com.symmetrylabs.palettes.PaletteLibrary;
import com.symmetrylabs.ui.UIAxes;
import com.symmetrylabs.ui.UICubeMapDebug;
import com.symmetrylabs.ui.UIFramerate;
import com.symmetrylabs.ui.UIMarkerPainter;
import com.symmetrylabs.ui.UIOverriddenRightPane;
import com.symmetrylabs.util.MarkerSource;
import heronarts.lx.LX;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.LXSerializable;
import heronarts.lx.model.LXModel;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIEventHandler;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIGLPointCloud;
import heronarts.p3lx.ui.studio.UIBottomTray;
import heronarts.p3lx.ui.studio.UIContextualHelpBar;
import heronarts.p3lx.ui.studio.UILeftPane;
import heronarts.p3lx.ui.studio.clip.UIClipLauncher;
import heronarts.p3lx.ui.studio.clip.UIClipView;
import heronarts.p3lx.ui.studio.device.UIDeviceBin;
import heronarts.p3lx.ui.studio.mixer.UIMixer;
import heronarts.p3lx.ui.studio.mixer.UIMixerStrip;
import heronarts.p3lx.ui.studio.mixer.UIMixerStripControls;
import heronarts.p3lx.ui.studio.modulation.UIModulator;
import processing.core.PApplet;
import processing.event.KeyEvent;

import java.io.File;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class LXStudio extends P3LX {

    public static final String COPYRIGHT = "Symmetry Labs";
    public PaletteLibrary paletteLibrary;

    public class UI extends heronarts.p3lx.ui.UI implements LXSerializable {

        public final PreviewWindow preview;
        public final UILeftPane leftPane;
        public final UIOverriddenRightPane rightPane;
        public final UIBottomTray bottomTray;
        public final UIContextualHelpBar helpBar;
        public final UIFramerate framerate;
        public final UIAxes axes;
        public final UIMarkerPainter markerPainter;
        public final UICubeMapDebug cubeMapDebug;

        private boolean toggleHelpBar = false;
        private boolean toggleClipView = false;
        private boolean clipViewVisible = true;
        private boolean performanceMode = false;

        public class PreviewWindow extends UI3dContext {

            public final UIGLPointCloud pointCloud;

            PreviewWindow(UI ui, P3LX lx, int x, int y, int w, int h) {
                super(ui, x, y, w, h);
                addComponent(this.pointCloud = (UIGLPointCloud) new UIGLPointCloud(lx).setPointSize(3));
                setCenter(lx.model.cx, lx.model.cy, lx.model.cz);
                setRadius(lx.model.rMax * 1.5f);
                setDescription("Preview Window: Displays the main output, or the channels/groups with CUE enabled");
            }

            @Override
            protected void onResize() {
                this.pointCloud.loadShader();
            }
        }

        UI(final LXStudio lx) {
            super(lx);
            initialize(lx, this);
            setBackgroundColor(this.theme.getDarkBackgroundColor());

            this.preview = new PreviewWindow(
                this,
                lx,
                UILeftPane.WIDTH,
                0,
                this.applet.width - UILeftPane.WIDTH - UIOverriddenRightPane.WIDTH,
                this.applet.height - UIBottomTray.HEIGHT - UIContextualHelpBar.VISIBLE_HEIGHT
            );
            this.leftPane = new UILeftPane(this, lx);
            this.rightPane = new UIOverriddenRightPane(this, lx);
            this.bottomTray = new UIBottomTray(this, lx);
            this.helpBar = new UIContextualHelpBar(this);
            this.framerate = new UIFramerate(this, lx, this.leftPane.getX() + this.leftPane.getWidth());
            this.axes = new UIAxes();
            this.markerPainter = new UIMarkerPainter();
            this.cubeMapDebug = new UICubeMapDebug(lx);
            this.preview.addComponent(this.cubeMapDebug);
            this.preview.addComponent(axes);
            this.preview.addComponent(markerPainter);


            addLayer(this.preview);
            addLayer(this.leftPane);
            addLayer(this.rightPane);
            addLayer(this.bottomTray);
            addLayer(this.helpBar);
            addLayer(this.framerate);

            _toggleClipView();
            _togglePerformanceMode();

            setTopLevelKeyEventHandler(new UIEventHandler() {
                @Override
                protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                    if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
                        lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                    } else if (keyChar == '?' && keyEvent.isShiftDown()) {
                        toggleHelpBar = true;
                    } else if (keyCode == java.awt.event.KeyEvent.VK_M && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
                        if (keyEvent.isShiftDown()) {
                            if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI) {
                                lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                            } else {
                                lx.engine.mapping.setMode(LXMappingEngine.Mode.MIDI);
                            }
                        } else {
                            if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.MODULATION_SOURCE) {
                                lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                            } else {
                                lx.engine.mapping.setMode(LXMappingEngine.Mode.MODULATION_SOURCE);
                            }
                        }
                    } else if (keyChar == "x".charAt(0)) {
                        axes.toggleVisible();
                    } else if (keyChar == "c".charAt(0)) {
                        cubeMapDebug.toggleVisible();
                    } else if (keyChar == "f".charAt(0)) {
                        framerate.toggleVisible();
                    } else if (keyChar == "p".charAt(0)) {
                        togglePerformanceMode();
                    } else if (keyChar == "v".charAt(0)) {
                        lx.ui.preview.toggleVisible();
                    }
                }
            });

            setResizable(true);
        }

        @Override
        protected void beginDraw() {
            if (this.toggleHelpBar) {
                this.toggleHelpBar = false;
                toggleHelpBar();
            }
            if (this.toggleClipView) {
                this.toggleClipView = false;
                _toggleClipView();
            }
        }

        public boolean isClipViewVisible() {
            return this.clipViewVisible;
        }

        private void setClipViewVisible(boolean visible) {
            if (this.clipViewVisible != visible) {
                toggleClipView();
            }
        }

        public boolean toggleClipView() {
            this.toggleClipView = true;
            return (this.clipViewVisible = !this.clipViewVisible);
        }

        public void togglePerformanceMode() {
            this.performanceMode = !this.performanceMode;
            _togglePerformanceMode();
        }

        private void _togglePerformanceMode() {
            this.leftPane.setVisible(!performanceMode);
            this.rightPane.setVisible(!performanceMode);
            if (SLStudio.applet.performanceManager == null) return;
            for (UIWindow w : SLStudio.applet.performanceManager.windows) {
                if (w != null) {
                    w.setVisible(performanceMode);
                }
            }
            for (UIWindow w : SLStudio.applet.performanceManager.crossfaders) {
                if (w != null) {
                    w.setVisible(performanceMode);
                }
            }

            if (SLStudio.applet.lx == null || SLStudio.applet.lx.engine == null || SLStudio.applet.lx.engine.midi == null)
                return;

            // lx.engine.midi.whenReady(new Runnable() {
            //   public void run() {
            //     for (final LXMidiSurface s : lx.engine.midi.surfaces) {
            //       s.enabled.setValue(!performanceMode);
            //     }
            //   }
            // });

            if (performanceMode) {
                SLStudio.applet.performanceManager.windows[SLStudio.applet.performanceManager.getWindowIndex(0)].rebindDeck();
                SLStudio.applet.performanceManager.windows[SLStudio.applet.performanceManager.getWindowIndex(1)].rebindDeck();
            }

        }


        @Override
        protected void onResize() {
            reflow();
        }

        private void toggleHelpBar() {
            this.helpBar.toggleVisible();
            reflow();
        }

        private void _toggleClipView() {
            // Mixer section
            float controlsY = this.clipViewVisible ? UIMixer.PADDING + UIClipLauncher.HEIGHT : 0;
            float stripHeight = this.clipViewVisible ? UIMixerStrip.HEIGHT : UIMixerStripControls.HEIGHT;

            UIMixer mixer = this.bottomTray.mixer;
            for (UIMixerStrip strip : mixer.channelStrips.values()) {
                strip.clipLauncher.setVisible(false);
                strip.controls.setY(controlsY);
                strip.setHeight(stripHeight);
            }
            mixer.addChannelButton.setY(controlsY + UIMixer.PADDING);
            mixer.masterStrip.clipLauncher.setVisible(false);
            mixer.masterStrip.controls.setY(controlsY);
            mixer.masterStrip.setHeight(stripHeight);
            mixer.sceneStrip.sceneLauncher.setVisible(false);
            mixer.sceneStrip.clipViewToggle.setY(controlsY);
            mixer.sceneStrip.setHeight(stripHeight);
            mixer.setContentHeight(stripHeight + 2 * UIMixer.PADDING);

            // Clip/device section
            this.bottomTray.clipView.setVisible(this.clipViewVisible);
            float binY = this.clipViewVisible
                ? UIClipView.HEIGHT + UIBottomTray.PADDING + UIMixerStrip.SPACING - 1
                : UIMixerStrip.SPACING;
            for (UIDeviceBin bin : this.bottomTray.deviceBins.values()) {
                bin.setY(binY);
            }
            this.bottomTray.rightSection.setHeight(stripHeight + 2 * UIMixer.PADDING);

            // Overall height
            this.bottomTray.setHeight(this.clipViewVisible ? UIBottomTray.HEIGHT : UIBottomTray.CLOSED_HEIGHT);

            // Reflow the UI
            reflow();
        }

        @Override
        public void reflow() {
            float uiWidth = getWidth();
            float uiHeight = getHeight();
            float helpBarHeight = this.helpBar.isVisible() ? UIContextualHelpBar.VISIBLE_HEIGHT : 0;
            float bottomTrayHeight = this.bottomTray.getHeight();
            float bottomTrayY = Math.max(100, uiHeight - bottomTrayHeight - helpBarHeight);
            this.bottomTray.setY(bottomTrayY);
            this.bottomTray.setWidth(uiWidth);
            this.bottomTray.reflow();
            this.helpBar.setY(uiHeight - helpBarHeight);
            this.helpBar.setWidth(uiWidth);
            this.leftPane.setHeight(bottomTrayY);
            this.rightPane.setHeight(bottomTrayY);
            this.rightPane.setX(uiWidth - this.rightPane.getWidth());

            this.preview.setSize(
                Math.max(100, uiWidth - this.leftPane.getWidth() - this.rightPane.getWidth()),
                Math.max(100, bottomTrayY)
            );
        }

        private static final String KEY_AUDIO_EXPANDED = "audioExpanded";
        private static final String KEY_PALETTE_EXPANDED = "paletteExpanded";
        private static final String KEY_MODULATORS_EXPANDED = "modulatorExpanded";
        private static final String KEY_ENGINE_EXPANDED = "engineExpanded";
        private static final String KEY_CAMERA_EXPANDED = "cameraExpanded";
        private static final String KEY_CLIP_VIEW_VISIBLE = "clipViewVisible";
        private static final String KEY_PREVIEW = "preview";

        @Override
        public void save(LX lx, JsonObject object) {
            object.addProperty(KEY_AUDIO_EXPANDED, this.leftPane.audio.isExpanded());
            object.addProperty(KEY_PALETTE_EXPANDED, this.leftPane.palette.isExpanded());
            object.addProperty(KEY_ENGINE_EXPANDED, this.leftPane.engine.isExpanded());
            //object.addProperty(KEY_CAMERA_EXPANDED, this.leftPane.camera.isExpanded());
            object.addProperty(KEY_CLIP_VIEW_VISIBLE, this.clipViewVisible);
            JsonObject modulatorObj = new JsonObject();

            for (UIObject child : this.rightPane.modulation) {
                // TODO Java: Is this the right UIModfulator?
                if (child instanceof UIModulator) {
                    UIModulator uiModulator = (UIModulator) child;
                    modulatorObj.addProperty(uiModulator.getIdentifier(), uiModulator.isExpanded());
                }
            }
            object.add(KEY_MODULATORS_EXPANDED, modulatorObj);
            object.add(KEY_PREVIEW, Utils.toObject(lx, ui.preview));
        }

        @Override
        public void load(LX lx, JsonObject object) {
            if (object.has(KEY_AUDIO_EXPANDED)) {
                this.leftPane.audio.setExpanded(object.get(KEY_AUDIO_EXPANDED).getAsBoolean());
            }
            if (object.has(KEY_PALETTE_EXPANDED)) {
                this.leftPane.palette.setExpanded(object.get(KEY_PALETTE_EXPANDED).getAsBoolean());
            }
            if (object.has(KEY_ENGINE_EXPANDED)) {
                this.leftPane.engine.setExpanded(object.get(KEY_ENGINE_EXPANDED).getAsBoolean());
            }
            if (object.has(KEY_CAMERA_EXPANDED)) {
                this.leftPane.camera.setExpanded(object.get(KEY_CAMERA_EXPANDED).getAsBoolean());
            }
            if (object.has(KEY_CLIP_VIEW_VISIBLE)) {
                setClipViewVisible(object.get(KEY_CLIP_VIEW_VISIBLE).getAsBoolean());
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
            if (object.has(KEY_PREVIEW)) {
                ui.preview.load(lx, object.getAsJsonObject(KEY_PREVIEW));
            }
        }

        public void addMarkerSource(MarkerSource source) {
            markerPainter.addSource(source);
        }

        public void removeMarkerSource(MarkerSource source) {
            markerPainter.removeSource(source);
        }
    }

    private static final String DEFAULT_PROJECT_FILE = "default.lxp";
    private static final String PROJECT_FILE_NAME = ".lxproject";
    private static final String KEY_UI = "ui";

    public final UI ui;

    public LXStudio(PApplet applet, LXModel model) {
        this(applet, model, true);
    }

    public LXStudio(PApplet applet, LXModel model, boolean multiThreaded) {
        super(applet, model);
        this.ui = (UI) super.ui;
        onUIReady(this, this.ui);
        registerExternal(KEY_UI, this.ui);

        try {
            File projectFile = this.applet.saveFile(PROJECT_FILE_NAME);
            if (projectFile.exists()) {
                String[] lines = this.applet.loadStrings(PROJECT_FILE_NAME);
                if (lines != null && lines.length > 0) {
                    File file = this.applet.saveFile(lines[0]);
                    if (file.exists()) {
                        openProject(file);
                    }
                }
            } else {
                File defaultProject = this.applet.saveFile(DEFAULT_PROJECT_FILE);
                if (defaultProject.exists()) {
                    openProject(defaultProject);
                }
            }
        } catch (Exception x) {
            // ignored
        }

        this.engine.setThreaded(multiThreaded);
    }

    @Override
    protected void setProject(File file, ProjectListener.Change change) {
        super.setProject(file, change);
        if (file != null) {
            this.applet.saveStrings(PROJECT_FILE_NAME, new String[]{file.getName()});
        }
    }

    @Override
    protected heronarts.p3lx.ui.UI buildUI() {
        return new UI(this);
    }

    /**
     * Subclasses may override to register additional components before the UI is built
     */
    protected void initialize(LXStudio lx, LXStudio.UI ui) {
    }

    protected void onUIReady(LXStudio lx, LXStudio.UI ui) {
    }

}
