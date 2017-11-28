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

        hue += 180;
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

public class SelectedStrip extends SLPattern {

  public SelectedStrip(LX lx) {
    super(lx);
    addParameter(selectedStrip);
  }

  public void run(double deltaMs) {
    setColors(0);
    for (Sun sun : this.model.suns) {
      for (Slice slice : sun.slices) {
        int stripIndex = selectedStrip.getValuei();

        if (stripIndex > slice.strips.size()) {
          break;
        }

        Strip strip = slice.strips.get(selectedStrip.getValuei() - 1);

        for (LXPoint p : strip.points) {
          colors[p.index] = LXColor.RED;
        }
      }
    }
  }
}

public class ParamCrossSections extends SLPattern {

  final CompoundParameter x = new CompoundParameter("XPOS", 0.3);
  final CompoundParameter y = new CompoundParameter("YPOS", 0.3);
  final CompoundParameter z = new CompoundParameter("ZPOS", 0.3);
  final CompoundParameter xw = new CompoundParameter("XWID", 0.3);
  final CompoundParameter yw = new CompoundParameter("YWID", 0.3);
  final CompoundParameter zw = new CompoundParameter("ZWID", 0.3);
  final CompoundParameter xl = new CompoundParameter("XLEV", 1);
  final CompoundParameter yl = new CompoundParameter("YLEV", 1);
  final CompoundParameter zl = new CompoundParameter("ZLEV", 0.5);

  public ParamCrossSections(LX lx) {
    super(lx);
    addParams();
  }
  
  protected void addParams() {
    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(xl);
    addParameter(yl);
    addParameter(zl);
    addParameter(xw);
    addParameter(yw);
    addParameter(zw);
  }
  
  float xv, yv, zv;
  
  protected void updateXYZVals() {
    xv = model.xMin + (x.getValuef() * model.xRange);
    yv = model.yMin + (y.getValuef() * model.yRange);
    zv = model.zMin + (z.getValuef() * model.zRange);
  }

  public void run(double deltaMs) {
    updateXYZVals();
    
    float xlv = 100*xl.getValuef();
    float ylv = 100*yl.getValuef();
    float zlv = 100*zl.getValuef();
    
    float xwv = 100. / (1 + 1*xw.getValuef());
    float ywv = 100. / (1 + 1*yw.getValuef());
    float zwv = 100. / (1 + 1*zw.getValuef());
    
    for (LXPoint p : model.points) {
      color c = 0;
      c = PImage.blendColor(c, lx.hsb(
      (palette.getHuef() + p.x/10 + p.y/3) % 360, 
      constrain(140 - 1.1*abs(p.x - model.xMax/2.), 0, 100), 
      max(0, xlv - xwv*abs(p.x - xv))
        ), ADD);
      c = PImage.blendColor(c, lx.hsb(
      (palette.getHuef() + 80 + p.y/10) % 360, 
      constrain(140 - 2.2*abs(p.y - model.yMax/2.), 0, 100), 
      max(0, ylv - ywv*abs(p.y - yv))
        ), ADD); 
      c = PImage.blendColor(c, lx.hsb(
      (palette.getHuef() + 160 + p.z / 10 + p.y/2) % 360, 
      constrain(140 - 2.2*abs(p.z - model.zMax/2.), 0, 100), 
      max(0, zlv - zwv*abs(p.z - zv))
        ), ADD); 
      colors[p.index] = c;
    }
  }
}