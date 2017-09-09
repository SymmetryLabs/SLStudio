// public class SeaboardDebug extends SLPattern {
// 	public SeaboardDebug(LX lx) {
// 		super(lx);
// 	}

// 	@Override
// 	public void run(double deltaMs) {
// 		setColors(LXColor.BLACK);
// 	}

// 	@Override
//   	public void noteOnReceived(MidiNoteOn note) {
//   		println("ON", note.getChannel(), note.getVelocity());
//   	}

//   	@Override
// 	public void noteOffReceived(MidiNote note) {
// 		println("OFF", note.getChannel(), note.getVelocity());
// 	}

// 	@Override
//   	void aftertouchReceived(MidiAftertouch aftertouch) {
//   		println("AFTER", aftertouch.getChannel(), aftertouch.getAftertouch());
//   	}

//   	@Override
// 	public void pitchBendReceived(MidiPitchBend pb) {
// 		println("BEND", pb.getChannel(), pb.getPitchBend());
// 	}

// 	@Override
// 	public void controlChangeReceived(MidiControlChange cc) {
// 		if (cc.getCC() == 74) {
// 			println("CC", cc.getChannel(), cc.getNormalized());
// 		}
// 	}

public class Seaboard extends SLPattern {
  public String getAuthor() {
    return "Mark C. Slee";
  }
  
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
    
  public final CompoundParameter size =
    new CompoundParameter("Size", 2*FEET, 1*FEET, 8*FEET)
    .setDescription("Size of the notes");

  private final Note[] notes = new Note[16];
  private final Note[] channelToNote = new Note[16];
  private int noteRoundRobin = 0;
  
  public Seaboard(LX lx) {
    super(lx);
    addParameter("attack", this.attack);
    addParameter("decay", this.decay);
    addParameter("size", this.size);
    for (int i = 0; i < this.notes.length; ++i) {
      this.channelToNote[i] = this.notes[i] = new Note();
    }
  }
  
  private final float[] b = new float[model.cubes.size()];
  
  private final int NUM_KEYS = 25;
  private final int CENTER_KEY = 60;
  
  private final float SPREAD = model.yRange / (NUM_KEYS + 6);
  
  public void run(double deltaMs) {
    for (int i = 0; i < b.length; ++i) {
      b[i] = 0;
    }
    float size = this.size.getValuef();
    
    // Iterate over each note
    for (Note note : this.notes) {
      float level = note.levelDamped.getValuef() * note.envelope.getValuef();
      if (level > 0) {
        float falloff = 100 / (size * (1 + 2 * note.slideDamped.getValuef()));
        int i = 0;
        float yp = model.cy + (note.pitch - CENTER_KEY + note.bendDamped.getValuef() * Note.BEND_RANGE) * SPREAD;
        for (Cube cube : model.cubes) {
          b[i] += max(0, level - falloff * abs(yp - cube.points[0].y));
          ++i;
        }
      }
    }
    
    // Set colors for a
    int i = 0;
    for (Cube cube : model.cubes) {
      setColor(cube, LXColor.gray(min(100, b[i++])));
    }
  }
  
  class Note {
    
    static final float BEND_RANGE = 48;
        
    private final NormalizedParameter level = new NormalizedParameter("Level");
    private final NormalizedParameter slide = new NormalizedParameter("Slide");
    private final BoundedParameter bend = new BoundedParameter("Bend", 0, -1, 1);

    final LXModulator bendDamped = startModulator(new DampedParameter(this.bend, .3, 1, .1));
    final LXModulator slideDamped = startModulator(new DampedParameter(this.slide, .3, 1));
    final LXModulator levelDamped = startModulator(new DampedParameter(this.level, .4));

    final ADEnvelope envelope = new ADEnvelope("Note", 0, 100, attack, decay);
        
    int pitch;
    
    Note() {
      addModulator(envelope);      
    }
  }
    
  @Override
  public void noteOnReceived(MidiNoteOn note) {
    this.channelToNote[note.getChannel()] = this.notes[this.noteRoundRobin];
    this.noteRoundRobin = (this.noteRoundRobin + 1) % 16; 
    
    Note n = this.channelToNote[note.getChannel()];
    n.bend.setValue(0);
    n.bendDamped.setValue(0);
    n.slide.setValue(0);
    n.slideDamped.setValue(0);
    n.pitch = note.getPitch();
    n.level.setValue(note.getVelocity() / 127.f);
    n.levelDamped.setValue(note.getVelocity() / 127.f);
    n.envelope.engage.setValue(true);
  }
  
  @Override
  public void noteOffReceived(MidiNote note) {
    Note n = this.channelToNote[note.getChannel()];
    n.level.setValue(n.levelDamped.getValue());
    n.envelope.engage.setValue(false);
  }
  
  @Override
  void aftertouchReceived(MidiAftertouch aftertouch) {
    // Wait until note attack stage is done...
    Note n = this.channelToNote[aftertouch.getChannel()];
    if (!n.envelope.isRunning()) {
      n.level.setValue(aftertouch.getAftertouch() / 127.f);
    }
  }
  
  @Override
  public void pitchBendReceived(MidiPitchBend pb) {
    this.channelToNote[pb.getChannel()].bend.setValue(pb.getNormalized());
  }
  
  @Override
  public void controlChangeReceived(MidiControlChange cc) {
    if (cc.getCC() == 74) {
      this.channelToNote[cc.getChannel()].slide.setValue(cc.getNormalized());
    }
  }
}