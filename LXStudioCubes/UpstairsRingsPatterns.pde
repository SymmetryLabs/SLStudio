public class UpstairsRingsSwarm extends SLPattern {
  
  SawLFO offset = new SawLFO(0, 1, 1000);
  SinLFO rate = new SinLFO(350, 1200, 63000);
  SinLFO falloff = new SinLFO(15, 50, 17000);
  SinLFO fX = new SinLFO(model.upstairsRings.xMin, model.upstairsRings.xMax, 19000);
  SinLFO fY = new SinLFO(model.upstairsRings.yMin, model.upstairsRings.yMax, 11000);
  SinLFO hOffX = new SinLFO(model.upstairsRings.xMin, model.upstairsRings.xMax, 13000);

  public UpstairsRingsSwarm(LX lx) {
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
    for (Ring ring : model.upstairsRings.rings) {
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
