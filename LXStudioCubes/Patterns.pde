import heronarts.lx.modulator.*;
import heronarts.p3lx.ui.studio.device.*;

public static class Test extends SLPattern {
  
  final CompoundParameter thing = new CompoundParameter("Thing", 0, model.yRange);
  final SinLFO lfo = new SinLFO("Stuff", 0, 1, 2000);
  
  public Test(LX lx) {
    super(lx);
    addParameter(thing);
    startModulator(lfo);
  }
  
  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = palette.getColor(max(0, 100 - 10*abs(p.y - thing.getValuef())));
    }
  }
}

public static class Palette extends SLPattern {
  public Palette(LX lx) {
    super(lx);
  }
  
  public void run(double deltaMs) {
    for (LXPoint p : model.points) {
      colors[p.index] = palette.getColor(p);
    }
  }
}

public static class CubeEQ extends SLPattern {

  private GraphicMeter eq = null;
  private LXAudioInput audioInput;

  private final BoundedParameter edge = new BoundedParameter("EDGE", 0.5);
  private final BoundedParameter clr = new BoundedParameter("CLR", 0.1, 0, .5);
  private final BoundedParameter blockiness = new BoundedParameter("BLK", 0.5);

  public CubeEQ(LX lx) {
    super(lx);
    audioInput = lx.engine.audio.getInput();
    eq = new GraphicMeter(audioInput);
    println(eq.gain);
    // addParameter(eq.gain);
    // addParameter(eq.range);
    // addParameter(eq.attack);
    // addParameter(eq.release);
    // addParameter(eq.slope);
    addParameter(edge);
    addParameter(clr);
    addParameter(blockiness);
    addModulator(eq).start();
  }

  // void onActive() {
  //   if (eq == null) {
  //     audioInput = lx.engine.audio.getInput();
  //     eq = new GraphicMeter(audioInput);
  //     eq.range.setValue(48);
  //     eq.release.setValue(800);
  //     addParameter(eq.gain);
  //     addParameter(eq.range);
  //     addParameter(eq.attack);
  //     addParameter(eq.release);
  //     addParameter(eq.slope);
  //     addParameter(edge);
  //     addParameter(clr);
  //     addParameter(blockiness);
  //     addModulator(eq).start();
  //   }
  // }

  public void run(double deltaMs) {
    float edgeConst = 2 + 30*edge.getValuef();
    float clrConst = 1.1 + clr.getValuef();

    for (LXPoint p : model.points) {
      float avgIndex = constrain(2 + p.x / model.xMax * (eq.numBands-4), 0, eq.numBands-4);
      int avgFloor = (int) avgIndex;

      float leftVal = eq.getBandf(avgFloor);
      float rightVal = eq.getBandf(avgFloor+1);
      float smoothValue = lerp(leftVal, rightVal, avgIndex-avgFloor);
      
      float chunkyValue = (
        eq.getBandf(avgFloor/4*4) +
        eq.getBandf(avgFloor/4*4 + 1) +
        eq.getBandf(avgFloor/4*4 + 2) +
        eq.getBandf(avgFloor/4*4 + 3)
      ) / 4.; 
      
      float value = lerp(smoothValue, chunkyValue, blockiness.getValuef());

      float b = constrain(edgeConst * (value*model.yMax - p.y), 0, 100);
      colors[p.index] = lx.hsb(
        480 + palette.getHuef() - min(clrConst*p.y, 120),
        100, 
        b
      );
    }
  }
}

public static class Swarm extends SLPattern {
  
  SawLFO offset = new SawLFO(0, 1, 1000);
  SinLFO rate = new SinLFO(350, 1200, 63000);
  SinLFO falloff = new SinLFO(15, 50, 17000);
  SinLFO fX = new SinLFO(model.xMin, model.xMax, 19000);
  SinLFO fY = new SinLFO(model.yMin, model.yMax, 11000);
  SinLFO hOffX = new SinLFO(model.xMin, model.xMax, 13000);

