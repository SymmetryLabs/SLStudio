package art.lookingup;

public class Bezier {
    public Point start;
    public Point c1;
    public Point c2;
    public Point end;
    //  Allow the beziers to be chained.  We need to know previous and next so that we can implement
    // control point constraints to keep the curve continuous.
    // TODO(allow adjustment of control point distance to end/start part while preserving the slope
    // of the line between control point end/start points and opposing control point in order
    // to preserve first derivative continuity.
    public Bezier prev;
    public Bezier next;
    float[] arcLengths;
    public float totalArcLength;
    public static final int ARC_SAMPLES = 300;

    public Bezier(Point s, Point c1, Point c2, Point e) {
        start = s;
        this.c1 = c1;
        this.c2 = c2;
        end = e;
        computeArcLengths();
    }

    public Point calculateBezierPoint(float t) {
        return calculateBezierPoint(t, start, c1, c2, end);
    }

    /* t is time(value of 0.0f-1.0f; 0 is the start 1 is the end) */
    static public Point calculateBezierPoint(float t, Point s, Point c1, Point c2, Point e)
    {
        float u = 1 - t;
        float tt = t*t;
        float uu = u*u;
        float uuu = uu * u;
        float ttt = tt * t;

        Point p = new Point(s.x * uuu, s.y * uuu);
        p.x += 3 * uu * t * c1.x;
        p.y += 3 * uu * t * c1.y;
        p.x += 3 * u * tt * c2.x;
        p.y += 3 * u * tt * c2.y;
        p.x += ttt * e.x;
        p.y += ttt * e.y;

        return p;
    }

    /**
     * Compute a lookup table of arc lengths.  This samples t values at points along
     * the curve and just does a simple linear distance computation. With the lookup
     * table we can ask for a distance along the curve and get the 't' value at that
     * point on the curve.  We can then use that 't' value to compute our X and Y
     * coordinates.
     */
    public void computeArcLengths() {
        arcLengths = new float[ARC_SAMPLES];
        Point prevPoint = start;
        float ox = 0f;
        float oy = 0f;
        float clen = 0f;

        for (int i = 0; i < arcLengths.length; i++) {
            Point nextPoint = calculateBezierPoint( i * 1f / arcLengths.length);
            float dx = nextPoint.x - prevPoint.x;
            float dy = nextPoint.y - prevPoint.y;
            clen += Math.sqrt(dx * dx + dy * dy);
            arcLengths[i] = clen;
            prevPoint = nextPoint;
        }
        totalArcLength = clen;
        // logger.info("Bezier total arc length: " + totalArcLength);
    }

    public float getTAtArcLength(float arcLength) {
        if (arcLength < arcLengths[0])
            return 0f;
        for (int i = 1; i < arcLengths.length; i++) {
            if (arcLength < arcLengths[i] && arcLength > arcLengths[i-1]) {
                return (((float)(i - 1))/(float)ARC_SAMPLES);
            }
        }
        return 1f;
    }

    static public class Point {
        public float x;
        public float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

}
