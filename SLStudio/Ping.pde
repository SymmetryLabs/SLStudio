import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;

public class SkyGradient extends SLPattern {
  public SkyGradient(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    SkyPaletteLibrary.ColorPalette palette = skyPalettes.getPalette("san francisco");
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
