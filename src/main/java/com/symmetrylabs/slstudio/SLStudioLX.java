package com.symmetrylabs.slstudio;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import com.symmetrylabs.shows.ShowRegistry;
import com.symmetrylabs.slstudio.ui.*;
import heronarts.lx.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.MouseEvent;
import processing.event.KeyEvent;

import heronarts.lx.effect.BlurEffect;
import heronarts.lx.effect.DesaturationEffect;
import heronarts.lx.effect.FlashEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.IteratorTestPattern;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UIEventHandler;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.component.UIImage;
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

import com.symmetrylabs.LXClassLoader;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.performance.PerformanceManager;
import com.symmetrylabs.util.CaptionSource;
import com.symmetrylabs.util.MarkerSource;

import javax.swing.*;

import static java.awt.event.KeyEvent.*;
import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.RGB8;

public class SLStudioLX extends P3LX {
    public static final String COPYRIGHT = "Symmetry Labs";

    private static final String DEFAULT_PROJECT_FILE = "default.lxp";
    private static final String PROJECT_FILE_NAME = ".lxproject";
    private static final String KEY_UI = "ui";
    private static final int RESTART_EXIT_CODE = 999;

    public class UI extends heronarts.p3lx.ui.UI implements LXSerializable {
        public final UIPreviewWindow preview;
        public final UILeftPane leftPane;
        public final UIOverriddenRightPane rightPane;
        public final UIBottomTray bottomTray;
        public final UIContextualHelpBar helpBar;
        public final UIFramerate framerate;
        public final UITextOverlay helpHelp;
        public final UITextOverlay helpText;
        public final UICaptionText captionText;
        public final UITextOverlay warningText;
        public final UIAxes axes;
        public final UIMarkerPainter markerPainter;
        public final UICubeMapDebug cubeMapDebug;
        public final UICameraControls cameraControls;

        private final List<Runnable> runPostDraw;

        private boolean toggleHelpBar = false;
        private boolean toggleClipView = false;
        private boolean clipViewVisible = true;
        private boolean performanceMode = false;

        private final LX lx;

        /**
         * Help text to display when "?" is pressed.  "@" will be replaced with
         * "Cmd" or "Ctrl", as appropriate for the operating system.
         */
        private static final String HELP_TEXT =
              "@-C           Toggle P3CubeMap debugging\n" +
                "@-D           Delete selected channel, warp, effect, or pattern\n" +
                "@-F           Toggle frame rate status line\n" +
                "@-G           Toggle UI geometry\n" +
                "@-L           Load a show\n" +
                "@-M           Toggle modulation mapping mode\n" +
                "@-Shift-M     Toggle MIDI mapping mode\n" +
                "@-N           New channel\n" +
                "@-P           Performance mode\n" +
                "@-R           Rename channel or pattern\n" +
                "@-S           Save current project\n" +
                "@-V           Toggle preview display\n" +
                "@-X           Toggle axis display\n" +
                "@-T           Toggle orthographic projection\n" +
                "@-/           Toggle help caption line\n" +
                "@-\\           Toggle 16-bit color (all)\n" +
                "@-|           Toggle 16-bit color (selected channel)\n" +
                "@-[/]         Decrease/increase rendered point size\n" +
                "@-Left/Right  Reorder selected channel, warp, or effect\n" +
                "@-Up/Down     Reorder selected pattern"
            ;

