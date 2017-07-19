import processing.core.PGraphics;

public class UIOverriddenRightPane extends UIPane {

  private final LX lx;
  private final UI ui;

  public final UI2dScrollContext utility;
  public final UI2dScrollContext modulation;
  public final UI2dScrollContext midi;
  public final UI2dScrollContext wearables; 

  public static final int PADDING = 4;
  public static final int WIDTH = 282;
  private static final int ADD_BUTTON_WIDTH = 40;

  private int lfoCount = 1;
  private int envCount = 1;
  private int beatCount = 1;
  private int macroCount = 1;

  public UIOverriddenRightPane(UI ui, final LX lx) {
    super(ui, lx, new String[] { "MODULATE", "OSC + MIDI", "UTILITY", "WEARABLES" }, ui.getWidth() - WIDTH, WIDTH);
    this.ui = ui;
    this.lx = lx;
    this.modulation = this.sections[0];
    this.midi = this.sections[1];
    this.utility = this.sections[2];
    this.wearables = this.sections[3];

    buildUtilityUI();
    buildMidiUI();
    buildModulationUI();
    buildWearablesUI();
  }

  private void buildWearablesUI() {
    new DeviceSection(lx, ui, 0, 0, wearables.getContentWidth(), wearables).addToContainer(wearables);

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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
    .setBorderRounding(4)
    .setDescription("Add a new parameter mapping to the modulation engine")
    .addToContainer(wearables);

    lx.engine.mapping.mode.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (lx.engine.mapping.getMode() != LXMappingEngine.Mode.MODULATION_SOURCE) {
          mapButton.setActive(false);
        }
      }
    });
    // DeviceKnobs deviceKnobs = new DeviceKnobs("Wearable Signals");
    // lx.engine.modulation.addModulator(deviceKnobs);
    // deviceKnobs.start();
  }

  private void buildUtilityUI() {
    //new UIAudioAnalyzer(this.lx, this.ui, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);

    new UIOutputs(lx, ui, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);

    if (((SLModel)model).cubes.size() > 0)
      new UIMapping(lx, ui, 0, 0, this.utility.getContentWidth()).addToContainer(this.utility);

    // TODO: list wearables ("devices")
  }

  private void buildMidiUI() {
    new UIOscManager(ui, lx, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
    new UIMidiSurfaces(ui, lx.engine.midi, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
    new UIMidiInputs(ui, lx.engine.midi, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
    new UIMidiMappings(ui, lx, 0, 0, this.midi.getContentWidth()).addToContainer(this.midi);
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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
    .setBorderRounding(4)
    .setDescription("Map audio analyzation properties")
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
    .setInactiveColor(ui.theme.getWindowBackgroundColor())
    .setBorderRounding(4)
    .setDescription("Add a new parameter mapping to the modulation engine")
    .addToContainer(bar);

    lx.engine.mapping.mode.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (lx.engine.mapping.getMode() != LXMappingEngine.Mode.MODULATION_SOURCE) {
          mapButton.setActive(false);
        }
      }
    });

    for (LXModulator modulator : lx.engine.modulation.getModulators()) {
      addModulator(modulator);
    }
    for (LXCompoundModulation modulation : lx.engine.modulation.modulations) {
      addModulation(modulation);
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
    for (UIObject child : this.modulation) {
      if (child instanceof UIModulator) {
        UIModulator uiModulator = (UIModulator) child;
        if (uiModulator.parameter == parameter || uiModulator.parameter == parameter.getComponent()) {
          return uiModulator;
        }
      }
    }
    return null;
  }

  private void addModulator(LXModulator modulator) {
    if (modulator instanceof VariableLFO) {
      new UIVariableLFO(this.ui, this.lx, (VariableLFO) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    } else if (modulator instanceof MultiStageEnvelope) {
      new UIMultiStageEnvelope(this.ui, this.lx, (MultiStageEnvelope) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    } else if (modulator instanceof BandGate) {
      new UIBandGate(this.ui, this.lx, (BandGate) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    } else if (modulator instanceof MacroKnobs) {
      new UIMacroKnobs(this.ui, this.lx, (MacroKnobs) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    } else if (modulator instanceof AudioAnalyzerKnobs) {
      new UIAudioAnalyzerKnobs(this.ui, this.lx, (AudioAnalyzerKnobs) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    } 
    // else if (modulator instanceof DeviceKnobs) {
    //   new UIDeviceKnobs(this.ui, this.lx, (DeviceKnobs) modulator, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    // } 
    else {
      System.err.println("No UI available for modulator type: " + modulator.getClass().getName());
    }
  }

  private void removeModulator(LXModulator modulator) {
    UIModulator uiModulator = findModulator(modulator);
    if (uiModulator != null) {
      uiModulator.removeFromContainer();
    }
  }

  private void addModulation(LXCompoundModulation modulation) {
    UIModulator uiModulator = findModulator(modulation.source);
    if (uiModulator == null) {
      uiModulator = (UIModulator) new UIParameterModulator(this.ui, this.lx, modulation.source, 0, 0, this.modulation.getContentWidth()).addToContainer(this.modulation, 1);
    }
    uiModulator.addModulation(modulation);
  }

  private void removeModulation(LXCompoundModulation modulation) {
    UIModulator uiModulator = findModulator(modulation.source);
    if (uiModulator != null) {
      uiModulator.removeModulation(modulation);
    }
  }

  private UIModulator findModulator(LXTriggerModulation trigger) {
    LXComponent source = trigger.source.getComponent();
    if (source instanceof LXModulator) {
      return findModulator((LXModulator) source);
    }
    return null;
  }

  private void addTrigger(LXTriggerModulation trigger) {
    UIModulator uiModulator = findModulator(trigger);
    if (uiModulator != null) {
      uiModulator.addTrigger(trigger);
    }
  }

  private void removeTrigger(LXTriggerModulation trigger) {
    UIModulator uiModulator = findModulator(trigger);
    if (uiModulator != null) {
      uiModulator.removeTrigger(trigger);
    }
  }
}