package com.symmetrylabs.shows.wingportal;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXVector;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;


public class WingPortalModel extends StripsModel<Strip> {

  public final static int PIXEL_PITCH = 2;
  
  public WingPortalModel() {
    super("wingportal", new Fixture());
    Fixture fixture = (Fixture) this.fixtures.get(0);
    for (Strip strip : fixture.strips) {
      this.strips.add(strip);
    }
  }
  
  public static class Fixture extends LXAbstractFixture {

    public final ArrayList<Strip> strips = new ArrayList<Strip>();

    Fixture() {
      final LXVector[][] stripVectors = new LXVector[][] {
        new LXVector[] { new LXVector(-40.395f, 14.129f, 24.531f), new LXVector(-16.670f, 80.741f, 39.854f) }, // 2
        new LXVector[] { new LXVector(-26.106f, 83.268f, 41.494f), new LXVector(-66.330f, 21.058f, 43.320f) }, // 3
        new LXVector[] { new LXVector(-89.854f, 46.039f, 68.163f), new LXVector(-32.041f, 98.049f, 48.158f) }, // 4
        new LXVector[] { new LXVector(-32.295f, 111.83f, 57.463f), new LXVector(-100.81f, 77.921f, 104.04f) }, // 5
        new LXVector[] { new LXVector(-109.53f, 108.99f, 141.93f), new LXVector(-34.100f, 126.01f, 63.412f) }, // 6
        new LXVector[] { new LXVector(-34.056f, 140.28f, 69.754f), new LXVector(-119.34f, 140.16f, 178.44f) }, // 7
        new LXVector[] { new LXVector(-130.01f, 165.80f, 214.62f), new LXVector(-34.845f, 153.43f, 75.422f) }, // 8
        new LXVector[] { new LXVector(-36.219f, 171.24f, 77.759f), new LXVector(-165.56f, 194.75f, 218.70f) }, // 9
        new LXVector[] { new LXVector(-203.99f, 220.76f, 201.61f), new LXVector(-38.867f, 182.30f, 68.484f) }, // 10
        new LXVector[] { new LXVector(-42.929f, 198.33f, 33.391f), new LXVector(-225.80f, 254.02f, 147.61f) }, // 11
        new LXVector[] { new LXVector(-264.70f, 290.60f, 82.820f), new LXVector(-47.587f, 211.36f, 6.2326f) }, // 12
        new LXVector[] { new LXVector(-51.519f, 224.99f, -21.95f), new LXVector(-307.27f, 326.42f, 8.9286f) }, // 13
        new LXVector[] { new LXVector(-347.99f, 363.29f, -60.44f), new LXVector(-55.735f, 237.94f, -49.28f) }, // 14
        new LXVector[] { new LXVector(-58.414f, 248.75f, -72.85f), new LXVector(-388.31f, 398.57f, -127.3f) }, // 15

        new LXVector[] { new LXVector(59.1419f, 247.16f, -72.87f), new LXVector(387.514f, 400.32f, -127.3f) }, // 59
        new LXVector[] { new LXVector(347.300f, 364.90f, -60.41f), new LXVector(56.5062f, 236.17f, -49.31f) }, // 60
        new LXVector[] { new LXVector(52.1781f, 223.37f, -21.98f), new LXVector(306.547f, 328.21f, 8.9598f) }, // 61
        new LXVector[] { new LXVector(264.073f, 290.53f, 84.643f), new LXVector(48.1611f, 211.42f, 4.5807f) }, // 62
        new LXVector[] { new LXVector(43.8496f, 198.41f, 31.905f), new LXVector(224.788f, 253.93f, 149.26f) }, // 63
        new LXVector[] { new LXVector(202.789f, 220.66f, 203.12f), new LXVector(39.9632f, 182.40f, 67.123f) }, // 64
        new LXVector[] { new LXVector(34.9525f, 171.14f, 78.962f), new LXVector(166.967f, 194.86f, 217.37f) }, // 65
        new LXVector[] { new LXVector(131.443f, 165.91f, 213.61f), new LXVector(33.2711f, 153.31f, 76.535f) }, // 66
        new LXVector[] { new LXVector(32.5556f, 140.16f, 70.964f), new LXVector(120.703f, 140.26f, 177.34f) }, // 67
        new LXVector[] { new LXVector(110.781f, 109.09f, 140.71f), new LXVector(32.7206f, 125.90f, 64.759f) }, // 68
        new LXVector[] { new LXVector(33.4484f, 111.93f, 55.916f), new LXVector(99.7668f, 77.831f, 105.44f) }, // 69
        new LXVector[] { new LXVector(90.9893f, 47.372f, 68.187f), new LXVector(30.7892f, 96.578f, 48.132f) }, // 70
        new LXVector[] { new LXVector(38.7313f, 13.587f, 24.521f), new LXVector(18.5068f, 81.340f, 39.865f) }, // 71
        new LXVector[] { new LXVector(-0.2588f, 82.171f, 39.466f), new LXVector(0.00000f, 26.426f, 11.163f) }, // 72
      };

      for (LXVector[] vectors : stripVectors) {
        LXVector start = vectors[0];
        LXVector end = vectors[1];

        int numPoints = (int) start.dist(end) * 4 / PIXEL_PITCH;
        List<LXPoint> stripPoints = new ArrayList<>();

        for (int i = 0; i < numPoints; i++) {
          LXVector pos = start.lerp(end, ((float)i) / numPoints);
          LXPoint p = new LXPoint(pos.x, pos.y, pos.z);
          stripPoints.add(p);
        }

        Strip strip = new Strip("strip", new Strip.Metrics(numPoints), stripPoints);
        this.strips.add(strip);

        for (LXPoint point : stripPoints) {
          this.points.add(point);
        }
      }
    }
  }
}
