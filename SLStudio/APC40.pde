

class APC40Listener extends LXComponent {
  LXMidiRemote remotes[] = new LXMidiRemote[2];

  public BooleanParameter hasRemote = new BooleanParameter("hasRemote", false);

  public APC40Listener(LX lx) {
    super(lx);

    lx.engine.midi.whenReady(new Runnable() {
      public void run() {
        bind();

      }
    });
  }

  class Listener implements LXMidiListener {
    BoundedParameter col;
    int side;

    public Listener(BoundedParameter col, int side) {
      this.col = col;
      this.side = side;
    }

    void aftertouchReceived(MidiAftertouch aftertouch){
      // println("AFTER", aftertouch.getChannel());
    } 

    void  controlChangeReceived(MidiControlChange cc) {
      if (cc.getCC() == 47) {
        int v = cc.getValue();
        int diff = v > 50 ? -5 : 5;
        float raw = col.getValuef() + diff;
        float mod = raw < 0 ? 360 - raw : raw % 360;
        col.setValue(mod);
      }
    }

    void  noteOffReceived(MidiNote note) {
      // println("NOTE OFF", note.getPitch(), note.getVelocity());
    }

    void  noteOnReceived(MidiNoteOn note) {
      if (note.getPitch() == 94) {
        println("UP");
        int i = performanceManager.getWindowIndex(side);
        performanceManager.windows[i].channel.goPrev();
      }
      if (note.getPitch() == 95) {
        println("DOWN");
        int i = performanceManager.getWindowIndex(side);
        performanceManager.windows[i].channel.goNext();
      }
    }

    void  pitchBendReceived(MidiPitchBend pitchBend) {
      // println("PB", pitchBend.getChannel());
    }
    void  programChangeReceived(MidiProgramChange pc) {
      // println("PC", pc.getProgram());
    }
  }

  void bind() {
    for (final LXMidiSurface s : lx.engine.midi.surfaces) {
      s.enabled.setValue(false);
      s.enabled.addListener(new LXParameterListener() {
          public void onParameterChanged(LXParameter parameter) {
            if (s.enabled.isOn()) {
              s.enabled.setValue(false);
            }
          }
      });
    }

    List<LXMidiInput> inputs = lx.engine.midi.getInputs();
    List<LXMidiOutput> outputs = lx.engine.midi.getOutputs();
    ArrayList<LXMidiInput> apcInputs = new ArrayList();
    ArrayList<LXMidiOutput> apcOutputs = new ArrayList();
    

    for (LXMidiInput i : inputs) {
      if (i.getName().contains("APC40")) {
        apcInputs.add(i);
      }
    }

    for (LXMidiOutput o : outputs) {
      if (o.getName().contains("APC40")) {
        apcOutputs.add(o);
      }
    }

    int n = min(apcInputs.size(), apcOutputs.size(), 2);

    for (int i = 0; i < n; i++) {
      LXMidiInput input = apcInputs.get(i);
      LXMidiOutput output = apcOutputs.get(i);

      input.open();
      output.open();

      remotes[i] = new LXMidiRemote(input, output);
      remotes[i].logEvents(true);

      if (i == 0) {
        input.addListener(new Listener(performanceManager.lColor, 0));
        remotes[i].sendNoteOn(0, 32, 127);
      } else {
        input.addListener(new Listener(performanceManager.rColor, 1));
        remotes[i].sendNoteOn(0, 33, 127);
      }
    }

    if (n > 0) {
      hasRemote.setValue(true);
    }
  }
}

class FoxListener extends LXComponent {
  LXMidiRemote remote;

  public FoxListener(LX lx) {
    super(lx);

    lx.engine.midi.whenReady(new Runnable() {
      public void run() {
        bind();
      }
    });
  }

  void bind() {
    LXMidiInput chosenInput = lx.engine.midi.matchInput("Faderfox");
    LXMidiOutput chosenOutput = lx.engine.midi.matchOutput("Faderfox");

    if (chosenInput == null || chosenOutput == null) {
      return;
    }

    chosenInput.open();
    chosenOutput.open();

    remote = new LXMidiRemote(chosenInput, chosenOutput);

    final CompoundParameter slowDown = new CompoundParameter("slowDown", 0);
    slowDown.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter parameter) {
            float v = slowDown.getValuef();
            lx.engine.speed.setValue(map(v, 0, 1, 1.0, 0));
        }
    });

    remote.bindController(lx.engine.crossfader, 112);
    // remote.bindController(lx.engine.masterChannel.getEffect("Blur").getParameter("amount"), 32);
    // remote.bindController(lx.engine.output.brightness, 33);
    // remote.bindController(slowDown, 34);
    remote.bindController(slowDown, 32);
  }
}
