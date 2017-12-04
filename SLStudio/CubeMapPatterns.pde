import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.P3LX;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Modifier;

public abstract class P3CubeMapPattern extends LXPattern {
  private final PGraphics pg;
  protected final PGraphics pgF, pgB, pgL, pgR, pgU, pgD;
  private final PVector origin;
  private final PVector bboxSize;
  private final int faceRes;

  private final String id = "" + Math.random();

  /**
   * A pattern that projects a cubemap image onto all the LEDs inside a given
   * bounding box in world space.  The cubemap image should have resolution 4k x 3k,
   * where each face of the cube takes up a k x k square, and the six faces are
   * arranged thus (where R/L are +x/-x of the origin, U/D are +y/-y, F/B are -z/+z):
   *         +---+
   *         | U |
   *     +---+---+---+---+
   *     | L | F | R | B |
   *     +---+---+---+---+
   *         | D |
   *         +---+
   * Note that the +z side is the back (B), not the front (F) as you might expect,
   * because Processing, insanely, uses a left-handed coordinate system.
   *
   * @param lx The global P3LX object.
   * @param origin The center of the bounding box in world space.
   * @param bboxSize The length, width, and height of the bounding box in world space.
   * @param faceRes The width and height, k, in pixels of one square face of the
   *     cubemap image, which will have total width 4k and total height 3k.
   */
  protected P3CubeMapPattern(P3LX lx, PVector origin, PVector bboxSize, int faceRes) {
    super(lx);
    this.pg = lx.applet.createGraphics(faceRes*4, faceRes*3, P3D);
    this.pgF = lx.applet.createGraphics(faceRes, faceRes, P3D);
    this.pgB = lx.applet.createGraphics(faceRes, faceRes, P3D);
    this.pgL = lx.applet.createGraphics(faceRes, faceRes, P3D);
    this.pgR = lx.applet.createGraphics(faceRes, faceRes, P3D);
    this.pgU = lx.applet.createGraphics(faceRes, faceRes, P3D);
    this.pgD = lx.applet.createGraphics(faceRes, faceRes, P3D);
    this.origin = origin;
    this.bboxSize = bboxSize;
    this.faceRes = faceRes;
  }

  private Runnable run = new Runnable() {
    long lastRunAt = System.currentTimeMillis();

    @Override
    public void run() {
      double deltaMs = System.currentTimeMillis() - lastRunAt;
      lastRunAt = System.currentTimeMillis();

      //pg.beginDraw();
      P3CubeMapPattern.this.run(deltaMs, pg);
      //pg.endDraw();
      pg.loadPixels();

      for (LXPoint p : model.points) {
        PVector v = new PVector(p.x, p.y, p.z).sub(origin);
        double ax = Math.abs(v.x);
        double ay = Math.abs(v.y);
        double az = Math.abs(v.z);

        // Ignore pixels outside the bounding box.
        if (ax > bboxSize.x/2 || ay > bboxSize.y/2 || az > bboxSize.z/2) {
          continue;
        }

        // Avoid division by zero.
        if (ax == 0 && ay == 0 && az == 0) {
          colors[p.index] = 0;
          continue;
        }

        // Select the face according to the component with the largest absolute value.
        if (ax > ay && ax > az) {
          if (v.x > 0) {  // Right face
            colors[p.index] = getColor(2*faceRes, faceRes, v.z/ax, -v.y/ax);
          } else {  // Left face
            colors[p.index] = getColor(0, faceRes, -v.z/ax, -v.y/ax);
          }
        } else if (ay > ax && ay > az) {
          if (v.y > 0) {  // Up face
            colors[p.index] = getColor(faceRes, 0, v.x/ay, -v.z/ay);
          } else {  // Down face
            colors[p.index] = getColor(faceRes, 2*faceRes, v.x/ay, v.z/ay);
          }
        } else {
          if (v.z > 0) {  // Back face
            colors[p.index] = getColor(3*faceRes, faceRes, -v.x/az, -v.y/az);
          } else {  // Front face
            colors[p.index] = getColor(faceRes, faceRes, v.x/az, -v.y/az);
          }
        }
      }
    }
  };

