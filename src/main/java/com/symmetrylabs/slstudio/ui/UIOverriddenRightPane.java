package com.symmetrylabs.slstudio.ui;

import processing.core.PGraphics;
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
    public static final int WIDTH = 320;
    private static final int ADD_BUTTON_WIDTH = 38;

    private int lfoCount = 1;
    private int envCount = 1;
    private int beatCount = 1;
    private int macroCount = 1;

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
