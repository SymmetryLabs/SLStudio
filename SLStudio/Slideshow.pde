
public abstract class ColorSlideshow extends SLPattern {
  public final CompoundParameter rate = new CompoundParameter("Rate", 3000, 10000, 250);
  public final CompoundParameter offsetX = new CompoundParameter("OffsetX", 0, 0, 1);
  public final CompoundParameter offsetY = new CompoundParameter("OffsetY", 0, 0, 1);
  public final BooleanParameter perSun = new BooleanParameter("PerSun", false);

  private final SawLFO lerp = (SawLFO) startModulator(new SawLFO(0, 1, rate));

  private int imageIndex = 0;
  private final PImage[] images;

  public ColorSlideshow(LX lx) {
    super(lx);

    String[] paths = getPaths();
    images = new PImage[paths.length];
    for (int i = 0; i < images.length; ++i) {
      images[i] = loadImage(paths[i]);
      images[i].loadPixels();
    }

    addParameter("rate", rate);
    addParameter("perSun", perSun);
  }

  abstract String[] getPaths();

  public void run(double deltaMs) {
    float lerp = this.lerp.getValuef();
    if (this.lerp.loop()) {
      this.imageIndex = (this.imageIndex + 1) % this.images.length;
    }
    PImage image1 = this.images[this.imageIndex];
    PImage image2 = this.images[(this.imageIndex + 1) % this.images.length];

    double offsetXValue = offsetX.getValue();
    double offsetYValue = offsetY.getValue();

    if (perSun.getValueb()) {
      for (Sun sun : model.suns) {
        for (LXPoint p : sun.points) {
          double px = FastMath.min((p.x - sun.xMin) / sun.xRange + offsetXValue, 1);
          double py = FastMath.min((p.y - sun.yMin) / sun.yRange + offsetYValue, 1);
          int c1 = image1.get((int)(px * (image1.width-1)), (int)(py * (image1.height-1)));
          int c2 = image2.get((int)(px * (image2.width-1)), (int)(py * (image2.height-1)));

          colors[p.index] = LXColor.lerp(c1, c2, lerp);
        }
      }
    }
    else {
      for (LXPoint p : model.points) {
        double px = FastMath.min((p.x - model.xMin) / model.xRange + offsetXValue, 1);
        double py = FastMath.min((p.y - model.xMin) / model.xRange + offsetYValue, 1);
        int c1 = image1.get((int)(px * (image1.width-1)), (int)(py * (image1.height-1)));
        int c2 = image2.get((int)(px * (image2.width-1)), (int)(py * (image2.height-1)));

        colors[p.index] = LXColor.lerp(c1, c2, lerp);
      }
    }
  }
}

public class ColorSlideshowClouds extends ColorSlideshow {
  public ColorSlideshowClouds(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/clouds1.jpeg",
      "images/clouds2.jpeg",
      "images/clouds3.jpeg"

    };
  }
}

public class ColorSlideshowSunsets extends ColorSlideshow {
  public ColorSlideshowSunsets(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/sunset1.jpeg",
      "images/sunset2.jpeg",
      "images/sunset3.jpeg",
      "images/sunset4.jpeg",
      "images/sunset5.jpeg",
      "images/sunset6.jpeg"
    };
  }
}

public class ColorSlideshowOceans extends ColorSlideshow {
  public ColorSlideshowOceans(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/ocean1.jpeg",
      "images/ocean2.jpeg",
      "images/ocean3.jpeg",
      "images/ocean4.jpeg"
    };
  }
}

public class ColorSlideshowCorals extends ColorSlideshow {
  public ColorSlideshowCorals(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/coral1.jpeg",
      "images/coral2.jpeg",
      "images/coral3.jpeg",
      "images/coral4.jpeg",
      "images/coral5.jpeg",
    };
  }
}