  private double deltaMsAccumulator = 0;

  @Override
  final protected void run(final double deltaMs) {
    DrawHelper.queueJob(id, this.run);
  }

  // Implement this method; it should paint the cubemap image onto pg.
  abstract void run(double deltaMs, PGraphics pg);

  /**
   * Gets a pixel colour from a selected face in the cubemap image.  The
   * face is expected to be the square from (faceMinX, faceMinY) to
   * (faceMinX + faceRes, faceMinY + faceRes), and a point within the
   * square is located using (u, v), which ranges from (-1, -1) to (1, 1).
   *
   * @param faceMinX The minimum x-value of a face in the cubemap image.
   * @param faceMinY The minimum y-value of a face in the cubemap image.
   * @param u A texture coordinate within the face, ranging from -1 to 1.
   * @param v A texture coordinate within the face, ranging from -1 to 1.
   */
  private int getColor(int faceMinX, int faceMinY, double u, double v) {
    if (u < -1 || u > 1 || v < -1 || v > 1) {
      return 0;
    }
    double offsetX = ((u + 1) / 2) * faceRes;
    double offsetY = ((v + 1) / 2) * faceRes;
    double x = faceMinX + offsetX;
    double y = faceMinY + offsetY;
    return pg.pixels[(int) x + ((int) y)*pg.width];
  }
}

public abstract static class MultiCubeMapPattern extends SLPattern {
  private P3LX lx;
  private List<Subpattern> subpatterns;

  protected MultiCubeMapPattern(LX lx, Class<? extends Subpattern> subpatternClass, int faceRes) {
    super(lx);

    subpatterns = new ArrayList<Subpattern>(model.suns.size());

    if (lx instanceof P3LX) {
      this.lx = (P3LX)lx;

      boolean isNonStaticInnerClass = (subpatternClass.isMemberClass() || subpatternClass.isLocalClass())
          && !Modifier.isStatic(subpatternClass.getModifiers());

      int sunIndex = 0;
      for (Sun sun : model.suns) {
        try {
          Subpattern subpattern;
          if (isNonStaticInnerClass) {
            subpattern = subpatternClass.getDeclaredConstructor(getClass()).newInstance(this);
          }
          else {
            subpattern = subpatternClass.getDeclaredConstructor().newInstance();
          }
          subpattern.init(this.lx, colors, sun, faceRes, sunIndex);
          subpatterns.add(subpattern);
        }
        catch (Exception e) {
          System.err.println("Exception when creating subpattern: " + e.getLocalizedMessage());
          e.printStackTrace();
        }

        ++sunIndex;
      }
    }
  }

  @Override
  public void run(double deltaMs) {
    for (Subpattern subpattern : subpatterns) {
      subpattern.run(deltaMs);
    }
  }

  public abstract static class Subpattern {
    protected P3LX lx;
    private int[] colors;
    private Sun sun;

    private PGraphics pg;
    protected PGraphics pgF, pgB, pgL, pgR, pgU, pgD;

    private PVector origin;
    private PVector bboxSize;
    private int faceRes;

    private final String id = "" + Math.random();

    protected int sunIndex;

