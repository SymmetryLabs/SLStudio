
public static abstract class ModelIndex {
  protected final LXFixture fixture;

  public ModelIndex(LXFixture fixture) {
    this.fixture = fixture;
  }

  protected float pointDistance(LXPoint a, LXPoint b) {
    float x_diff = a.x - b.x;
    float y_diff = a.y - b.y;
    float z_diff = a.z - b.z;
    return (float)Math.sqrt(x_diff * x_diff + y_diff * y_diff + z_diff * z_diff);
  }

  public abstract List<PointDist> pointsWithin(LXPoint target, float d);
  public abstract PointDist nearestPoint(LXPoint target);

  public static class PointDist {
    public final LXPoint p;
    public final float d;

    public PointDist(LXPoint p, float d) {
      this.p = p;
      this.d = d;
    }
  }
}

public static class LinearIndex extends ModelIndex {
  public LinearIndex(LXFixture fixture) {
    super(fixture);
  }

  @Override
  public List<PointDist> pointsWithin(LXPoint target, float d) {
    List<PointDist> nearbyPoints = new ArrayList<PointDist>();
    for (LXPoint p : fixture.getPoints()) {
      float pd = pointDistance(target, p);
      if (pd <= d) {
        nearbyPoints.add(new PointDist(p, pd));
      }
    }
    return nearbyPoints;
  }

  @Override
  public PointDist nearestPoint(LXPoint target) {
    float nearestDist = 0;
    LXPoint nearestPoint = null;
    for (LXPoint p : fixture.getPoints()) {
      float d = pointDistance(target, p);
      if (nearestPoint == null || d < nearestDist) {
        nearestPoint = p;
        nearestDist = d;
      }
    }
    return nearestPoint == null ? null : new PointDist(nearestPoint, nearestDist);
  }
}

// public static class KDTreeIndex extends ModelIndex {
//   private KDTree<LXPoint> kd;

//   public KDTreeIndex(LXFixture fixture) {
//     super(fixture);

//     kd = new KDTree<LXPoint>(3);

//     for (LXPoint point : fixture.getPoints()) {
//       try {
//         kd.insert(new double[] {point.x, point.y, point.z}, point);
//       }
//       catch (Exception e) {
//         System.err.println("Exception while building KDTree: " + e.getMessage());
//       }
//     }
//   }

//   @Override
//   public List<PointDist> pointsWithin(LXPoint target, float d) {
//     List<PointDist> nearbyPoints = new ArrayList<PointDist>();

//     List<LXPoint> nearby = null;

//     try {
//       nearby = kd.nearestEuclidean(new double[] {target.x, target.y, target.z}, d);
//     }
//     catch (Exception e) {
//       System.err.println("Exception while finding nearest points: " + e.getMessage());
//     }

//     if (nearby == null)
//       return nearbyPoints;

//     for (LXPoint p : nearby) {
//       float pd = pointDistance(target, p);
//       nearbyPoints.add(new PointDist(p, pd));
//     }

//     return nearbyPoints;
//   }

//   @Override
//   public PointDist nearestPoint(LXPoint target) {
//     LXPoint nearest = null;
//     try {
//       nearest = kd.nearest(new double[] {target.x, target.y, target.z});
//     }
//     catch (Exception e) {
//       System.err.println("Exception while finding nearest point: " + e.getMessage());
//     }

//     if (nearest == null)
//       return null;

//     return new PointDist(nearest, pointDistance(target, nearest));
//   }
// }

public static class OctreeIndex extends ModelIndex {
  private FixedWidthOctree<LXPoint> ot;

  public OctreeIndex(LXModel model) {
    this(model, false);
  }

  public OctreeIndex(LXModel model, boolean flattenZ) {
    super(model);

    ot = new FixedWidthOctree<LXPoint>(model.cx, model.cy, model.cz,
                max(model.xRange, model.yRange, model.zRange), 5);

    for (LXPoint point : model.getPoints()) {
      try {
        ot.insert(point.x, point.y, flattenZ ? 0 : point.z, point);
      }
      catch (Exception e) {
        System.err.println("Exception while building Octree: " + e.getMessage());
      }
    }
  }

  @Override
  public List<PointDist> pointsWithin(LXPoint target, float d) {
    List<PointDist> nearbyPoints = new ArrayList<PointDist>();

    List<LXPoint> nearby = null;

    try {
      nearby = ot.withinDistance((float)target.x, (float)target.y, (float)target.z, d);
    }
    catch (Exception e) {
      System.err.println("Exception while finding nearest points: " + e.getMessage());
    }

    if (nearby == null)
      return nearbyPoints;

    for (LXPoint p : nearby) {
      float pd = pointDistance(target, p);
      nearbyPoints.add(new PointDist(p, pd));
    }

    return nearbyPoints;
  }

  @Override
  public PointDist nearestPoint(LXPoint target) {
    LXPoint nearest = null;
    try {
      nearest = ot.nearest((float)target.x, (float)target.y, (float)target.z);
    }
    catch (Exception e) {
      System.err.println("Exception while finding nearest point: " + e.getMessage());
    }

    if (nearest == null)
      return null;

    return new PointDist(nearest, pointDistance(target, nearest));
  }
}
