// The following code was written by Mark Slee for the Tenere project,
// and only very slightly adapted by Raphael for the Winter Suns project.
// Thanks Mark for the wonderfully written code!
// Original: https://github.com/treeoftenere/Tenere/blob/3da77abb62bf1b552a3e7535b470f13cfdf0837e/Tenere/Midi.pde


public class ADSR {
  final CompoundParameter attack = (CompoundParameter)
    new CompoundParameter("Attack", 50, 25, 1000)
    .setExponent(2)
    .setUnits(LXParameter.Units.MILLISECONDS)
    .setDescription("Sets the attack time of the notes");
    
  final CompoundParameter decay = (CompoundParameter)
    new CompoundParameter("Decay", 500, 50, 3000)
    .setExponent(2)
    .setUnits(LXParameter.Units.MILLISECONDS)
    .setDescription("Sets the decay time of the notes");
    
  final CompoundParameter sustain = (CompoundParameter)
    new CompoundParameter("Sustain", .5)
    .setExponent(2)
    .setDescription("Sets the sustain level of the notes");    
    
  final CompoundParameter release = (CompoundParameter)
    new CompoundParameter("Release", 500, 50, 5000)
    .setExponent(2)
    .setUnits(LXParameter.Units.MILLISECONDS)
    .setDescription("Sets the decay time of the notes");
}

public class SeaboardSuns extends SLPattern {
  public String getAuthor() {
    return "Mark C. Slee (with slight modification by Raphael Palefsky-Smith)";
  }

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
    2,
    4,
    7,
    10,
    9,
    8,
    6,
    5,
    3,
    1,
    0
  };
  
  private final ADSR adsr = new ADSR();
  private final CompoundParameter attack = adsr.attack;
  private final CompoundParameter decay = adsr.decay;
  private final CompoundParameter sustain = adsr.sustain;
  private final CompoundParameter release = adsr.release;
    
  private final CompoundParameter velocityBrightness = new CompoundParameter("Vel>Brt", .5)
    .setDescription("Sets the amount of modulation from note velocity to brightness");

  private final CompoundParameter size = new CompoundParameter("Size", .5);
  private final CompoundParameter falloff = new CompoundParameter("Fallof", .5, 0, 2);
  private final BooleanParameter wiggle = new BooleanParameter("wiggle", false);

  
  public static final int NUM_NOTE_LAYERS = 11;
  private final NoteLayer[] notes = new NoteLayer[NUM_NOTE_LAYERS];
  private boolean damperDown = false;
  private final HashMap<Integer, Integer> pitchToIndex = new HashMap();
  private final HashMap<Integer, NoteLayer> channelToNote = new HashMap();

  
  public SeaboardSuns(LX lx) {
    super(lx);
    for (int i = 0; i < this.notes.length; ++i) {
      int si = SUN_ORDER[i];
      Sun sun = model.suns.get(si);
      addLayer(this.notes[i] = new NoteLayer(lx, sun));
    }
    addParameter("attack", this.attack);
    addParameter("decay", this.decay);
    addParameter("sustain", this.sustain);
    addParameter("release", this.release);
    addParameter("velocityBrightness", this.velocityBrightness);
    addParameter("size", this.size);
    addParameter("falloff", this.falloff);
    addParameter("wiggle", this.wiggle);

    for (int i = 0; i < KEY_ORDER.length; i++) {
      pitchToIndex.put(KEY_ORDER[i], i);
    }
  }
  
  public void run(double deltaMs) {
    setColors(#000000);
  }
  
  class NoteLayer extends LXLayer {
    
    private NormalizedParameter level = new NormalizedParameter("Level", 1); 
    private final BoundedParameter bend = new BoundedParameter("Bend", 0, -1, 1);
    private final ADSREnvelope envelope = new ADSREnvelope("Env", 0, level, attack, decay, sustain, release);  
    private boolean damper = false;
    private final LXModel layerModel;

    
    NoteLayer(LX lx, LXModel layerModel) {
      super(lx);
      addModulator(this.envelope);

      this.bend.setValue(0);

      this.layerModel = layerModel;
    }
    
    public void run(double deltaMs) {
      float bright = 100 * (float)this.envelope.getValue();
      if (!wiggle.getValueb()) {
        setColor(layerModel, LXColor.gray(bright));
        return;
      }
      float centerX = map(bend.getValuef(), -1, 1, layerModel.xMin, layerModel.xMax);
      if (damper) {
        centerX = layerModel.xMin + (layerModel.xRange / 2);
      }
      float span = size.getValuef() * layerModel.xRange;
      float fSpan = falloff.getValuef() * span;
      for (LXPoint p : layerModel.points) {
        float dist = abs(p.x - centerX);
        if (dist < span) {
          colors[p.index] = LXColor.gray(bright);
        } else {
          float v = bright * (fSpan - (dist - span)) / fSpan;
          colors[p.index] = LXColor.gray(max(0, v));
        }
      }
    }
  }

  private NoteLayer getNote(MidiNote note) {
    int pitch = note.getPitch();
    if (!pitchToIndex.containsKey(pitch)) {
      return null;
    }
    int index = pitchToIndex.get(pitch);
    if (index >= notes.length) {
      return null;
    }
    return notes[index];
  }
  
  @Override
  public void noteOnReceived(MidiNoteOn note) {
    NoteLayer noteLayer = getNote(note);
    if (noteLayer == null) return;
    noteLayer.level.setValue(lerp(1, note.getVelocity() / 127., this.velocityBrightness.getNormalizedf()));
    noteLayer.envelope.attack();
    noteLayer.damper = false;
    channelToNote.put(note.getChannel(), noteLayer);
  }
  
  @Override
  public void noteOffReceived(MidiNote note) {
    NoteLayer noteLayer = getNote(note);
    if (noteLayer == null) return;
    if (this.damperDown) {
      noteLayer.damper = true;
    } else {
      noteLayer.envelope.release();
    }
  }

  @Override
    public void pitchBendReceived(MidiPitchBend pb) {
      int p = pb.getPitchBend();
      float b = constrain(p / 60.f, -1.0, 1.0);
      NoteLayer nl = channelToNote.get(pb.getChannel());
      if (nl != null) {
        nl.bend.setValue(b);
      }
    }
  
  @Override
  public void controlChangeReceived(MidiControlChange cc) {
    if (cc.getCC() == MidiControlChange.DAMPER_PEDAL) {
      if (cc.getValue() > 0) {
        if (!this.damperDown) {
          this.damperDown = true;
          for (NoteLayer note : this.notes) {
            if (note.envelope.engage.isOn()) {
              note.damper = true;
            }
          }
        }
      } else {
        if (this.damperDown) {
          this.damperDown = false;
          for (NoteLayer note : this.notes) {
            if (note.damper) {
              note.envelope.engage.setValue(false);
            }
          }
        }
      }
    }
  }
}

public class Invert extends LXPattern {
  public Invert(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      int existing = colors[p.index];
      float h = LXColor.h(existing);
      float s = LXColor.s(existing);
      float b = LXColor.b(existing);
      colors[p.index] = LXColor.hsb(h, s, 100 - b);
    }
  }
}