
public abstract class TextureSlideshow extends SLPattern {
  public final CompoundParameter rate = new CompoundParameter("rate", 3000, 10000, 250);
  public final BooleanParameter perSun = new BooleanParameter("perSun", false);
  public final CompoundParameter offsetX = new CompoundParameter("offsetX", 0, -1, 1);
  public final CompoundParameter offsetY = new CompoundParameter("offsetY", 0, -1, 1);
  public final CompoundParameter zoomX = new CompoundParameter("zoomX", 0, 0, 5);
  public final CompoundParameter zoomY = new CompoundParameter("zoomY", 0, 0, 5);

  private final SawLFO lerp = (SawLFO)startModulator(new SawLFO(0, 1, rate));

  private int imageIndex = 0;
  private final PImage[] images;
  private final int[][] imageLayers;

  public TextureSlideshow(LX lx) {
    super(lx);

    String[] paths = getPaths();
    images = new PImage[paths.length];
    for (int i = 0; i < images.length; ++i) {
      images[i] = loadImage(paths[i]);
      images[i].loadPixels();
    }

    imageLayers = new int[images.length][colors.length];

    addParameter("rate", rate);
    addParameter("perSun", perSun);
    addParameter("offsetX", offsetX);
    addParameter("offsetY", offsetY);
    addParameter("zoomX", zoomX);
    addParameter("zoomY", zoomY);

    LXParameterListener updateRastersListener = new LXParameterListener() {
      private boolean inProgress = false;

      public void onParameterChanged(LXParameter ignore) {
        synchronized (this) {
          if (inProgress)
            return;

          inProgress = true;
        }

        updateRasters();

        synchronized (this) {
          inProgress = false;
        }
      }
    };

    perSun.addListener(updateRastersListener);
    zoomX.addListener(updateRastersListener);
    zoomY.addListener(updateRastersListener);
    offsetX.addListener(updateRastersListener);
    offsetY.addListener(updateRastersListener);

    updateRasters();
  }

  abstract String[] getPaths();

  private int bilinearInterp(PImage image, double px, double py) {
    int imgOffsX = (int)(offsetX.getValue() * (image.width - 1) + image.width);
    int imgOffsY = (int)(offsetY.getValue() * (image.height - 1) + image.height);

    double zoomXValue = zoomX.getValue() + 1;
    double zoomYValue = zoomY.getValue() + 1;

    double imgX = px * (image.width - 1) / zoomXValue + imgOffsX;
    int imgXFloor = (int)Math.floor(imgX);
    int imgXCeil = (int)Math.ceil(imgX);
    double xRem = imgXCeil - imgXFloor;

    double imgY = py * (image.height - 1) / zoomYValue + imgOffsY;
    int imgYFloor = (int)Math.floor(imgY);
    int imgYCeil = (int)Math.ceil(imgY);
    double yRem = imgYCeil - imgYFloor;

    imgXFloor %= image.width;
    imgXCeil %= image.width;
    imgYFloor %= image.height;
    imgYCeil %= image.height;

    int q11 = image.get(imgXFloor, imgYFloor);
    int q12 = image.get(imgXFloor, imgYCeil);
    int q21 = image.get(imgXCeil, imgYFloor);
    int q22 = image.get(imgXCeil, imgYCeil);

    int q1 = LXColor.lerp(q11, q21, xRem);
    int q2 = LXColor.lerp(q12, q22, xRem);

    return LXColor.lerp(q1, q2, yRem);
  }

  private void updateRasters() {
    for (int i = 0; i < images.length; ++i) {
      PImage image = images[i];
      int[] layer = imageLayers[i];

      if (perSun.getValueb()) {
        for (Sun sun : model.suns) {
          for (LXPoint p : sun.points) {
            double px = (p.x - sun.xMin) / sun.xRange;
            double py = (p.y - sun.yMin) / sun.yRange;

            layer[p.index] = bilinearInterp(image, px, py);
          }
        }
      }
      else {
        for (LXPoint p : model.points) {
          double px = (p.x - model.xMin) / model.xRange;
          double py = (p.y - model.yMin) / model.yRange;

          layer[p.index] = bilinearInterp(image, px, py);
        }
      }
    }
  }

  public void run(double deltaMs) {
    double lerpValue = lerp.getValue();
    if (lerp.loop()) {
      imageIndex = (imageIndex + 1) % images.length;
    }

    int image1Index = imageIndex;
    int image2Index = (imageIndex + 1) % images.length;

    for (LXPoint p : lx.model.points) {
      int c1 = imageLayers[image1Index][p.index];
      int c2 = imageLayers[image2Index][p.index];

      colors[p.index] = LXColor.lerp(c1, c2, lerpValue);
    }
  }
}

public class TextureClouds extends TextureSlideshow {
  public TextureClouds(LX lx) {
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

public class TextureSunsets extends TextureSlideshow {
  public TextureSunsets(LX lx) {
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

public class TextureOceans extends TextureSlideshow {
  public TextureOceans(LX lx) {
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

public class TextureCorals extends TextureSlideshow {
  public TextureCorals(LX lx) {
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

public class TextureJupiter1 extends TextureSlideshow {
  public TextureJupiter1(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/junocam-jupiter-blues-pia21972.jpg",
    };
  }
}

public class TextureJupiter2 extends TextureSlideshow {
  public TextureJupiter2(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/junocam-jupiter-pj09-90-001.jpg",
    };
  }
}

public class TextureNebula1 extends TextureSlideshow {
  public TextureNebula1(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/apod-ccbysa-171004-soul-herschel.jpg",
    };
  }
}

public class TextureNebula2 extends TextureSlideshow {
  public TextureNebula2(LX lx) {
    super(lx);
  }

  public String[] getPaths() {
    return new String[] {
      "images/apod-ccbysa-171101-thors-helmet.jpg",
    };
  }
}
