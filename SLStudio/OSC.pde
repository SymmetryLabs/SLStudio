public class BlobTracker extends LXModulatorComponent implements LXOscListener {
  private static final int OSC_PORT = 4343;
  private List<Blob> blobs = new ArrayList<Blob>();
  private float mergeRadius = 30;  // inches
  private float maxSpeed = 240;  // inches per second
  private float maxDeltaSec = 0.5;  // don't track movement across large gaps in time
  private float blobY = 40;  // inches off the ground
  private long lastMessageMillis = 0;
  
  public BlobTracker(LX lx) {
    super(lx, "BlobTracker");
    try {
      lx.engine.osc.receiver(OSC_PORT).addListener(this);
    } catch (java.net.SocketException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void setMergeRadius(float radius) {
    mergeRadius = radius;
  }
  
  public void setMaxSpeed(float speed) {
    maxSpeed = speed;
  }
  
  public void setMaxDeltaSec(float deltaSec) {
    maxDeltaSec = deltaSec;
  }
  
  public void setBlobY(float y) {
    blobY = y;
  }
  
  public void oscMessage(OscMessage message) {
    int arg = 0;
    long millis = message.getInt(arg++);
    float deltaSec = (float) (millis - lastMessageMillis)*0.001;
    System.out.printf("millis: %d, deltaSec: %.3f\n", millis, deltaSec);
    lastMessageMillis = millis;
    
    List<Blob> newBlobs = new ArrayList<Blob>();
    int count = message.getInt(arg++);
    for (int i = 0; i < count; i++) {
      float x = message.getFloat(arg++);
      float y = message.getFloat(arg++);
      float size = message.getFloat(arg++);
      newBlobs.add(new Blob(new PVector(x, blobY, y), size));
    }
    mergeBlobs(newBlobs, mergeRadius);
    
    List<Blob> prevBlobs = blobs;    
    blobs = newBlobs;
    
    if (deltaSec < maxDeltaSec) {
      for (Blob b : blobs) {
        b.vel = estimateVelocity(b, prevBlobs, deltaSec, maxSpeed);
      }
    }
    
    println(blobs.size() + ":");
    for (Blob b : blobs) {
      println("  - " + b);
    }
  }
  
  /** Modifies a list of blobs in place, merging blobs within mergeRadius. */
  void mergeBlobs(List<Blob> blobs, float mergeRadius) {
    boolean mergeFound;
    do {
      mergeFound = false;
      search_for_merges:
      for (Blob b : blobs) {
        for (Blob other : blobs) {
          if (b != other && PVector.sub(b.pos, other.pos).mag() < mergeRadius) {
            blobs.remove(b);
            blobs.remove(other);
            blobs.add(new Blob(PVector.div(PVector.add(b.pos, other.pos), 2), b.size + other.size));
            mergeFound = true;
            break search_for_merges;            
          }
        }
      }
    } while (mergeFound);
  }
  
  /** Returns an estimate of the velocity of a blob, given a list of previous blobs. */
  PVector estimateVelocity(Blob blob, List<Blob> prevBlobs, float deltaSec, float maxSpeed) {
    PVector minVel = null;
    for (Blob b : prevBlobs) {
      PVector vel = PVector.div(PVector.sub(blob.pos, b.pos), deltaSec);
      if (minVel == null || vel.mag() < minVel.mag()) {
        minVel = vel;
      }
    }
    if (minVel != null && minVel.mag() < maxSpeed) {
      return minVel;
    }
    return new PVector(0, 0, 0);
  }

  /** Returns a copy of the current list of blobs. */
  public List<Blob> getBlobs() {
    List<Blob> result = new ArrayList<Blob>();
    for (Blob b : blobs) {
      result.add(new Blob(b.pos, b.vel, b.size));
    }
    return result;
  }
  
  public class Blob {
    public PVector pos;
    public PVector vel;
    public float size;

    Blob(PVector pos, PVector vel, float size) {
      this.pos = pos;
      this.vel = vel;
      this.size = size;
    }
    
    Blob(PVector pos, float size) {
      this(pos, new PVector(0, 0, 0), size);
    }
    
    String toString() {
      return String.format("pos %s vel %s size %.0f", pos.toString(), vel.toString(), size);
    }
  }
}