  public Swarm(LX lx) {
    super(lx);
    
    addModulator(offset).trigger();
    addModulator(rate).trigger();
    addModulator(falloff).trigger();
    addModulator(fX).trigger();
    addModulator(fY).trigger();
    addModulator(hOffX).trigger();
    offset.setPeriod(rate);
  }

  float modDist(float v1, float v2, float mod) {
    v1 = v1 % mod;
    v2 = v2 % mod;
    if (v2 > v1) {
      return min(v2-v1, v1+mod-v2);
    } 
    else {
      return min(v1-v2, v2+mod-v1);
    }
  }

  void run(double deltaMs) {
    float s = 0;
    for (Strip strip : model.strips) {
      int i = 0;
      for (LXPoint p : strip.points) {
        float fV = max(-1, 1 - dist(p.x/2., p.y, fX.getValuef()/2., fY.getValuef()) / 64.);
       // println("fv: " + fV); 
        colors[p.index] = lx.hsb(
        palette.getHuef() + 0.3 * abs(p.x - hOffX.getValuef()),
        constrain(80 + 40 * fV, 0, 100), 
        constrain(100 - 
          (30 - fV * falloff.getValuef()) * modDist(i + (s*63)%61, offset.getValuef() * strip.metrics.numPoints, strip.metrics.numPoints), 0, 100)
          );
        ++i;
      } 
      ++s;
    }
  }
}

public static class SpaceTime extends SLPattern {

  SinLFO pos = new SinLFO(0, 1, 3000);
  SinLFO rate = new SinLFO(1000, 9000, 13000);
  SinLFO falloff = new SinLFO(10, 70, 5000);
  float angle = 0;

  BoundedParameter rateParameter = new BoundedParameter("RATE", 0.5);
  BoundedParameter sizeParameter = new BoundedParameter("SIZE", 0.5);

  public SpaceTime(LX lx) {
    super(lx);

    addModulator(  pos).trigger();
    addModulator(rate).trigger();
    addModulator(falloff).trigger();
    pos.setPeriod(rate);
    addParameter(rateParameter);
    addParameter(sizeParameter);
  }

  public void onParameterChanged(LXParameter parameter) {
    if (parameter == rateParameter) {
      rate.stop();
      rate.setValue(9000 - 8000*parameter.getValuef());
    }  else if (parameter == sizeParameter) {
      falloff.stop();
      falloff.setValue(70 - 60*parameter.getValuef());
    }
  }

  void run(double deltaMs) {
    angle += deltaMs * 0.0007;
    float sVal1 = model.strips.size() * (0.5 + 0.5*sin(angle));
    float sVal2 = model.strips.size() * (0.5 + 0.5*cos(angle));

    float pVal = pos.getValuef();
    float fVal = falloff.getValuef();

    int s = 0;
    for (Strip strip : model.strips) {
      int i = 0;
      for (LXPoint p : strip.points) {
        colors[p.index] = lx.hsb(
          palette.getHuef() + 360 - p.x*.2 + p.y * .3,
          constrain(.4 * min(abs(s - sVal1), abs(s - sVal2)), 20, 100),
          max(0, 100 - fVal*abs(i - pVal*(strip.metrics.numPoints - 1)))
        );
        ++i;
      }
      ++s;
    }
  }
}

public static class ShiftingPlane extends SLPattern {

  final BoundedParameter hueShift = new BoundedParameter("hShift", 0.5, 0, 1);

  final SinLFO a = new SinLFO(-.2, .2, 5300);
  final SinLFO b = new SinLFO(1, -1, 13300);
  final SinLFO c = new SinLFO(-1.4, 1.4, 5700);
  final SinLFO d = new SinLFO(-10, 10, 9500);

  public ShiftingPlane(LX lx) {
    super(lx);
    addParameter(hueShift);
    addModulator(a).trigger();
    addModulator(b).trigger();
    addModulator(c).trigger();
    addModulator(d).trigger();
  }
  
