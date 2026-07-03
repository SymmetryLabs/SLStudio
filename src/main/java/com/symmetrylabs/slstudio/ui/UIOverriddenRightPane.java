package com.symmetrylabs.slstudio.ui;

import processing.core.PGraphics;
import processing.event.MouseEvent;
import com.symmetrylabs.slstudio.cue.UICuePanel;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.LXModulationEngine;
import heronarts.lx.audio.BandGate;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.MacroKnobs;
import heronarts.lx.modulator.MultiStageEnvelope;
import heronarts.lx.modulator.VariableLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXCompoundModulation;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXTriggerModulation;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UISlider;
import heronarts.p3lx.ui.studio.UIPane;
import heronarts.p3lx.ui.studio.midi.UIMidiInputs;
import heronarts.p3lx.ui.studio.midi.UIMidiMappings;
import heronarts.p3lx.ui.studio.midi.UIMidiSurfaces;
import heronarts.p3lx.ui.studio.modulation.UIComponentModulator;
import heronarts.p3lx.ui.studio.modulation.UIModulator;
import heronarts.p3lx.ui.studio.osc.UIOscManager;

import com.symmetrylabs.util.artnet.ui.UIArtNetConfig;
import com.symmetrylabs.util.dmx.ui.UIDmxMappings;
import com.symmetrylabs.shows.tree.*;
import com.symmetrylabs.shows.tree.ui.*;
import com.symmetrylabs.slstudio.SLStudio;


public class UIOverriddenRightPane extends UIPane {

    private final LX lx;
    private final UI ui;

    public final UI2dScrollContext model;
    public final UI2dScrollContext modulation;
    public final UI2dScrollContext midi;
    public final UI2dScrollContext utility;

    private final CompoundParameter backgroundLightParam = new CompoundParameter("background", 0.09, 0, 1);

    public static final int PADDING = 4;
    public static final int WIDTH = 522;
    private static final int ADD_BUTTON_WIDTH = 38;

    private int lfoCount = 1;
    private int envCount = 1;
    private int beatCount = 1;
    private int macroCount = 1;

    /** Tracks which tab (section) is currently active so the scrollbar knows which context to scroll. */
    private int activeSectionIndex = 0;
    private PaneScrollBar paneScrollBar;

    // UIPane geometry constants (mirrored from UIPane source)
    private static final int SB_MARGIN   = 8;   // UIPane.MARGIN
    private static final int SB_INSET_Y  = 32;  // UIPane.MARGIN(8) + UIPane.INSET_Y(24)
    private static final int SB_PADDING  = 6;   // UIPane.PADDING
    private static final int SB_BAR_W    = 6;
    private static final int SB_BAR_PADB = 4;   // extra bottom pad

    public UIOverriddenRightPane(UI ui, final LX lx) {
        super(ui, lx, new String[]{"MODULATE", "EXTERN I/O", "MODEL", "UTILITY"}, ui.getWidth() - WIDTH, WIDTH);
        this.ui = ui;
        this.lx = lx;
        this.modulation = this.sections[0];
        this.midi = this.sections[1];
        this.model = this.sections[2];
        this.utility = this.sections[3];

        buildModelUI();
        buildUtilityUI();
        buildMidiUI();
        buildModulationUI();

        // Scrollbar: a narrow strip on the far-right edge of the pane, sitting inside
        // the right margin gap (between the inset content and the pane border).
        // This keeps it clear of the grp button column which ends at the inset's right edge.
        float sbX = WIDTH - SB_BAR_W - 2;
        paneScrollBar = new PaneScrollBar(sbX, 0, SB_BAR_W, ui.getHeight());
        paneScrollBar.addToContainer(this);
    }

    /** Resyncs active section and redraws scrollbar — called from mouse handlers and wheel. */
    private void syncScrollBar() {
        for (int i = 0; i < sections.length; i++) {
            if (sections[i].visible.isOn()) {
                activeSectionIndex = i;
                break;
            }
        }
        if (paneScrollBar != null) paneScrollBar.redraw();
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
        super.onMousePressed(mouseEvent, mx, my);
        syncScrollBar();
    }