    /**
     * A pattern that projects a cubemap image onto all the LEDs inside a given
     * bounding box in world space.  The cubemap image should have resolution 4k x 3k,
     * where each face of the cube takes up a k x k square, and the six faces are
     * arranged thus (where R/L are +x/-x of the origin, U/D are +y/-y, F/B are -z/+z):
     *         +---+
     *         | U |
     *     +---+---+---+---+
     *     | L | F | R | B |
     *     +---+---+---+---+
     *         | D |
     *         +---+
     * Note that the +z side is the back (B), not the front (F) as you might expect,
     * because Processing, insanely, uses a left-handed coordinate system.
     *
     * @param lx The global P3LX object.
     * @param origin The center of the bounding box in world space.
     * @param bboxSize The length, width, and height of the bounding box in world space.
     * @param faceRes The width and height, k, in pixels of one square face of the
     *     cubemap image, which will have total width 4k and total height 3k.
     */
    private void init(P3LX lx, int[] colors, Sun sun, int faceRes, int sunIndex) {
      this.lx = lx;
      this.colors = colors;
      this.sun = sun;
      this.sunIndex = sunIndex;

      PVector origin = new PVector( //<>//
      sun.boundingBox.origin.x + sun.boundingBox.size.x*.5,
      sun.boundingBox.origin.y + sun.boundingBox.size.y*.5,
      sun.boundingBox.origin.z + sun.boundingBox.size.z*.5);
      PVector bboxSize = sun.boundingBox.size;

      this.pg = lx.applet.createGraphics(faceRes*4, faceRes*3, P3D); //<>//
      this.pgF = lx.applet.createGraphics(faceRes, faceRes, P3D);
      this.pgB = lx.applet.createGraphics(faceRes, faceRes, P3D);
      this.pgL = lx.applet.createGraphics(faceRes, faceRes, P3D);
      this.pgR = lx.applet.createGraphics(faceRes, faceRes, P3D);
      this.pgU = lx.applet.createGraphics(faceRes, faceRes, P3D);
      this.pgD = lx.applet.createGraphics(faceRes, faceRes, P3D);

      this.origin = origin;
      this.bboxSize = bboxSize;
      this.faceRes = faceRes;
    }

    private Runnable run = new Runnable() {
      long lastRunAt = System.currentTimeMillis();
 //<>//
      @Override
      public void run() {
        double deltaMs = System.currentTimeMillis() - lastRunAt;
        lastRunAt = System.currentTimeMillis();

        //pg.beginDraw(); //<>//
        Subpattern.this.run(deltaMs, pg);
        //pg.endDraw();
        pg.loadPixels();

        for (LXPoint p : sun.points) {
          PVector v = new PVector(p.x, p.y, p.z).sub(origin);
          double ax = Math.abs(v.x);
          double ay = Math.abs(v.y);
          double az = Math.abs(v.z);

          // Avoid division by zero.
          if (ax == 0 && ay == 0 && az == 0) {
            colors[p.index] = 0;
            continue;
          }

          // Select the face according to the component with the largest absolute value.
          if (ax > ay && ax > az) {
            if (v.x > 0) {  // Right face
              colors[p.index] = getColor(2*faceRes, faceRes, v.z/ax, -v.y/ax);
            } else {  // Left face
              colors[p.index] = getColor(0, faceRes, -v.z/ax, -v.y/ax);
            }
          } else if (ay > ax && ay > az) {
            if (v.y > 0) {  // Up face
              colors[p.index] = getColor(faceRes, 0, v.x/ay, -v.z/ay);
            } else {  // Down face
              colors[p.index] = getColor(faceRes, 2*faceRes, v.x/ay, v.z/ay);
            }
          } else {
            if (v.z > 0) {  // Back face
              colors[p.index] = getColor(3*faceRes, faceRes, -v.x/az, -v.y/az);
            } else {  // Front face
              colors[p.index] = getColor(faceRes, faceRes, v.x/az, -v.y/az);
            }
          }
        }
      }
    };

    private double deltaMsAccumulator = 0;

    protected final void run(final double deltaMs) {
      DrawHelper.queueJob(id, this.run);
    }

    // Implement this method; it should paint the cubemap image onto pg.
    abstract void run(double deltaMs, PGraphics pg);

