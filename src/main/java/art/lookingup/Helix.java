package art.lookingup;

public class Helix {
    public static final int ARC_SAMPLES = 1000;
    public static final float MAX_T = 40f;
    float[] arcLengths;
    public float totalArcLength;
    float slope;
    float radius;

    public Helix(float radius, float slope) {
        this.slope = slope;
        this.radius = radius;
        computeArcLengths(MAX_T);
    }

    public Point3 calculateHelicalPoint(float t)
    {
        Point3 p = new Point3();
        p.x = radius * (float)Math.cos(t);
        p.z = radius * (float)Math.sin(t);
        p.y = t * slope;

        return p;
    }

    /**
     * Compute a lookup table of arc lengths.  This samples t values at points along
     * the curve and just does a simple linear distance computation. With the lookup
     * table we can ask for a distance along the curve and get the 't' value at that
     * point on the curve.  We can then use that 't' value to compute our X and Y
     * coordinates.
     */
    public void computeArcLengths(float maxT) {
        arcLengths = new float[ARC_SAMPLES];
        Point3 prevPoint = new Point3();
        float ox = 0f;
        float oy = 0f;
        float clen = 0f;
        prevPoint.x = 0f;
        prevPoint.y = 0f;
        prevPoint.z = 0f;

        prevPoint = calculateHelicalPoint(0f);

        for (int i = 0; i < arcLengths.length; i++) {
            float t = (maxT * i + 1) / (float)ARC_SAMPLES;
            Point3 nextPoint = calculateHelicalPoint(  t);
            float dx = nextPoint.x - prevPoint.x;
            float dy = nextPoint.y - prevPoint.y;
            float dz = nextPoint.z - prevPoint.z;
            clen += Math.sqrt(dx * dx + dy * dy + dz * dz);
            arcLengths[i] = clen;
            prevPoint = nextPoint;
        }
        Point3 maxP = calculateHelicalPoint(maxT);
        //logger.info("max helical point: " + maxP.x + "," + maxP.y + "," + maxP.z);
        totalArcLength = clen;
        //logger.info("HELIX total arc length: " + totalArcLength);
    }

    public float getTAtArcLength(float arcLength) {
        if (arcLength < Math.abs(0.01f))
            return 0f;
        for (int i = 0; i < arcLengths.length; i++) {
            if (arcLength == arcLengths[i]) {
                return MAX_T * (((float)(i))/(float)ARC_SAMPLES);
            }
            if (arcLength < arcLengths[i] && arcLength > arcLengths[i-1]) {
                float moreThanT = (((float)(i - 1))/(float)ARC_SAMPLES);
                float lessThanT = (((float)(i))/(float)ARC_SAMPLES);
                float avgT = (moreThanT + lessThanT) / 2f;
                return MAX_T * avgT;
            }
        }
        return 1f;
    }

    static public class Point3 {
        float x;
        float y;
        float z;
    }

}
