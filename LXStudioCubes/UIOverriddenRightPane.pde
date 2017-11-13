public class UIOverriddenRightPane extends UIPane {

  private final LX lx;
  private final UI ui;

  public final UI2dScrollContext utility;
  public final UI2dScrollContext modulation;
  public final UI2dScrollContext midi;

  public static final int PADDING = 4;
  public static final int WIDTH = 284;
  private static final int ADD_BUTTON_WIDTH = 38;

  private int lfoCount = 1;
  private int envCount = 1;
  private int beatCount = 1;
  private int macroCount = 1;

  public UIOverriddenRightPane(UI ui, final LX lx) {
    super(ui, lx, new String[] { "MODULATION", "OSC + MIDI", "MAPPING" }, ui.getWidth() - WIDTH, WIDTH);
    this.ui = ui;
    this.lx = lx;
    this.modulation = this.sections[0];
    this.midi = this.sections[1];
    this.utility = this.sections[2];

    buildUtilityUI();
    buildMidiUI();
    buildModulationUI();
  }

  private void buildUtilityUI() {
    //new UIAudioAnalyzer(this.lx, this.ui, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);

    new UIOutputs(lx, ui, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);

    if (((SLModel)model).cubes.size() > 0)
      new UIMapping(ui, lx, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);
  }

  private void buildMidiUI() {
    new UIOscManager(this.ui, this.lx, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
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
        pg.line(0, this.height-1, UIOverriddenRightPane.this.width-1, this.height-1);
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

    new UIButton(0, 0, ADD_BUTTON_WIDTH, 16) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          AudioAnalyzerKnobs audioAnalyzerKnobs = new AudioAnalyzerKnobs("Audio Analyzation");
          lx.engine.modulation.addModulator(audioAnalyzerKnobs);
          audioAnalyzerKnobs.start();
        }
      }
    }
    .setLabel("Audio")
    .setMomentary(true)
    .setInactiveColor(ui.theme.getDeviceBackgroundColor())
    .setBorderRounding(4)
    .setDescription("Map audio analyzation properties")
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
      return (UIModulator) new UIComponentModulator(this.ui, this.lx, component, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    }
    return null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void addModulator(LXModulator modulator) {
    UIModulator.Factory uiFactory = this.ui.registry.getModulatorUIFactory(modulator);
    if (uiFactory == null) {
      System.err.println("No UI class registered for modulator type: " + modulator.getClass().getName());
    } else {
      uiFactory.buildUI(this.ui, this.lx, modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation);
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