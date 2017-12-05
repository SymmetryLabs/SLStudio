public class EyesPattern extends SLPattern {

  public EyesPattern(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (Sun sun : model.suns) {
      int i = 0;

      int[] colors = {
        LXColor.RED, 
        LXColor.GREEN, 
        LXColor.BLUE, 
        LXColor.BLACK
      };

      for (Slice slice : sun.slices) {
        setColor(slice, colors[i++]);
      }
    }
  }
}

public class SeaboardPattern extends SLPattern {

  private int[] KEY_ORDER = {
    48,
    50,
    52,
    53,
    55,
    57,
    59,
    60,
    62,
    64,
    65,
    67,
    69,
    71,
    72,
  };

  private int[] SUN_ORDER = {
    0,
    1,
    10,
    2,
    9,
    3,
    4,
    8,
    5,
    6,
    7,
  };

  public final CompoundParameter attack = (CompoundParameter)
      new CompoundParameter("Attack", 50, 25, 1000)
      .setExponent(2)
      .setUnits(LXParameter.Units.MILLISECONDS)
      .setDescription("Sets the attack time of the notes");
      
    public final CompoundParameter decay = (CompoundParameter)
      new CompoundParameter("Decay", 500, 50, 3000)
      .setExponent(2)
      .setUnits(LXParameter.Units.MILLISECONDS)
      .setDescription("Sets the decay time of the notes");

  public class Press {
    int channel;
    int index;
    
    NormalizedParameter pressure = new NormalizedParameter("Pressure");
    NormalizedParameter slide = new NormalizedParameter("Slide");
    BoundedParameter glide = new BoundedParameter("Glide", 0, -1, 1);

    LXModulator pressureDamped = startModulator(new DampedParameter(this.pressure, .3, 1, .1));
    LXModulator slideDamped = startModulator(new DampedParameter(this.slide, .3, 1, .1));
    LXModulator glideDamped = startModulator(new DampedParameter(this.glide, .3, 1, .1));

    ADEnvelope envelope = new ADEnvelope("Note", 0, 100, attack, decay);


    public Press(int index, float velocity) {
      this.index = index;

      pressureDamped.setValue(velocity);
      slideDamped.setValue(0);
      glideDamped.setValue(0);

      pressure.setValue(velocity);
      slide.setValue(0);
      glide.setValue(0);

      addModulator(envelope);      

    }
  }



  HashMap<Integer, Integer> pitchToIndex;
  HashMap<Integer, Press> channelToPress;
  ArrayList<Press> presses;

  public SeaboardPattern(LX lx) {
    super(lx);

    pitchToIndex = new HashMap();
    channelToPress = new HashMap();
    presses = new ArrayList();

    for (int i = 0; i < 11; i++) {
      presses.add(new Press(i, 0));
    }

    for (int i = 0; i < KEY_ORDER.length; i++) {
      pitchToIndex.put(KEY_ORDER[i], i);
    }
  }

  public void run(double deltaMs) {
    for (int i = 0; i < model.suns.size(); i++) {
      Sun sun = model.suns.get(i);
      int s_i = SUN_ORDER[i];
      Press p = presses.get(s_i);
      setColor(sun, LXColor.gray(100 * p.pressure.getValuef()));
    }
  }

  

  @Override
      public void noteOnReceived(MidiNoteOn note) {
        int channel = note.getChannel();
        int pitch = note.getPitch();
        float velocity = note.getVelocity() / 127.f;

        if (!pitchToIndex.containsKey(pitch)) {
          return;
        }
        int index = pitchToIndex.get(pitch);
        if (index >= presses.size()) return;
        Press p = presses.get(index);
        channelToPress.put(channel, p);
        p.envelope.engage.setValue(true);
    }

    @Override
      public void noteOffReceived(MidiNote note) {
        int channel = note.getChannel();
        int pitch = note.getPitch();
        float velocity = note.getVelocity() / 127.f;

        if (!pitchToIndex.containsKey(pitch)) {
          return;
        }
        int index = pitchToIndex.get(pitch);
        if (index >= presses.size()) return;
        Press p = presses.get(index);
        p.pressure.setValue(0);
        channelToPress.remove(channel);
        p.envelope.engage.setValue(false);
    }

    @Override
      void aftertouchReceived(MidiAftertouch aftertouch) {
        int channel = aftertouch.getChannel();
        float pressure = aftertouch.getAftertouch() / 127.f;
        if (channelToPress.containsKey(channel)) {
          Press p = channelToPress.get(channel);
          if (!p.envelope.isRunning()) {
            p.pressure.setValue(pressure);
          }
        }

    }

    @Override
      public void pitchBendReceived(MidiPitchBend pb) {
        int channel = pb.getChannel();
        // pb.getPitch();
        float glide = (float)pb.getPitchBend();
        if (channelToPress.containsKey(channel)) {
          channelToPress.get(channel).glide.setValue(glide);
        }


    }

    @Override
      public void controlChangeReceived(MidiControlChange cc) {
      if (cc.getCC() == 74) {
        int channel = cc.getChannel();
        // cc.getPitch();
        float slide = (float)cc.getNormalized();
        if (channelToPress.containsKey(channel)) {
          channelToPress.get(channel).slide.setValue(slide);
        }
      }
    }
}