        UI(final SLStudioLX lx) {
            super(lx);

            this.lx = lx;

            initialize(lx, this);

            this.preview = new UIPreviewWindow(this, lx, UILeftPane.WIDTH, 0,
            this.applet.width - UILeftPane.WIDTH - UIOverriddenRightPane.WIDTH,
            this.applet.height - UIBottomTray.HEIGHT - UIContextualHelpBar.VISIBLE_HEIGHT);

            this.leftPane = new UILeftPane(this, lx);
            this.rightPane = new UIOverriddenRightPane(this, lx);
            this.bottomTray = new UIBottomTray(this, lx);
            this.helpBar = new UIContextualHelpBar(this);

            this.framerate = new UIFramerate(this, lx, preview, 6, 6, PConstants.LEFT, PConstants.TOP);

            this.helpHelp = new UITextOverlay(this, preview, -6, 6, PConstants.RIGHT, PConstants.TOP);
            helpHelp.setText("(? for help)");

            this.helpText = new UITextOverlay(this, preview, 6, 40, PConstants.LEFT, PConstants.TOP);
            int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            helpText.setText(HELP_TEXT.replaceAll("@", mask == KeyEvent.CTRL ? "Ctrl" : "Cmd"));
            helpText.setVisible(false);

            this.captionText = new UICaptionText(this, preview, 6, -6, PConstants.LEFT, PConstants.BOTTOM);
            this.warningText = new UITextOverlay(this, preview, 6, -6, PConstants.LEFT, PConstants.BOTTOM);
            this.warningText.setColor(0xffff4040);
            updateWarningText(SLStudio.warnings);

            this.axes = new UIAxes();
            this.markerPainter = new UIMarkerPainter();
            this.cubeMapDebug = new UICubeMapDebug(lx);
            this.cameraControls = new UICameraControls(this, preview);

            this.preview.addComponent(this.cubeMapDebug);
            this.preview.addComponent(axes);
            this.preview.addComponent(markerPainter);

            new UI2dComponent(0, 0, leftPane.getWidth(), 30) {}.setBackgroundColor(0).addToContainer(leftPane);

            new UIImage(applet.loadImage("symmetry-labs-logo.png"), 4, 4)
            .setDescription("Symmetry Labs")
            .addToContainer(leftPane);

            addLayer(this.preview);
            addLayer(this.leftPane);
            addLayer(this.rightPane);
            addLayer(this.bottomTray);
            addLayer(this.helpBar);
            addLayer(this.framerate);
            addLayer(this.helpHelp);
            addLayer(this.helpText);
            addLayer(this.captionText);
            addLayer(this.warningText);
            addLayer(this.cameraControls);

            _toggleClipView();
            _togglePerformanceMode();

            setTopLevelKeyEventHandler(new UIEventHandler() {
                @Override
                public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
                    if (keyChar == '?') {
                        helpText.toggleVisible();
                    }
                    if (keyChar == 27) {
                        lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                    }

                    // Remember to update HELP_TEXT above when adding/changing any hotkeys!
                    if (keyEvent.isMetaDown() || keyEvent.isControlDown()) {
                        switch (keyCode) {
                            case VK_C:
                                cubeMapDebug.toggleVisible();
                                break;
                            case VK_F:
                                framerate.toggleVisible();
                                break;
                            case VK_G:
                                markerPainter.toggleVisible();
                                break;
                            case VK_L:
                                String showName = (String) JOptionPane.showInputDialog(
                                    null, "Select a show and click OK to restart.", "Load a show",
                                    JOptionPane.QUESTION_MESSAGE, null, ShowRegistry.getNames().toArray(), null);
                                if (showName != null) {
                                    applet.saveStrings(SLStudio.SHOW_FILE_NAME, new String[] {showName});
                                    applet.saveStrings(SLStudio.RESTART_FILE_NAME, new String[0]);
                                    applet.exit();
                                }
                                break;
                            case VK_M:
                                LXMappingEngine.Mode mode = keyEvent.isShiftDown() ?
                                        LXMappingEngine.Mode.MIDI : LXMappingEngine.Mode.MODULATION_SOURCE;
                                lx.engine.mapping.setMode(
                                        lx.engine.mapping.getMode() == mode ?
                                        LXMappingEngine.Mode.OFF : mode);
                                break;
                            case VK_P:
                                togglePerformanceMode();
                                break;
                            case VK_T:
                                lx.ui.preview.ortho.toggle();
                                break;
                            case VK_V:
                                lx.ui.preview.toggleVisible();
                                break;
                            case VK_X:
                                axes.toggleVisible();
                                break;
                            case VK_SLASH:
                                toggleHelpBar = true;
                                break;
                            case VK_BACK_SLASH:
                                switch (keyChar) {
                                    case '\\':
                                        PolyBuffer.Space space = lx.engine.colorSpace.getEnum() == RGB16 ? RGB8 : RGB16;
                                        lx.engine.colorSpace.setValue(space);
                                        for (LXChannel channel : lx.engine.channels) {
                                            channel.colorSpace.setValue(space);
                                        }
                                        break;
                                    case '|':
                                        if (engine.getFocusedChannel() instanceof LXChannel) {
                                            LXChannel channel = (LXChannel) engine.getFocusedChannel();
                                            space = channel.colorSpace.getEnum() == RGB16 ? RGB8 : RGB16;
                                            channel.colorSpace.setValue(space);
                                        }
                                        break;
                                }
                                break;
                            case VK_OPEN_BRACKET:
                                preview.setPointSize(Math.max(1, preview.getPointSize() - 1));
                                break;
                            case VK_CLOSE_BRACKET:
                                preview.setPointSize(Math.min(30, preview.getPointSize() + 1));
                                break;
                        }
                    }
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            SLPattern pattern = (SLPattern) focusedPattern;
                            pattern.unconsumeKeyEvent();
                            pattern.onKeyPressed(keyEvent, keyChar, keyCode);
                            root.keyEventConsumed = pattern.keyEventConsumed();
                        }
                    }
                }

