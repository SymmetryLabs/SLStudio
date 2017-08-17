public class SkylightShiftingPlane extends SLPattern {

  final CompoundParameter hueShift = new CompoundParameter("hShift", 0.5, 0, 1);

  final SinLFO a = new SinLFO(-.2, .2, 5300);
  final SinLFO b = new SinLFO(1, -1, 13300);
  final SinLFO c = new SinLFO(-1.4, 1.4, 5700);
  final SinLFO d = new SinLFO(-10, 10, 9500);

  public SkylightShiftingPlane(LX lx) {
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

    for (LXPoint p : model.skylight.points) {
      float d = abs(av*(p.x-model.skylight.cx) + bv*(p.y-model.skylight.cy) + cv*(p.z-model.skylight.cz) + dv) / denom;
      colors[p.index] = lx.hsb(
        hv + (abs(p.x-model.skylight.cx)*.6 + abs(p.y-model.skylight.cy)*.9 + abs(p.z - model.skylight.cz))*hueShift.getValuef(),
        constrain(110 - d*6, 0, 100),
        constrain(130 - 7*d, 0, 100)
      );
    }
  }
}