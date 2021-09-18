package art.lookingup;

import java.util.ArrayList;
import java.util.List;

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
        return 0f;
    }

    // Moved anchor tree bezier curve generation out of model creation to clean up the code over there
    // since we are not using this method.
    /**
     * Generate the run using Bezier curves.  We are currently not using this.
     *
     * @param runId
     * @param trees
     * @param xPos
     *
    public Run(int runId, List<KaledoscopeModel.AnchorTree> trees, float xPos) {
        this.runId = runId;
        this.runType = KaledoscopeModel.Run.RunType.BUTTERFLY;
        int numStrands = FireflyShow.butterflyRunsNumStrands.get(runId);
        float treeMargin = 2f;

        if (FireflyShow.runsButterflies == 2) {
            beziers = Bezier.getAnchorTreeBeziers(runId, trees, treeMargin);
        } else if (FireflyShow.runsButterflies == 1) {
            beziers = Bezier.get1RunBeziers(xPos);
        } else {
            beziers = Bezier.get3RunBeziers(xPos);
        }
        strands = new ArrayList<KaledoscopeModel.Strand>();
        butterflies = new ArrayList<LUButterfly>();
        flowers = new ArrayList<LUFlower>();
        allPoints = new ArrayList<LXPoint>();
        ptsRunIndex = new HashMap<Integer, Integer>();
        ptsRunInches = new HashMap<Integer, Float>();

        for (int i = 0; i < numStrands; i++) {
            KaledoscopeModel.Strand strand = new KaledoscopeModel.Strand(this, allStrands.size(), i, beziers);
            allPoints.addAll(strand.allPoints);
            butterflies.addAll(strand.butterflies);
            allStrands.add(strand);
            strands.add(strand);
        }
        int ptRunIndex = 0;
        for (LXPoint pt : allPoints) {
            ptsRunIndex.put(pt.index, ptRunIndex);
            ++ptRunIndex;
        }
    }
    */

    static final float bezierCtrlPtYOffset = 20f;
    /**
     * Generate the bezier curves for the butterflies based on the anchor tree locations.  This is for the
     * scenario with 2 runs of butterflies.
     *
     * @param trees
     * @param treeMargin
     * @return
     */
    static public List<Bezier> getAnchorTreeBeziers(int runId, List<AnchorTree> trees, float treeMargin) {
        List<Bezier> curves = new ArrayList<Bezier>();
        // Run index == 0 needs to be to the left of tree.
        // Run index == 1 needs to be to the right of tree.
        float startX = trees.get(0).p.x - (trees.get(0).p.radius + treeMargin);
        if (runId == 1)
            startX = trees.get(0).p.x + (trees.get(0).p.radius + treeMargin);
        Bezier.Point bezierStart = new Bezier.Point(startX, trees.get(0).p.z);
        float endX = trees.get(1).p.x - (trees.get(1).p.radius + treeMargin);
        if (runId == 1)
            endX = trees.get(1).p.x + (trees.get(1).p.radius + treeMargin);
        Bezier.Point bezierEnd = new Bezier.Point(endX, trees.get(1).p.z);
        Bezier.Point bezierC1 = new Bezier.Point(bezierStart.x, bezierStart.y + bezierCtrlPtYOffset);
        Bezier.Point bezierC2 = new Bezier.Point(bezierEnd.x, bezierEnd.y - bezierCtrlPtYOffset);
        Bezier bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


        Bezier.Point b2Start = new Bezier.Point(bezierEnd.x, bezierEnd.y);
        float endX2 = trees.get(2).p.x - (trees.get(2).p.radius + treeMargin);
        if (runId == 1)
            endX2 = trees.get(2).p.x + (trees.get(2).p.radius + treeMargin);
        Bezier.Point b2End = new Bezier.Point(endX2, trees.get(2).p.z);
        Bezier.Point b2C1 = new Bezier.Point(b2Start.x, b2Start.y + bezierCtrlPtYOffset);
        Bezier.Point b2C2 = new Bezier.Point(b2End.x, b2End.y - bezierCtrlPtYOffset);
        Bezier bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);

        Bezier.Point b3Start = new Bezier.Point(b2End.x, b2End.y);
        float endX3 = trees.get(3).p.x - (trees.get(3).p.radius + treeMargin);
        if (runId == 1)
            endX3 = trees.get(3).p.x + (trees.get(3).p.radius + treeMargin);
        Bezier.Point b3End = new Bezier.Point(endX3, trees.get(3).p.z);
        Bezier.Point b3C1 = new Bezier.Point(b3Start.x, b3Start.y + bezierCtrlPtYOffset);
        Bezier.Point b3C2 = new Bezier.Point(b3End.x, b3End.y - bezierCtrlPtYOffset);
        Bezier bezier3 = new Bezier(b3Start, b3C1, b3C2, b3End);

        Bezier.Point b4Start = new Bezier.Point(b3End.x, b3End.y);
        float endX4 = trees.get(4).p.x - (trees.get(4).p.radius + treeMargin);
        if (runId == 1)
            endX4 = trees.get(4).p.x + (trees.get(4).p.radius + treeMargin);
        Bezier.Point b4End = new Bezier.Point(endX4, trees.get(4).p.z);
        Bezier.Point b4C1 = new Bezier.Point(b4Start.x, b4Start.y + bezierCtrlPtYOffset);
        Bezier.Point b4C2 = new Bezier.Point(b4End.x, b4End.y - bezierCtrlPtYOffset);
        Bezier bezier4 = new Bezier(b4Start, b4C1, b4C2, b4End);
        curves.add(bezier1);
        curves.add(bezier2);
        curves.add(bezier3);
        curves.add(bezier4);

        return curves;
    }

    static public List<Bezier> get3RunBeziers(float pos) {
        List<Bezier> curves = new ArrayList<Bezier>();
        float curveEndpointDistance = 80f;
        float cxOffset= 70f;
        float cyOffset= 20f;

        Bezier.Point bezierStart = new Bezier.Point(pos, 0f);
        Bezier.Point bezierEnd = new Bezier.Point(pos, curveEndpointDistance);
        Bezier.Point bezierC1 = new Bezier.Point(bezierStart.x + cxOffset, bezierStart.y + cyOffset);
        Bezier.Point bezierC2 = new Bezier.Point(bezierEnd.x + cxOffset, bezierEnd.y - cyOffset);
        Bezier bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


        Bezier.Point b2Start = new Bezier.Point(bezierEnd.x, bezierEnd.y);
        Bezier.Point b2End = new Bezier.Point(bezierEnd.x, curveEndpointDistance * 2);
        Bezier.Point b2C1 = new Bezier.Point(b2Start.x - cxOffset, b2Start.y + cyOffset);
        Bezier.Point b2C2 = new Bezier.Point(b2End.x - cxOffset, b2End.y - cyOffset);
        Bezier bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);

        Bezier.Point b3Start = new Bezier.Point(b2End.x, b2End.y);
        Bezier.Point b3End = new Bezier.Point(b2End.x, curveEndpointDistance * 3);
        Bezier.Point b3C1 = new Bezier.Point(b3Start.x + cxOffset, b3Start.y + cyOffset);
        Bezier.Point b3C2 = new Bezier.Point(b3End.x + cxOffset, b3End.y - cyOffset);
        Bezier bezier3 = new Bezier(b3Start, b3C1, b3C2, b3End);

        Bezier.Point b4Start = new Bezier.Point(b3End.x, b3End.y);
        Bezier.Point b4End = new Bezier.Point(b3End.x, curveEndpointDistance * 4);
        Bezier.Point b4C1 = new Bezier.Point(b4Start.x - cxOffset, b4Start.y + cyOffset);
        Bezier.Point b4C2 = new Bezier.Point(b4End.x - cxOffset, b4End.y - cyOffset);
        Bezier bezier4 = new Bezier(b4Start, b4C1, b4C2, b4End);

        curves.add(bezier1);
        curves.add(bezier2);
        curves.add(bezier3);
        curves.add(bezier4);

        return curves;
    }

    /**
     * This should be enough curves to create a zigzag pattern.  We will just flatten the curves
     * to a line (c1 = start, c2 = end).
     * @param pos
     * @return
     */
    static public List<Bezier> get1RunBeziers(float pos) {
        List<Bezier> curves = new ArrayList<Bezier>();
        float curveEndpointDistance = 80f;
        float cxOffset= 70f;
        float cyOffset= 20f;

        Bezier.Point bezierStart = new Bezier.Point(pos, 0f);
        Bezier.Point bezierEnd = new Bezier.Point(pos, curveEndpointDistance);
        Bezier.Point bezierC1 = new Bezier.Point(bezierStart.x + cxOffset, bezierStart.y + cyOffset);
        Bezier.Point bezierC2 = new Bezier.Point(bezierEnd.x + cxOffset, bezierEnd.y - cyOffset);
        Bezier bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


        Bezier.Point b2Start = new Bezier.Point(bezierEnd.x, bezierEnd.y);
        Bezier.Point b2End = new Bezier.Point(bezierEnd.x, curveEndpointDistance * 2);
        Bezier.Point b2C1 = new Bezier.Point(b2Start.x - cxOffset, b2Start.y + cyOffset);
        Bezier.Point b2C2 = new Bezier.Point(b2End.x - cxOffset, b2End.y - cyOffset);
        Bezier bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);

        Bezier.Point b3Start = new Bezier.Point(b2End.x, b2End.y);
        Bezier.Point b3End = new Bezier.Point(b2End.x, curveEndpointDistance * 3);
        Bezier.Point b3C1 = new Bezier.Point(b3Start.x + cxOffset, b3Start.y + cyOffset);
        Bezier.Point b3C2 = new Bezier.Point(b3End.x + cxOffset, b3End.y - cyOffset);
        Bezier bezier3 = new Bezier(b3Start, b3C1, b3C2, b3End);

        Bezier.Point b4Start = new Bezier.Point(b3End.x, b3End.y);
        Bezier.Point b4End = new Bezier.Point(b3End.x, curveEndpointDistance * 4);
        Bezier.Point b4C1 = new Bezier.Point(b4Start.x - cxOffset, b4Start.y + cyOffset);
        Bezier.Point b4C2 = new Bezier.Point(b4End.x - cxOffset, b4End.y - cyOffset);
        Bezier bezier4 = new Bezier(b4Start, b4C1, b4C2, b4End);

        curves.add(bezier1);
        curves.add(bezier2);
        curves.add(bezier3);
        curves.add(bezier4);

        return curves;
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