    /**
     * Gets a pixel colour from a selected face in the cubemap image.  The
     * face is expected to be the square from (faceMinX, faceMinY) to
     * (faceMinX + faceRes, faceMinY + faceRes), and a point within the
     * square is located using (u, v), which ranges from (-1, -1) to (1, 1).
     *
     * @param faceMinX The minimum x-value of a face in the cubemap image.
     * @param faceMinY The minimum y-value of a face in the cubemap image.
     * @param u A texture coordinate within the face, ranging from -1 to 1.
     * @param v A texture coordinate within the face, ranging from -1 to 1.
     */
    private int getColor(int faceMinX, int faceMinY, double u, double v) {
      if (u < -1 || u > 1 || v < -1 || v > 1) {
        return 0;
      }
      double offsetX = ((u + 1) / 2) * faceRes;
      double offsetY = ((v + 1) / 2) * faceRes;
      double x = faceMinX + offsetX;
      double y = faceMinY + offsetY;
      return pg.pixels[(int) x + ((int) y)*pg.width];
    }
  }

}



/**
 * An example of a cube-mapped pattern.  This paints solid colours on the six faces
 * of the cube, with markers to show their orientation, black stripes to show how
 * the texture is distorted by the projection, and grey lines to show how the cube
 * is formed by folding up the flat cubemap image.
 */
public class TestCube extends P3CubeMapPattern {
  public TestCube(LX lx) {
    // These parameters project this cubemap onto one of the 3/4-height suns.
    // See buildModel() in Mappings.pde for the sun positions.
    super((P3LX) lx, new PVector(55*12 + 8*12, 4*12, 2*12 + 0.3*8*12), new PVector(16*12, 16*12, 16*12*0.3), 100);
  }

  void run(double deltaMs, PGraphics pg) {
    pg.beginDraw();
    pg.background(0);

    pg.rectMode(CORNERS);
    pg.strokeWeight(0);

    // Fill the faces (red for L/R, green for U/D, blue for F/B).
    // To indicate orientation, add a white square at the +x, +y, +z corners.
    pg.fill(255, 0, 0);
    pg.rect(0, 100, 100, 200);  // left face
    pg.rect(200, 100, 300, 200);  // right face
    pg.fill(255);
    pg.rect(10, 110, 30, 130);  // +y, +z highlight
    pg.rect(270, 110, 290, 130);  // +y, +z highlight

    pg.fill(0, 255, 0);
    pg.rect(100, 0, 200, 100);  // up face
    pg.rect(100, 200, 200, 300);  // down face
    pg.fill(255);
    pg.rect(170, 10, 190, 30);  // +x, +z highlight
    pg.rect(170, 270, 190, 290);  // +x, +z highlight

    pg.fill(0, 0, 255);
    pg.rect(100, 100, 200, 200);  // front face
    pg.rect(300, 100, 400, 200);  // back face
    pg.fill(255);
    pg.rect(170, 110, 190, 130);  // +x, +y highlight
    pg.rect(310, 110, 330, 130);  // +x, +y highlight

    // Draw a black grid so that all faces are striped.
    pg.stroke(0);
    pg.strokeWeight(6);
    pg.beginShape(LINES);
    for (int i = 10; i < 400; i += 20) {
      pg.line(0, i, 400, i);
      pg.line(i, 0, i, 300);
    }
    pg.endShape();

    // A grey cross shows how the cube folds up, with the cross centered on the front face.
    pg.stroke(128);
    pg.strokeWeight(10);
    pg.line(40, 150, 360, 150);
    pg.line(150, 40, 150, 260);
    pg.endDraw();
  }
}

/**
 * A simple job holding class that allows patterns to queue rendering work for the main processing thread, which is
 * required for using OpenGL and such.
 *
 * Simply call DrawHelper.queueJob(id, someRunnable) to have that code executed on the main thread.
 *
 * The id should be a unique value per instance of the pattern or other component. Only the latest job added for an id
 * will be executed.
 */
public static class DrawHelper {
  private static Map<String, Runnable> jobs = Collections.synchronizedMap(new HashMap<String, Runnable>());

  public static void queueJob(String id, Runnable job) {
    jobs.put(id, job);
  }

  public static void runAll() {
    List<Runnable> ourJobs;

    synchronized (jobs) {
      ourJobs = new ArrayList<Runnable>(jobs.values());
      jobs.clear();
    }

    for (Runnable job : ourJobs) {
      job.run();
    }
  }
}