  public void run(double deltaMs) {
    float hv = palette.getHuef();
    float av = a.getValuef();
    float bv = b.getValuef();
    float cv = c.getValuef();
    float dv = d.getValuef();
    float denom = sqrt(av*av + bv*bv + cv*cv);

    for (LXPoint p : model.points) {
      float d = abs(av*(p.x-model.cx) + bv*(p.y-model.cy) + cv*(p.z-model.cz) + dv) / denom;
      colors[p.index] = lx.hsb(
        hv + (abs(p.x-model.cx)*.6 + abs(p.y-model.cy)*.9 + abs(p.z - model.cz))*hueShift.getValuef(),
        constrain(110 - d*6, 0, 100),
        constrain(130 - 7*d, 0, 100)
      );
    }
  }
}

//   public void buildControlUI(UI ui, UIPatternControl container) {
//     int i = 0;
//     for (LXLayer layer : getLayers()) {
//       new UIButton((i % 4)*33, (i/4)*22, 28, 18).setLabel(Integer.toString(i+1)).setParameter(((Layer)layer).active).addToContainer(container);
//       ++i;
//     }
//     int knobSpacing = UIKnob.WIDTH + 4;
//     new UIKnob(0, 92).setParameter(size).addToContainer(container);
//     new UIKnob(knobSpacing, 92).setParameter(response).addToContainer(container);

//     container.setContentWidth(3*knobSpacing - 4);
//   }
  
//   class Layer extends LXLayer {
    
//     private final StudioCubes.Source.Channel object;
//     private final BooleanParameter active = new BooleanParameter("Active", true); 
    
//     Layer(LX lx, StudioCubes.Source.Channel object) {
//       super(lx);
//       this.object = object;
//       addParameter(active);
//     }
    
//     public void run(double deltaMs) {
//       if (!this.active.isOn()) {
//         return;
//       }
//       if (object.active) {
//         float falloff = 100 / (size.getValuef() + response.getValuef() * object.getValuef());
//         for (LXPoint p : model.points) {
//           float dist = dist(p.x, p.y, p.z, object.tx, object.ty, object.tz);
//           float b = 100 - dist*falloff;
//           if (b > 0) {
//             addColor(p.index, palette.getColor(p,  b));
//           }
//         }
//       }
//     }
//   }
  
//   public void run(double deltaMs) {
//     setColors(LXColor.BLACK);
//   }
// }

// public class Bouncing extends SLPattern {
  
//   public CompoundParameter gravity = new CompoundParameter("Gravity", -200, -100, -400);
//   public CompoundParameter size = new CompoundParameter("Length", 2*FEET, 1*FEET, 4*FEET);
//   public CompoundParameter amp = new CompoundParameter("Height", model.yRange, 1*FEET, model.yRange);
  
//   public Bouncing(LX lx) {
//     super(lx);
//     // for (Column column : venue.columns) {
//     //   addLayer(new Bouncer(lx, column));
//     // }
//     addParameter(gravity);
//     addParameter(size);
//     addParameter(amp);
//   }
  
//   class Bouncer extends LXLayer {
    
//     private final Column column;
//     private final Accelerator position;
    
//     Bouncer(LX lx, Column column) {
//       super(lx);
//       this.column = column;
//       this.position = new Accelerator(column.yMax, 0, gravity);
//       startModulator(position);
//     }
    
//     public void run(double deltaMs) {
//       if (position.getValue() < 0) {
//         position.setValue(-position.getValue());
//         position.setVelocity(sqrt(abs(2 * (amp.getValuef() - random(0, 2*FEET)) * gravity.getValuef()))); 
//       }
//       float h = palette.getHuef();
//       float falloff = 100. / size.getValuef();
//       //for (Rail rail : column.rails) {
//         for (LXPoint p : model.points) {
//           float b = 100 - falloff * abs(p.y - position.getValuef());
//           if (b > 0) {
//             addColor(p.index, palette.getColor(p, b));
//           }
//         }
//       //}
//     }
//   }
    
