public class FlockWave extends SLPattern {
  CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);  // focus coordinates (m)
  CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  CompoundParameter spawnRadius = new CompoundParameter("spRad", 100, 0, 1000);  // radius (m) within which to spawn birds
  CompoundParameter spawnMinSpeed = new CompoundParameter("spMin", 2, 0, 40);  // minimum focus speed (m/s) that spawns birds
  CompoundParameter spawnMaxSpeed = new CompoundParameter("spMax", 20, 0, 40);  // maximum focus speed (m/s) that spawns birds
  
  CompoundParameter density = new CompoundParameter("density", 2, 0, 4);  // maximum spawn rate (birds/s)
  CompoundParameter scatter = new CompoundParameter("scatter", 100, 0, 1000);  // initial velocity randomness (m/s)
  CompoundParameter speedMult = new CompoundParameter("spdMult", 1, 0, 2);  // (ratio) target bird speed / focus speed
  CompoundParameter maxSpeed = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (m/s)
  CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn
  CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
  CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);  // time (s) to fade down to 10% intensity

  CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (m)
  CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
  CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)
  DiscreteParameter palette = new DiscreteParameter("palette", skyPalettes.getNames());  // selected colour palette
  CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)

  PVector prevFocus = null;
  Set<Bird> birds = new HashSet<Bird>();
  float numToSpawn = 0;

  public FlockWave(LX lx) {
    super(lx);
    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(spawnRadius);
    addParameter(spawnMinSpeed);
    addParameter(spawnMaxSpeed);

    addParameter(density);
    addParameter(scatter);
    addParameter(speedMult);
    addParameter(maxSpeed);
    addParameter(turnSec);
    addParameter(fadeInSec);
    addParameter(fadeOutSec);

    addParameter(size);
    addParameter(detail);
    addParameter(ripple);
    addParameter(palette);
    addParameter(palShift);
  }

  public void run(double deltaMs) {
    float deltaSec = (float) deltaMs * 0.001;
    PVector focus = new PVector(x.getValuef(), y.getValuef(), z.getValuef());
    if (prevFocus != null) {
      PVector vel = PVector.sub(focus, prevFocus);
      vel.div(deltaSec);
      spawnBirds(deltaSec, focus, vel);
      advanceBirds(deltaSec, vel);
      removeExpiredBirds();
     // println("deltaMs: " + deltaMs + " / speed: " + vel.mag() + " / birds: " + birds.size());
    }
    renderBirds();
    prevFocus = focus;
  }

  void spawnBirds(float deltaSec, PVector focus, PVector vel) {
    float spawnMin = spawnMinSpeed.getValuef(); //<>//
    float spawnMax = spawnMaxSpeed.getValuef();
    float speed = vel.mag();
    if (speed > spawnMin) {
      numToSpawn += deltaSec * density.getValuef() * (speed - spawnMin) / (spawnMax - spawnMin);
      while (numToSpawn >= 1.0) {
        spawnBird(focus);
        numToSpawn -= 1.0;
      }
    }
  }

  void spawnBird(PVector focus) {
    PVector pos = getRandomUnitVector();
    pos.mult(spawnRadius.getValuef());
    pos.add(focus);
    birds.add(new Bird(pos, LXColor.hsb(Math.random()*360, Math.random()*100, 100)));
  }

  void advanceBirds(float deltaSec, PVector vel) {
    PVector targetVel = PVector.mult(vel, speedMult.getValuef());
    for (Bird b : birds) {
      b.run(deltaSec, targetVel);
    }
  }

  void removeExpiredBirds() {
    List<Bird> expired = new ArrayList<Bird>();
    for (Bird b : birds) {
      if (b.hasExpired) {
        expired.add(b);
      }
    }
    birds.removeAll(expired);
  }

  void renderBirds() {
    renderPlasma();
  }

  void renderTrails() {
    float radius = size.getValuef();
    float sqRadius = radius*radius;

    for (LXPoint p : model.points) {
      int rgb = 0;
      for (Bird b : birds) {
        if (Math.abs(b.pos.x - p.x) < radius) {
          if ((b.pos.x - p.x)*(b.pos.x - p.x) + (b.pos.y - p.y)*(b.pos.y - p.y) < sqRadius) {
            rgb = LXColor.add(rgb, LXColor.lerp(0, b.rgb, b.value));
          }
        }
      }
      colors[p.index] = LXColor.add(LXColor.lerp(colors[p.index], 0, 0.1), rgb);
    }
  }

  SortedSet<Bird> getSortedSet(Set<Bird> birds) {
    SortedSet<Bird> result = new TreeSet<Bird>();
    for (Bird b : birds) {
      result.add(b);
    }
    return result;
  }

  SortedSet<Bird> getSubSet(SortedSet<Bird> birds, float xLow, float xHigh) {
    Bird low = new Bird(new PVector(xLow, 0, 0), 0);
    Bird high = new Bird(new PVector(xHigh, 0, 0), 0);
    // return birds.subSet(low, high);
    TreeSet<Bird> result = new TreeSet<Bird>();
    for (Bird b : birds) {
      if (b.compareTo(low) >= 0 && b.compareTo(high) < 0) result.add(b);
    }
    return result;
  }

  void renderVoronoi() {
    float radius = size.getValuef();

    radius = 10000;
    for (LXPoint p : model.points) {
      Bird closestBird = null;
      float minSqDist = 1e6;
      for (Bird b : birds) {
        if (Math.abs(b.pos.x - p.x) < radius) {
          float sqDist = (b.pos.x - p.x)*(b.pos.x - p.x) + (b.pos.y - p.y)*(b.pos.y - p.y);
          if (sqDist < minSqDist) {
            minSqDist = sqDist;
            closestBird = b;
          }
        }
      }
      colors[p.index] = minSqDist > radius ? 0 : LXColor.lerp(0, closestBird.rgb, closestBird.value);
    }
  }

  void renderPlasma() {
    ColorPalette pal = skyPalettes.getPalette(palette.getOption());
    float waveNumber = detail.getValuef();
    float extent = size.getValuef();
    float rippleSpeed = ripple.getValuef();
    SortedSet<Bird> sortedBirds = getSortedSet(birds);
    Bird low = new Bird(new PVector(0, 0, 0), 0);
    Bird high = new Bird(new PVector(0, 0, 0), 0);
    for (LXPoint p : model.points) {
      low.pos.x = p.x - extent;
      high.pos.x = p.x + extent;
      double sum = 0;
      for (Bird b : sortedBirds.subSet(low, high)) {
        if (Math.abs(b.pos.y - p.y) < extent) {
          double sqDist = (
              (b.pos.x - p.x)*(b.pos.x - p.x) +
              (b.pos.y - p.y)*(b.pos.y - p.y) +
              (b.pos.z - p.z)*(b.pos.z - p.z)
          ) / (extent*extent);
          if (sqDist < 1) {
            double dist = Math.sqrt(sqDist);
            double a = 1 - sqDist;
            sum += a*a*Math.sin(waveNumber * 2 * Math.PI * dist - b.elapsedSec * rippleSpeed)*Math.cos(waveNumber * 5/4 * dist)*b.value;
          }
        }
      }
      colors[p.index] = pal.getColor(sum + palShift.getValuef());
    }
  }

  PVector getRandomUnitVector() {
    PVector pos = new PVector();
    while (true) {
      pos.set((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1);
      if (pos.mag() < 1) {
        return pos;
      }
    }
  }

  public class Bird implements Comparable<Bird> {
    public PVector pos;
    public PVector vel;
    public PVector prevPos;
    public int rgb;
    public float value;
    public float elapsedSec;
    public boolean hasExpired;

    Bird(PVector pos, int rgb) {
      this.pos = pos;
      this.vel = PVector.mult(getRandomUnitVector(), scatter.getValuef());
      this.rgb = rgb;
      this.value = 0;
      this.elapsedSec = 0;
      this.hasExpired = false;
    }

    void run(float deltaSec, PVector targetVel) {
      advance(deltaSec);
      turn(deltaSec, targetVel);

      elapsedSec += deltaSec;
      if (elapsedSec < fadeInSec.getValuef()) {
        value = elapsedSec / fadeInSec.getValuef();
      } else {
        value = (float) Math.pow(0.1, (elapsedSec - fadeInSec.getValuef()) / fadeOutSec.getValuef());
        if (value < 0.001) hasExpired = true;
      }
    }

    void advance(float deltaSec) {
      prevPos = pos;
      pos.add(PVector.mult(vel, (float) deltaSec));
    }

    void turn(float deltaSec, PVector targetVel) {
      float speed = vel.mag();
      float targetSpeed = targetVel.mag();

      float frac = (float) Math.pow(0.1, deltaSec / turnSec.getValuef());
      vel = PVector.add(PVector.mult(vel, frac), PVector.mult(targetVel, 1 - frac));
      speed = speed * frac + targetSpeed * (1 - frac);
      if (targetSpeed > maxSpeed.getValuef()) targetSpeed = maxSpeed.getValuef();

      float mag = vel.mag();
      if (mag > 0 && mag < speed) vel.div(mag / speed);
    }

    public int compareTo(Bird other) {
      return Float.compare(pos.x, other.pos.x);
    }
  }
}