    @Override
    public void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
        super.onMouseWheel(mouseEvent, mx, my, delta);
        if (paneScrollBar != null) paneScrollBar.redraw();
    }

    /**
     * Thin draggable scrollbar drawn as a narrow strip on the right edge of the pane.
     * Coordinates in onDraw and mouse handlers are all in this component's local space
     * (origin at the component's top-left corner).
     */
    private class PaneScrollBar extends UI2dComponent {
        private static final int TRACK_COLOR      = 0x44FFFFFF;
        private static final int THUMB_COLOR      = 0xAABBBBBB;
        private static final int THUMB_HOVER_COLOR = 0xCCDDDDDD;

        private boolean dragging      = false;
        private boolean hovering      = false;
        private float   dragStartY    = 0;
        private float   dragStartScroll = 0;

        PaneScrollBar(float x, float y, float w, float h) {
            super(x, y, w, h);
        }

        private UI2dScrollContext activeSection() {
            if (activeSectionIndex < 0 || activeSectionIndex >= sections.length) return null;
            return sections[activeSectionIndex];
        }

        private float viewH()    { UI2dScrollContext s = activeSection(); return s == null ? 1 : s.getHeight(); }
        private float contentH() { UI2dScrollContext s = activeSection(); return s == null ? 1 : Math.max(s.getScrollHeight(), viewH()); }

        /** Track top in local coords (offset from top of this component). */
        private float trackTop() { return SB_INSET_Y + SB_PADDING; }
        /** Track bottom in local coords — aligned to the bottom of the section viewport. */
        private float trackBot() {
            UI2dScrollContext s = activeSection();
            float viewBottom = (s != null) ? SB_INSET_Y + SB_PADDING + s.getHeight() : this.height;
            return viewBottom - SB_PADDING - SB_BAR_PADB;
        }
        /** Track height. */
        private float trackH()   { return Math.max(0, trackBot() - trackTop()); }

        private float thumbH(float trackH) {
            return Math.max(20f, trackH * (viewH() / contentH()));
        }

        private float thumbTopY(float trackH) {
            UI2dScrollContext s = activeSection();
            if (s == null) return 0;
            float scrollable = contentH() - viewH();
            if (scrollable <= 0) return 0;
            float frac = (-s.getScrollY()) / scrollable;
            return frac * (trackH - thumbH(trackH));
        }

        @Override
        public void onDraw(UI ui, PGraphics pg) {
            float view = viewH();
            float content = contentH();
            if (content <= view + 1) return;  // nothing to scroll

            float tH = trackH();
            if (tH <= 0) return;

            float tTop = trackTop();
            // In local space x=0 is the left edge of this component (= the bar itself)
            pg.noStroke();
            pg.fill(TRACK_COLOR);
            pg.rect(0, tTop, SB_BAR_W, tH, 3);

            float th = thumbH(tH);
            float ty = tTop + thumbTopY(tH);
            pg.fill(hovering || dragging ? THUMB_HOVER_COLOR : THUMB_COLOR);
            pg.rect(0, ty, SB_BAR_W, th, 3);
        }

        @Override
        public void onMousePressed(MouseEvent e, float mx, float my) {
            float view = viewH();
            float content = contentH();
            if (content <= view + 1) return;

            float tH  = trackH();
            float tTop = trackTop();
            float tBot = trackBot();
            if (my < tTop || my > tBot) return;

            UI2dScrollContext s = activeSection();
            if (s == null) return;

            float th = thumbH(tH);
            float ty = tTop + thumbTopY(tH);

            if (my >= ty && my <= ty + th) {
                dragging = true;
                dragStartY = my;
                dragStartScroll = s.getScrollY();
            } else {
                float clickFrac = Math.max(0, Math.min(1, (my - tTop - th / 2f) / (tH - th)));
                s.setScrollY(-(content - view) * clickFrac);
                redraw();
            }
        }

        @Override
        public void onMouseReleased(MouseEvent e, float mx, float my) {
            dragging = false;
        }

        @Override
        public void onMouseDragged(MouseEvent e, float mx, float my, float dx, float dy) {
            if (!dragging) return;
            UI2dScrollContext s = activeSection();
            if (s == null) return;
            float tH  = trackH();
            float th  = thumbH(tH);
            float scrollable = contentH() - viewH();
            float dragFrac = (my - dragStartY) / (tH - th);
            s.setScrollY(dragStartScroll - dragFrac * scrollable);
            redraw();
        }

        @Override
        public void onMouseMoved(MouseEvent e, float mx, float my) {
            float tTop = trackTop();
            float tBot = trackBot();
            boolean now = (my >= tTop && my <= tBot);
            if (now != hovering) { hovering = now; redraw(); }
        }

        @Override
        public void onMouseOut(MouseEvent e) {
            if (hovering) { hovering = false; redraw(); }
        }

        @Override
        protected void onResize() {
            redraw();
        }
    }

    private void buildModelUI() {
    }

    private void buildUtilityUI() {
        new UIOfflineRender(this.ui, this.lx, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);

        ui.setBackgroundColor(LXColor.gray(backgroundLightParam.getValue() * 100));
        new UISlider(UISlider.Direction.HORIZONTAL, PADDING, PADDING, utility.getWidth() - 2 * PADDING, 20)
            .setParameter(backgroundLightParam)
            .addToContainer(utility);
        backgroundLightParam.addListener(p -> {
            ui.setBackgroundColor(LXColor.gray(p.getValue() * 100));
        });

        new UICuePanel(lx, ui, 0, 0, this.utility.getContentWidth()).addToContainer(utility);
    }

    private void buildPerformanceUI() {
    }

    private void buildMidiUI() {
        new UIOscManager(this.ui, this.lx, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
        new UIArtNetConfig(this.ui, this.lx.engine.artNet, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
        new UIDmxMappings(this.ui, this.lx, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
        new UIMidiSurfaces(this.ui, this.lx.engine.midi, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
        new UIMidiInputs(this.ui, this.lx.engine.midi, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
        new UIMidiMappings(this.ui, this.lx, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
    }

    private void buildModulationUI() {
        this.modulation.setArrowKeyFocus(UI2dContainer.ArrowKeyFocus.VERTICAL);

        UI2dContainer bar = (UI2dContainer) new UI2dContainer(0, 0, this.modulation.getContentWidth(), 22) {
            @Override
            public void onDraw(UI ui, PGraphics pg) {
                pg.stroke(0xff333333);
                pg.line(0, this.height - 1, UIOverriddenRightPane.this.width - 1, this.height - 1);
            }
        }
            .setLayout(UI2dContainer.Layout.HORIZONTAL)
            .setChildMargin(4)
            .addToContainer(this.modulation);

        new UIButton(0, 0, ADD_BUTTON_WIDTH, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    VariableLFO lfo = new VariableLFO("LFO " + lfoCount++);
                    lx.engine.modulation.addModulator(lfo);
                    lfo.start();
                }
            }
        }
            .setLabel("LFO")
            .setMomentary(true)
            .setInactiveColor(ui.theme.getDeviceBackgroundColor())
            .setBorderRounding(4)
            .setDescription("Add a new LFO to the modulation engine")
            .addToContainer(bar);

        new UIButton(0, 0, ADD_BUTTON_WIDTH, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    MultiStageEnvelope envelope = new MultiStageEnvelope("Env " + envCount++);
                    lx.engine.modulation.addModulator(envelope);
                    envelope.start();
                }
            }
        }
            .setLabel("Env")
            .setMomentary(true)
            .setInactiveColor(ui.theme.getDeviceBackgroundColor())
            .setBorderRounding(4)
            .setDescription("Add a new envelope to the modulation engine")
            .addToContainer(bar);

        new UIButton(0, 0, ADD_BUTTON_WIDTH, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    BandGate beatDetect = new BandGate("Beat " + beatCount++, lx);
                    lx.engine.modulation.addModulator(beatDetect);
                    beatDetect.start();
                }
            }
        }
            .setLabel("Beat")
            .setMomentary(true)
            .setInactiveColor(ui.theme.getDeviceBackgroundColor())
            .setBorderRounding(4)
            .setDescription("Add a new Beat detector to the modulation engine")
            .addToContainer(bar);

        new UIButton(0, 0, ADD_BUTTON_WIDTH, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    MacroKnobs macroKnobs = new MacroKnobs("Macro " + macroCount++);
                    lx.engine.modulation.addModulator(macroKnobs);
                    macroKnobs.start();
                }
            }
        }
            .setLabel("Macro")
            .setMomentary(true)
            .setInactiveColor(ui.theme.getDeviceBackgroundColor())
            .setBorderRounding(4)
            .setDescription("Add a new Beat detector to the modulation engine")
            .addToContainer(bar);

        final UIButton triggerButton = (UIButton) new UIButton(0, 0, 16, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    lx.engine.mapping.setMode(LXMappingEngine.Mode.TRIGGER_SOURCE);
                } else if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.TRIGGER_SOURCE) {
                    lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                }
            }
        }
            .setIcon(ui.theme.iconTriggerSource)
            .setInactiveColor(ui.theme.getDeviceBackgroundColor())
            .setBorderRounding(4)
            .setDescription("Add a new trigger mapping to the modulation engine")
            .addToContainer(bar);

        final UIButton mapButton = (UIButton) new UIButton(0, 0, 24, 16) {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    lx.engine.mapping.setMode(LXMappingEngine.Mode.MODULATION_SOURCE);
                } else if (lx.engine.mapping.getMode() == LXMappingEngine.Mode.MODULATION_SOURCE) {
                    lx.engine.mapping.setMode(LXMappingEngine.Mode.OFF);
                }
            }
        }
            .setIcon(ui.theme.iconMap)
            .setInactiveColor(ui.theme.getDeviceBackgroundColor())
            .setBorderRounding(4)
            .setDescription("Add a new parameter mapping to the modulation engine")
            .addToContainer(bar);

        lx.engine.mapping.mode.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                if (lx.engine.mapping.getMode() != LXMappingEngine.Mode.MODULATION_SOURCE) {
                    mapButton.setActive(false);
                }
                if (lx.engine.mapping.getMode() != LXMappingEngine.Mode.TRIGGER_SOURCE) {
                    triggerButton.setActive(false);
                }
            }
        });

        for (LXModulator modulator : lx.engine.modulation.getModulators()) {
            addModulator(modulator);
        }
        for (LXCompoundModulation modulation : lx.engine.modulation.modulations) {
            addModulation(modulation);
        }
        for (LXTriggerModulation trigger : lx.engine.modulation.triggers) {
            addTrigger(trigger);
        }

        lx.engine.modulation.addListener(new LXModulationEngine.Listener() {
            public void modulatorAdded(LXModulationEngine engine, LXModulator modulator) {
                addModulator(modulator);
            }

            public void modulatorRemoved(LXModulationEngine engine, LXModulator modulator) {
                removeModulator(modulator);
            }

            public void modulationAdded(LXModulationEngine engine, LXCompoundModulation modulation) {
                addModulation(modulation);
            }

            public void modulationRemoved(LXModulationEngine engine, LXCompoundModulation modulation) {
                removeModulation(modulation);
            }

            public void triggerAdded(LXModulationEngine engine, LXTriggerModulation trigger) {
                addTrigger(trigger);
            }

            public void triggerRemoved(LXModulationEngine engine, LXTriggerModulation trigger) {
                removeTrigger(trigger);
            }
        });
    }

    private UIModulator findModulator(LXParameter parameter) {
        return findModulator(parameter, false);
    }

    private UIModulator findModulator(LXParameter parameter, boolean create) {
        for (UIObject child : this.modulation) {
            if (child instanceof UIModulator) {
                UIModulator uiModulator = (UIModulator) child;
                if (uiModulator.component == parameter || uiModulator.component == parameter.getComponent()) {
                    return uiModulator;
                }
            }
        }
        if (create) {
            LXComponent component = parameter.getComponent();
            if (component == this.lx.engine.modulation && (parameter instanceof LXComponent)) {
                component = (LXComponent) parameter;
            }
            return (UIModulator) new UIComponentModulator(
                this.ui,
                this.lx,
                component,
                0,
                0,
                this.modulation.getContentWidth()
            ).addToContainer(this.modulation, 1);
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void addModulator(LXModulator modulator) {
        UIModulator.Factory uiFactory = this.ui.registry.getModulatorUIFactory(modulator);
        if (uiFactory == null) {
            System.err.println("No UI class registered for modulator type: " + modulator.getClass().getName());
        } else {
            uiFactory.buildUI(this.ui, this.lx, modulator, 0, 0, this.modulation.getContentWidth())
                .addToContainer(this.modulation);
        }
    }

    private void removeModulator(LXModulator modulator) {
        UIModulator uiModulator = findModulator(modulator);
        if (uiModulator != null) {
            uiModulator.removeFromContainer();
        }
    }

    private void addModulation(LXCompoundModulation modulation) {
        findModulator(modulation.source, true).addModulation(modulation);
    }

    private void removeModulation(LXCompoundModulation modulation) {
        UIModulator uiModulator = findModulator(modulation.source);
        if (uiModulator != null) {
            uiModulator.removeModulation(modulation);
        }
    }

    private void addTrigger(LXTriggerModulation trigger) {
        findModulator(trigger.source, true).addTrigger(trigger);
    }

    private void removeTrigger(LXTriggerModulation trigger) {
        UIModulator uiModulator = findModulator(trigger.source);
        if (uiModulator != null) {
            uiModulator.removeTrigger(trigger);
        }
    }
}