//   public void run(double deltaMs) {
//     setColors(LXColor.BLACK);
//   }
// }

// public class Movers extends SLPattern {
  
//   private CompoundParameter period = new CompoundParameter("Speed", 150000, 200000, 50000); 
  
//   public Movers(LX lx) {  
//     super(lx);
//     addParameter(period);
//     for (int i = 0; i < 15; ++i) {
//       addLayer(new Mover(lx));
//     }
//   }
  
//   class Mover extends LXLayer {
//     final TriangleLFO pos = new TriangleLFO(0, lx.total, period);
    
//     Mover(LX lx) {
//       super(lx);
//       startModulator(pos.randomBasis());
//     }
    
//     public void run(double deltaMs) {
//       for (LXPoint p : model.points) {
//         float b = 100 - 3*abs(p.index - pos.getValuef());
//         if (b > 0) {
//           addColor(p.index, palette.getColor(p, b));
//         }
//       }
//     }
//   }
  
//   public void run(double deltaMs) {
//     setColors(LXColor.BLACK);
//   }
// }

public class Noise extends SLPattern {
  
  public final CompoundParameter scale = new CompoundParameter("Scale", 10, 5, 40);
  public final CompoundParameter xSpeed = new CompoundParameter("XSpd", 0, -6, 6);
  public final CompoundParameter ySpeed = new CompoundParameter("YSpd", 0, -6, 6);
  public final CompoundParameter zSpeed = new CompoundParameter("ZSpd", 1, -6, 6);
  public final CompoundParameter floor = new CompoundParameter("Floor", 0, -2, 2);
  public final CompoundParameter range = new CompoundParameter("Range", 1, .2, 4);
  public final CompoundParameter xOffset = new CompoundParameter("XOffs", 0, -1, 1);
  public final CompoundParameter yOffset = new CompoundParameter("YOffs", 0, -1, 1);
  public final CompoundParameter zOffset = new CompoundParameter("ZOffs", 0, -1, 1);
  
  public Noise(LX lx) {
    super(lx);
    addParameter(scale);
    addParameter(floor);
    addParameter(range);
    addParameter(xSpeed);
    addParameter(ySpeed);
    addParameter(zSpeed);
    addParameter(xOffset);
    addParameter(yOffset);
    addParameter(zOffset);
  }
  
  private class Accum {
    private float accum = 0;
    private int equalCount = 0;
    private float sign = 1;
    
    void accum(double deltaMs, float speed) {
      float newAccum = (float) (this.accum + this.sign * deltaMs * speed / 4000.);
      if (newAccum == this.accum) {
        if (++this.equalCount >= 5) {
          this.equalCount = 0;
          this.sign = -sign;
          newAccum = this.accum + sign*.01;
        }
      }
      this.accum = newAccum;
    }
  };
  
  private final Accum xAccum = new Accum();
  private final Accum yAccum = new Accum();
  private final Accum zAccum = new Accum();
    
  @Override
  public void run(double deltaMs) {
    xAccum.accum(deltaMs, xSpeed.getValuef());
    yAccum.accum(deltaMs, ySpeed.getValuef());
    zAccum.accum(deltaMs, zSpeed.getValuef());
    
    float sf = scale.getValuef() / 1000.;
    float rf = range.getValuef();
    float ff = floor.getValuef();
    float xo = xOffset.getValuef();
    float yo = yOffset.getValuef();
    float zo = zOffset.getValuef();
    for (LXPoint p :  model.points) {
      float b = ff + rf * noise(sf*p.x + xo + xAccum.accum, sf*p.y + yo + yAccum.accum, sf*p.z + zo + zAccum.accum);
      colors[p.index] = palette.getColor(p, constrain(b*100, 0, 100));
    }
  }
}

public static abstract class SLPattern extends LXPattern {
  public final SLModel model;

  public SLPattern(LX lx) {
    super(lx);
    this.model = (SLModel)lx.model;
  }
}