public class SkyGradient extends SLPattern {
  public SkyGradient(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    ColorPalette palette = skyPalettes.getPalette("san francisco");
    for (LXPoint p : model.points) {
      float altitude = (p.y - model.yMin) / (model.yMax - model.yMin);
      colors[p.index] = palette.getColor(altitude);
    }
  }
}

public class LightSource extends SLPattern {
  CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);
  CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  CompoundParameter falloff = new CompoundParameter("falloff", 0.75, 0, 1);
  CompoundParameter gain = new CompoundParameter("gain", 1, 0, 3);

  public LightSource(LX lx) {
    super(lx);
    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(falloff);
    addParameter(gain);
  }

  public void run(double deltaMs) {
    PVector light = new PVector(x.getValuef(), y.getValuef(), z.getValuef());
    for (LXPoint p : model.points) {
      if (p instanceof LXPointNormal) {
        LXPointNormal pn = (LXPointNormal) p;
        PVector pv = new PVector(p.x, p.y, p.z);
        PVector toLight = PVector.sub(light, pv);
        float dist = toLight.mag();

        dist /= falloff.getValue();
        if (dist < 1) dist = 1; // avoid division by zero or excessive brightness
        float brightness = 1.0 / (dist * dist);

        float cosAngle = PVector.dot(toLight.normalize(), pn.normal);
        if (cosAngle < 0) cosAngle = 0;

        float value = cosAngle * brightness * gain.getValuef();
        colors[p.index] = LX.hsb(palette.getHuef(), 100f, 100f * (value > 1 ? 1 : value));
      }
    }
  }
}