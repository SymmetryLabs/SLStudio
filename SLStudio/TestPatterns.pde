public class SunOrientation extends SLPattern {
  public SunOrientation(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (Sun sun : this.model.suns) {
      int i = 0;
      for (Slice slice : sun.slices) {
        color col = ((i++ & 1) == 0) ? LXColor.BLUE : LXColor.RED;
        for (LXPoint p : slice.points) {
          colors[p.index] = col;
        }
      }
    }
  }
}

public class TestSuns extends SLPattern {

  public TestSuns(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    float hue = 0;

    for (Sun sun : model.suns) {
      for (LXPoint p : sun.points) {
        colors[p.index] = lx.hsb(hue, 100, 100);
      }

      hue += 70;
    }
  }
}

public class TestSlices extends SLPattern {

  public TestSlices(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (Sun sun : model.suns) {
      float hue = 0;

      for (Slice slice : sun.slices) {
        for (LXPoint p : slice.points) {
          colors[p.index] = lx.hsb(hue, 100, 100);
        }

        hue += 70;
      }
    }
  }
}

public class TestStrips extends SLPattern {

  public TestStrips(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    for (Sun sun : model.suns) {
      float hue = 0;

      for (Strip strip : sun.strips) {
        for (LXPoint p : strip.points) {
          colors[p.index] = lx.hsb(hue, 100, 100);
        }

        hue += 70;
      }
    }
  }
}

public class TestLowPowerStrips extends SLPattern {

  public TestLowPowerStrips(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    setColors(0);

    for (Sun sun : model.suns) {
      for (Strip strip : sun.strips) {
        int si = 0;
        for (LXPoint p : strip.points) {
          if (si < 3) {
            colors[p.index] =lx.hsb(LXColor.RED, 100, 100);
          }
          if (si > strip.points.length - 3) {
            colors[p.index] =lx.hsb(LXColor.BLUE, 100, 100);
          }
          si++;
        }
      }
    }
  }
}