                @Override
                public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMousePressed(mouseEvent, mx, my);
                        }
                    }
                }

                @Override
                public void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseReleased(mouseEvent, mx, my);
                        }
                    }
                }

                @Override
                public void onMouseClicked(MouseEvent mouseEvent, float mx, float my) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseClicked(mouseEvent, mx, my);
                        }
                    }
                }

                @Override
                public void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseDragged(mouseEvent, mx, my, dx, dy);
                        }
                    }
                }

                @Override
                public void onMouseMoved(MouseEvent mouseEvent, float mx, float my) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseMoved(mouseEvent, mx, my);
                        }
                    }
                }

                @Override
                public void onMouseOver(MouseEvent mouseEvent) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseOver(mouseEvent);
                        }
                    }
                }

                @Override
                public void onMouseOut(MouseEvent mouseEvent) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseOut(mouseEvent);
                        }
                    }
                }

                @Override
                public void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            ((SLPattern) focusedPattern).onMouseWheel(mouseEvent, mx, my, delta);
                        }
                    }
                }

                @Override
                public void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            SLPattern pattern = (SLPattern) focusedPattern;
                            pattern.unconsumeKeyEvent();
                            pattern.onKeyReleased(keyEvent, keyChar, keyCode);
                            root.keyEventConsumed = pattern.keyEventConsumed();
                        }
                    }
                }

                @Override
                public void onKeyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {
                    if (engine.getFocusedChannel() instanceof LXChannel) {
                        LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                        LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                        if (focusedPattern instanceof SLPattern) {
                            SLPattern pattern = (SLPattern) focusedPattern;
                            pattern.unconsumeKeyEvent();
                            pattern.onKeyTyped(keyEvent, keyChar, keyCode);
                            root.keyEventConsumed = pattern.keyEventConsumed();
                        }
                    }
                }
            });

            setResizable(true);

            runPostDraw = new ArrayList<>();
        }

        protected SLPattern getFocusedSLPattern() {
            if (engine.getFocusedChannel() instanceof LXChannel) {
                LXChannel focusedChannel = (LXChannel) engine.getFocusedChannel();
                LXPattern focusedPattern = focusedChannel.getFocusedPattern();
                if (focusedPattern instanceof SLPattern) {
                    return (SLPattern) focusedPattern;
                }
            }
            return null;
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

        public void runAfterDraw(Runnable r) {
            synchronized (runPostDraw) {
                runPostDraw.add(r);
            }
        }

        @Override
        protected void endDraw() {
            synchronized (runPostDraw) {
                for (Runnable r : runPostDraw) {
                    r.run();
                }
                runPostDraw.clear();
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
            if (SLStudio.applet.performanceManager == null) {
                reflow();
                return;
            }
            for (UIWindow w : SLStudio.applet.performanceManager.deckWindows) {
                if (w != null) {
                    w.setVisible(performanceMode);
                }
            }
            for (UIWindow w : SLStudio.applet.performanceManager.crossfaders) {
                if (w != null) {
                    w.setVisible(performanceMode);
                }
            }

            if (lx == null || lx.engine == null || lx.engine.midi == null) {
                reflow();
                return;
            }

            // lx.engine.midi.whenReady(new Runnable() {
            //   public void run() {
            //     for (final LXMidiSurface s : lx.engine.midi.surfaces) {
            //       s.enabled.setValue(!performanceMode);
            //     }
            //   }
            // });

            if (performanceMode) {
                SLStudio.applet.performanceManager.deckWindows[SLStudio.applet.performanceManager.focusedDeskIndexForSide(
                    PerformanceManager.DeckSide.LEFT)].rebindDeck();
                SLStudio.applet.performanceManager.deckWindows[SLStudio.applet.performanceManager.focusedDeskIndexForSide(PerformanceManager.DeckSide.RIGHT)].rebindDeck();
            }
            reflow();
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

            /*
            UI2dScrollContext outputsOuterScrollContext = this.rightPane.utility;
            float listHeight = outputsOuterScrollContext.getHeight() - UIOutputs.TOP_MARGIN;
            UI2dScrollContext outputsInnerScrollContext = this.rightPane.uiOutputs.outputList;
            if (outputsInnerScrollContext.getHeight() != listHeight) {
                outputsInnerScrollContext.setHeight(listHeight);
            }
            UIOutputs outputs = this.rightPane.uiOutputs;
            if (outputs.getContentTarget().getHeight() != listHeight) {
                outputs.getContentTarget().setHeight(listHeight);
            }
            */

            this.preview.setSize(
                Math.max(100, uiWidth - this.leftPane.getWidth() - this.rightPane.getWidth()),
                Math.max(100, bottomTrayY)
            );
            this.framerate.reposition();
            this.helpHelp.reposition();
            this.helpText.reposition();
            this.captionText.reposition();
            this.warningText.reposition();
            this.cameraControls.reposition(
                performanceMode ? rightPane.getWidth() : 0);
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

        public void addCaptionSource(CaptionSource source) {
            captionText.addSource(source);
        }

        public void removeCaptionSource(CaptionSource source) {
            captionText.removeSource(source);
        }

        protected void updateWarningText(Map<String, String> warnings) {
            String text = "";
            List<String> keys = new ArrayList<String>(warnings.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                String message = warnings.get(key);
                if (message != null) {
                    text += key + ": " + message + "\n";
                }
            }
            text = text.trim();
            int numLines = text.isEmpty() ? 0 : text.split("\n").length;
            captionText.setAnchor(6, -6 - numLines * SLStudio.MONO_FONT.lineHeight);
            warningText.setText(text);
        }

        @Override
        public String getActivePatternGroup() {
            return ((SLStudio) applet).getSelectedShowName();
        }
    }

    public final UI ui;

    public SLStudioLX(PApplet applet, LXModel model) {
        this(applet, model, true);
    }

    public SLStudioLX(PApplet applet, LXModel model, boolean multiThreaded) {
        super(applet, model);

        this.ui = (UI)super.ui;

        onUIReady(this, ui);
        registerExternal(KEY_UI, ui);

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

        engine.setThreaded(multiThreaded);
    }

    @Override
    protected void setProject(File file, ProjectListener.Change change) {
        super.setProject(file, change);
        if (file != null) {
            /* We have to turn the paths into absolute paths here to satisfy the
             * Windows Path implementation. */
            Path projectDir = applet.saveFile(PROJECT_FILE_NAME).getParentFile().toPath().toAbsolutePath();
            Path absPath = file.toPath().toAbsolutePath();
            Path relPath = projectDir.relativize(absPath);
            this.applet.saveStrings(PROJECT_FILE_NAME, new String[]{relPath.toString()});
        }
    }

    @Override
    protected UI buildUI() {
        return new UI(this);
    }

    protected void initialize(SLStudioLX lx, SLStudioLX.UI ui) {
        // Add all warps
        LXClassLoader.findWarps().stream().forEach(lx::registerWarp);

        // Add all effects
        LXClassLoader.findEffects().stream().forEach(lx::registerEffect);

        // Add all patterns
        LXClassLoader.findPatterns().stream().forEach(lx::registerPattern);

        lx.registerPattern(heronarts.p3lx.pattern.SolidColorPattern.class);
        lx.registerPattern(IteratorTestPattern.class);

        lx.registerEffect(FlashEffect.class);
        lx.registerEffect(BlurEffect.class);
        lx.registerEffect(DesaturationEffect.class);
    }

    protected void onUIReady(SLStudioLX lx, SLStudioLX.UI ui) { }

    @Override
    protected LXEffect instantiateEffect(final String className) {
        return super.instantiateEffect(LXClassLoader.guessExistingEffectClassName(className));
    }

    @Override
    protected LXPattern instantiatePattern(final String className) {
        return super.instantiatePattern(LXClassLoader.guessExistingPatternClassName(className));
    }
}
