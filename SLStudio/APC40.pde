

class APC40Listener extends LXComponent {
  LXMidiRemote remote;
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
    void aftertouchReceived(MidiAftertouch aftertouch){
      // println("AFTER", aftertouch.getChannel());
    } 
    void  controlChangeReceived(MidiControlChange cc) {
      if (cc.getCC() == 47) {
        int v = cc.getValue();
        int diff = v > 50 ? -5 : 5;
        float raw = performanceManager.lColor.getValuef() + diff;
        float mod = raw < 0 ? 360 - raw : raw % 360;
        performanceManager.lColor.setValue(mod);
      }
      // println("CC", cc.getCC(), cc.getValue());
    }
    void  noteOffReceived(MidiNote note) {
      // println("NOTE OFF", note.getPitch(), note.getVelocity());
    }
    void  noteOnReceived(MidiNoteOn note) {
      if (note.getPitch() == 94) {
        println("UP");
        int i = performanceManager.getWindowIndex(0);
        performanceManager.windows[i].channel.goPrev();
      }
      if (note.getPitch() == 95) {
        println("DOWN");
        int i = performanceManager.getWindowIndex(0);
        performanceManager.windows[i].channel.goNext();
      }
      // println("NOTE ON", note.getPitch(), note.getVelocity());
    }
    void  pitchBendReceived(MidiPitchBend pitchBend) {
      // println("PB", pitchBend.getChannel());
    }
    void  programChangeReceived(MidiProgramChange pc) {
      // println("PC", pc.getProgram());
    }
  }

  void bind() {
    LXMidiInput chosenInput = lx.engine.midi.matchInput("APC40");
    LXMidiOutput chosenOutput = lx.engine.midi.matchOutput("APC40");

    if (chosenInput == null || chosenOutput == null) {
      return;
    }

    chosenInput.open();
    chosenOutput.open();

    remote = new LXMidiRemote(chosenInput, chosenOutput);
    remote.logEvents(true);

    hasRemote.setValue(true);

    chosenInput.addListener(new Listener());
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

  class Listener implements LXMidiListener {
    void aftertouchReceived(MidiAftertouch aftertouch){
      println("AFTER", aftertouch.getChannel());
    } 
    void  controlChangeReceived(MidiControlChange cc) {
      println("CC", cc.getCC(), cc.getValue());
    }
    void  noteOffReceived(MidiNote note) {
      println("NOTE OFF", note.getPitch(), note.getVelocity());
    }
    void  noteOnReceived(MidiNoteOn note) {
      println("NOTE ON", note.getPitch(), note.getVelocity());
    }
    void  pitchBendReceived(MidiPitchBend pitchBend) {
      println("PB", pitchBend.getChannel());
    }
    void  programChangeReceived(MidiProgramChange pc) {
      println("PC", pc.getProgram());
    }
  }

  void bind() {
    LXMidiInput chosenInput = lx.engine.midi.matchInput("Faderfox");
    LXMidiOutput chosenOutput = lx.engine.midi.matchOutput("Faderfox");

    if (chosenInput == null || chosenOutput == null) {
      return;
    }

    chosenInput.open();
    chosenOutput.open();

    // lx.engine.midi.addListener(new Listener());

    remote = new LXMidiRemote(chosenInput, chosenOutput);

    remote.bindController(lx.engine.crossfader, 112);
    remote.bindController(lx.engine.masterChannel.getEffect("Blur").getParameter("amount"), 32);
    remote.bindController(lx.engine.output.brightness, 33);
    remote.bindController(lx.engine.speed, 34);
  }
}