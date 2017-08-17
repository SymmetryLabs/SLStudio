public class RotatedRingsCrossSections extends SLPattern {
  
  final SinLFO x = new SinLFO(model.rotatedRings.xMin, model.rotatedRings.xMax, 5000);
  final SinLFO y = new SinLFO(model.rotatedRings.yMin, model.rotatedRings.yMax, 6000);
  final SinLFO z = new SinLFO(model.rotatedRings.zMin, model.rotatedRings.zMax, 7000);
  
  final CompoundParameter xw = new CompoundParameter("XWID", 0.3);
  final CompoundParameter yw = new CompoundParameter("YWID", 0.3);
  final CompoundParameter zw = new CompoundParameter("ZWID", 0.3);
  final CompoundParameter xr = new CompoundParameter("XRAT", 0.7);
  final CompoundParameter yr = new CompoundParameter("YRAT", 0.6);
  final CompoundParameter zr = new CompoundParameter("ZRAT", 0.5);
  final CompoundParameter xl = new CompoundParameter("XLEV", 1);
  final CompoundParameter yl = new CompoundParameter("YLEV", 1);
  final CompoundParameter zl = new CompoundParameter("ZLEV", 0.5);

  public RotatedRingsCrossSections(LX lx) {
    super(lx);
    addModulator(x).trigger();
    addModulator(y).trigger();
    addModulator(z).trigger();
    addParams();
  }
  
  protected void addParams() {
    addParameter(xr);
    addParameter(yr);
    addParameter(zr);
    addParameter(xw);
    addParameter(xl);
    addParameter(yl);
    addParameter(zl);
    addParameter(yw);
    addParameter(zw);
  }
  
  void onParameterChanged(LXParameter p) {
    if (p == xr) {
      x.setPeriod(10000 - 8800*p.getValuef());
    } else if (p == yr) {
      y.setPeriod(10000 - 9000*p.getValuef());
    } else if (p == zr) {
      z.setPeriod(10000 - 9000*p.getValuef());
    }
  }
  
  float xv, yv, zv;
  
  protected void updateXYZVals() {
    xv = x.getValuef();
    yv = y.getValuef();
    zv = z.getValuef();
  }

  public void run(double deltaMs) {
    updateXYZVals();
    
    float xlv = 100*xl.getValuef();
    float ylv = 100*yl.getValuef();
    float zlv = 100*zl.getValuef();
    
    float xwv = 100. / (0.01 + 10*xw.getValuef());
    float ywv = 100. / (0.01 + 10*yw.getValuef());
    float zwv = 100. / (0.01 + 10*zw.getValuef());
    
    for (LXPoint p : model.rotatedRings.points) {
      color c = 0;
      c = PImage.blendColor(c, lx.hsb(
      palette.getHuef() + p.x/10 + p.y/3, 
      constrain(140 - 1.1*abs(p.x - model.rotatedRings.xMax/2.), 0, 100), 
      max(0, xlv - xwv*abs(p.x - xv))
        ), ADD);
      c = PImage.blendColor(c, lx.hsb(
      palette.getHuef() + 80 + p.y/10, 
      constrain(140 - 2.2*abs(p.y - model.rotatedRings.yMax/2.), 0, 100), 
      max(0, ylv - ywv*abs(p.y - yv))
        ), ADD); 
      c = PImage.blendColor(c, lx.hsb(
      palette.getHuef() + 160 + p.z / 10 + p.y/2, 
      constrain(140 - 2.2*abs(p.z - model.rotatedRings.zMax/2.), 0, 100), 
      max(0, zlv - zwv*abs(p.z - zv))
        ), ADD); 
      colors[p.index] = c;
    }
  }
}

public class RotatedRingsSwarm extends SLPattern {
  
  SawLFO offset = new SawLFO(0, 1, 1000);
  SinLFO rate = new SinLFO(350, 1200, 63000);
  SinLFO falloff = new SinLFO(15, 50, 17000);
  SinLFO fX = new SinLFO(model.rotatedRings.xMin, model.rotatedRings.xMax, 19000);
  SinLFO fY = new SinLFO(model.rotatedRings.yMin, model.rotatedRings.yMax, 11000);
  SinLFO hOffX = new SinLFO(model.rotatedRings.xMin, model.rotatedRings.xMax, 13000);

  public RotatedRingsSwarm(LX lx) {
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
    for (Ring ring : model.rotatedRings.rings) {
      int i = 0;
      for (LXPoint p : ring.points) {
        float fV = max(-1, 1 - dist(p.x/2., p.y, fX.getValuef()/2., fY.getValuef()) / 64.);
       // println("fv: " + fV); 
        colors[p.index] = lx.hsb(
        palette.getHuef() + 0.3 * abs(p.x - hOffX.getValuef()),
        constrain(80 + 40 * fV, 0, 100), 
        constrain(100 - 
          (30 - fV * falloff.getValuef()) * modDist(i + (s*63)%61, offset.getValuef() * ring.points.length, ring.points.length), 0, 100)
          );
        ++i;
      } 
      ++s;
    }
  }
}