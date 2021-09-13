package art.lookingup;

import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KaledoscopeModel extends SLModel {

    public static float butterflySpacingInches = 12f;
    public static float lineSpacingInches = 24f;

    private static final Logger logger = Logger.getLogger(KaledoscopeModel.class.getName());

    public static List<LUButterfly> allButterflies;
    public static List<LUFlower> allFlowers;
    public static List<Run> allRuns;
    public static List<Run> allButterflyRuns;
    public static List<Run> allFlowerRuns;
    public static List<Strand> allStrands;
    public static int numStrandsPerRun;
    public static float butterflyYHeight = 120f;
    public static final int BEZIERS_PER_RUN = 4;

    /**
     * A Strand is some number of butterflies wired in series. Strands are meant to limit the number of LEDs on a
     * single output to manage FPS.  A 'Run' of butterflies is a series of strands.  Typically a strand would receive
     * data via a Pixlite long range receiver or something similar.
     *
     * A Strand can also be a number of flowers wired in series.
     */
    static public class Strand {
        // The global strandId.  These are allocated as we build the model.
        int strandId;
        // The index number of this strand on a particular run
        public int strandRunIndex;
        public enum StrandType {
            BUTTERFLY,
            FLOWER
        }
        StrandType strandType;
        public Run run;

        public List<LUButterfly> butterflies;
        public List<LUFlower> flowers;
        public List<LXPoint> allPoints;
        public List<LXPoint> addressablePoints;


        public Strand(Run run, AnchorTree tree, int strandId, StrandType strandType, int strandRunIndex) {
            this.strandId = strandId;
            this.run = run;
            this.strandType = strandType;
            flowers = new ArrayList<LUFlower>();
            butterflies = new ArrayList<LUButterfly>();
            allPoints = new ArrayList<LXPoint>();
            addressablePoints  = new ArrayList<LXPoint>();
            this.strandRunIndex = strandRunIndex;

            int configuredNumFlowers = FireflyShow.allStrandLengths.get(strandId);
            logger.log(Level.INFO, "Generating flower strand id " + strandId + " of length: " + configuredNumFlowers);
            float flowerSpacing = 12f;
            float flowerMaxHeight = 110f;
            for (int i = 0; i < configuredNumFlowers; i++) {
                int prevStrandsFlowers = run.flowers.size();
                // Flowers are wired from top to bottom since the wiring will be high up in the tree.  We need to
                // compute every 12 inches along a helix that started at the top of the run.
                float flowerRunArcDistance = (i + prevStrandsFlowers) * flowerSpacing;
                float t = tree.helix.getTAtArcLength(flowerRunArcDistance);
                Point3 pos = tree.helix.calculateHelicalPoint(t);
                LUFlower flower = new LUFlower(i, i + prevStrandsFlowers, pos.x + tree.x, flowerMaxHeight - pos.y, pos.z + tree.z);
                flowers.add(flower);
                allFlowers.add(flower);
                allPoints.addAll(flower.allPoints);
                addressablePoints.addAll(flower.addressablePoints);
            }
        }

        public Strand(Run run, int strandId, int strandRunIndex, List<Bezier> beziers) {
            this.strandId = strandId;
            strandType = StrandType.BUTTERFLY;
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();
            addressablePoints = new ArrayList<LXPoint>();

            this.strandRunIndex = strandRunIndex;
            // The number of configured butterflies on this strand.
            int configuredNumButterflies = FireflyShow.allStrandLengths.get(strandId);

            for (int i = 0; i < configuredNumButterflies; i++) {
                // How many butterflies so far on this run.

                int prevStrandsButterflies = run.butterflies.size();
                int currentButterflyRunIndex= prevStrandsButterflies + i;
                float runStartOffsetInches = 2f * 12f;
                // Compute the feet from the beginning of the run, including any start offset.
                float currentButterflyArcDistance = runStartOffsetInches + currentButterflyRunIndex * butterflySpacingInches;
                int currentBezierIndex = getBezierSegmentIndexByDistance(currentButterflyArcDistance, beziers);
                float prevCurveDistance = previousCurveArcLengths(currentBezierIndex, beziers);
                float butterflyThisCurveDistance = currentButterflyArcDistance - prevCurveDistance;
                Bezier bezier = beziers.get(currentBezierIndex);
                float thisCurveT = bezier.getTAtArcLength(butterflyThisCurveDistance);
                Point bPos = bezier.calculateBezierPoint(thisCurveT);

                LUButterfly butterfly = new LUButterfly(i, i + prevStrandsButterflies, bPos.x, butterflyYHeight, bPos.y);
                butterflies.add(butterfly);
                allButterflies.add(butterfly);
                allPoints.addAll(butterfly.allPoints);
                addressablePoints.addAll(butterfly.allPoints);
            }
        }

        public void recomputeBeziers(List<Bezier> beziers) {
            for (int i = 0; i < butterflies.size(); i++) {
                Point bPos = computeButterflyPosition(beziers, i);
                butterflies.get(i).updatePosition(bPos);
            }
        }

        /**
         * Given the Nth butterfly on this strand and a list of bezier curves, compute the position of
         * this butterfly.
         *
         * @param beziers
         * @param butterflyStrandIndex
         * @return
         */
        public Point computeButterflyPosition(List<Bezier> beziers, int butterflyStrandIndex) {

            int prevStrandsButterflies = 0;
            for (int strandNum = 0; strandNum < strandRunIndex; strandNum++) {
                prevStrandsButterflies += run.strands.get(strandNum).butterflies.size();
            }
            int currentButterflyRunIndex = prevStrandsButterflies + butterflyStrandIndex;
            float runStartOffsetInches = 2f * 12f;
            // Compute the feet from the beginning of the run, including any start offset.
            float currentButterflyArcDistance = runStartOffsetInches + currentButterflyRunIndex * butterflySpacingInches;
            int currentBezierIndex = getBezierSegmentIndexByDistance(currentButterflyArcDistance, beziers);
            float prevCurveDistance = previousCurveArcLengths(currentBezierIndex, beziers);
            float butterflyThisCurveDistance = currentButterflyArcDistance - prevCurveDistance;
            Bezier bezier = beziers.get(currentBezierIndex);
            float thisCurveT = bezier.getTAtArcLength(butterflyThisCurveDistance);
            Point bPos = bezier.calculateBezierPoint(thisCurveT);
            return bPos;
        }

        /**
         * Given a targeted arc length, return the Bezier curve that contains that arc length position.
         * @param arcLength
         * @param beziers
         * @return
         */
        public int getBezierSegmentIndexByDistance(float arcLength, List<Bezier> beziers) {
            float totalBezierLen = 0f;
            for (int i = 0; i < beziers.size(); i++) {
                totalBezierLen += beziers.get(i).totalArcLength;
                if (arcLength < totalBezierLen) {
                    return i;
                }
            }
            logger.warning("Butterfly requested arc length position: " + arcLength + " is longer than total bezier curve lengths: " + totalBezierLen);
            return beziers.size() - 1;
        }

        /**
         * Given a curveIndex and an array of Bezier curves, compute the total arc length distance of the previous curves.
         *
         * @param curveIndex
         * @param beziers
         * @return
         */
        public float previousCurveArcLengths(int curveIndex, List<Bezier> beziers) {
            float totalLength = 0f;
            for (int i = 0; i < curveIndex; i++) {
                totalLength += beziers.get(i).totalArcLength;
            }
            return totalLength;
        }
    }

    static public class Bezier {
        public Point start;
        public Point c1;
        public Point c2;
        public Point end;
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
            logger.info("Bezier total arc length: " + totalArcLength);
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
    }

    /**
     * A Run is a single full line of butterflies.  It is composed of multiple strands wired in series.  The
     * purpose of a strand is to limit the number of LEDs on a single output in order to increase the FPS.
     * A run also consists of a series of bezier curves to model the curvature of the wires.
     */
    static public class Run {
        public List<LXPoint> allPoints;
        public List<Strand> strands;
        public List<LUButterfly> butterflies;
        public List<LUFlower> flowers;
        Bezier bezier1;
        Bezier bezier2;
        Bezier bezier3;
        Bezier bezier4;
        public List<Bezier> beziers;
        int runIndex;
        float cxOffset = 70f;
        float cyOffset = 20f;

        public enum RunType {
            BUTTERFLY,
            FLOWER
        }
        RunType runType;

        public Run(int runIndex, RunType runType, AnchorTree tree) {
            this.runIndex = runIndex;
            this.runType = runType;
            strands = new ArrayList<Strand>();
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();

            // To make numStrands configurable do like this after adding UI
            // int numStrands = FireflyShow.butterflyRunsNumStrands.get(runIndex);
            int numStrands = 1;
            for (int i = 0; i < numStrands; i++) {
                Strand strand = new Strand(this, tree, allStrands.size(), Strand.StrandType.FLOWER, i);
                allPoints.addAll(strand.allPoints);
                flowers.addAll(strand.flowers);
                allStrands.add(strand);
                strands.add(strand);
            }
        }

        public Run(int runIndex, List<AnchorTree> trees) {
            this.runIndex = runIndex;
            this.runType = RunType.BUTTERFLY;
            int numStrands = FireflyShow.butterflyRunsNumStrands.get(runIndex);
            float treeMargin = 2f;

            // Run index == 0 needs to be to the left of tree.
            // Run index == 1 needs to be to the right of tree.
            float startX = trees.get(0).x - (trees.get(0).radius + treeMargin);
            if (runIndex == 1)
                startX = trees.get(0).x + (trees.get(0).radius + treeMargin);
            Point bezierStart = new Point(startX, trees.get(0).z);
            float endX = trees.get(1).x - (trees.get(1).radius + treeMargin);
            if (runIndex == 1)
                endX = trees.get(1).x + (trees.get(1).radius + treeMargin);
            Point bezierEnd = new Point(endX, trees.get(1).z);
            Point bezierC1 = new Point(bezierStart.x, bezierStart.y + cyOffset);
            Point bezierC2 = new Point(bezierEnd.x, bezierEnd.y - cyOffset);
            bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


            Point b2Start = new Point(bezierEnd.x, bezierEnd.y);
            float endX2 = trees.get(2).x - (trees.get(2).radius + treeMargin);
            if (runIndex == 1)
                endX2 = trees.get(2).x + (trees.get(2).radius + treeMargin);
            Point b2End = new Point(endX2, trees.get(2).z);
            Point b2C1 = new Point(b2Start.x, b2Start.y + cyOffset);
            Point b2C2 = new Point(b2End.x, b2End.y - cyOffset);
            bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);

            Point b3Start = new Point(b2End.x, b2End.y);
            float endX3 = trees.get(3).x - (trees.get(3).radius + treeMargin);
            if (runIndex == 1)
                endX3 = trees.get(3).x + (trees.get(3).radius + treeMargin);
            Point b3End = new Point(endX3, trees.get(3).z);
            Point b3C1 = new Point(b3Start.x, b3Start.y + cyOffset);
            Point b3C2 = new Point(b3End.x, b3End.y - cyOffset);
            bezier3 = new Bezier(b3Start, b3C1, b3C2, b3End);

            Point b4Start = new Point(b3End.x, b3End.y);
            float endX4 = trees.get(4).x - (trees.get(4).radius + treeMargin);
            if (runIndex == 1)
                endX4 = trees.get(4).x + (trees.get(4).radius + treeMargin);
            Point b4End = new Point(endX4, trees.get(4).z);
            Point b4C1 = new Point(b4Start.x, b4Start.y + cyOffset);
            Point b4C2 = new Point(b4End.x, b4End.y - cyOffset);
            bezier4 = new Bezier(b4Start, b4C1, b4C2, b4End);

            strands = new ArrayList<Strand>();
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();
            beziers = new ArrayList<Bezier>();
            beziers.add(bezier1);
            beziers.add(bezier2);
            beziers.add(bezier3);
            beziers.add(bezier4);
            for (int i = 0; i < numStrands; i++) {
                Strand strand = new Strand(this, allStrands.size(), i, beziers);
                allPoints.addAll(strand.allPoints);
                butterflies.addAll(strand.butterflies);
                allStrands.add(strand);
                strands.add(strand);
            }
        }
    }

    static public class Point3 {
        float x;
        float y;
        float z;
    }

    /**
     * Anchor trees are the trees that the cabling is anchored to.  The tree positions determine the flower strand
     * locations and the control points on the bezier curves.  The diameter of the trees determine the helical shape
     * of the flower strands.
     *
     * We will perform the same procedure we used for computing bezier curve arc lengths.  Just sample many times
     * along the helix to compute a helix approximated by many short line segments.  We can then map those segments
     * to t values and then for a provided length add up segments until we achieve that length and then pick
     * a nearby t value.
     *
     * Five generated tree coordinates:
     * 60.0,12.0 : -60.0,252.0 : 60.0,492.0 : -60.0,732.0 : 60.0,972.0
     */
    static public class AnchorTree {
        public float x;
        public float z;
        public float radius;
        public Helix helix;

        public AnchorTree(float x, float z, float radius, float helixSlope) {
            this.x = x;
            this.z = z;
            logger.info("Anchor tree coordinates: " + x + "," + z);
            this.radius = radius;
            helix = new Helix(radius, helixSlope);
        }

    }


    static public class Helix {
        public static final int ARC_SAMPLES = 400;
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

            for (int i = 0; i < arcLengths.length; i++) {
                float t = (maxT * i) / (float)ARC_SAMPLES;
                Point3 nextPoint = calculateHelicalPoint(  t);
                float dx = nextPoint.x - prevPoint.x;
                float dy = nextPoint.y - prevPoint.y;
                float dz = nextPoint.z - prevPoint.z;
                clen += Math.sqrt(dx * dx + dy * dy + dz * dz);
                arcLengths[i] = clen;
                prevPoint = nextPoint;
            }
            Point3 maxP = calculateHelicalPoint(maxT);
            logger.info("max helical point: " + maxP.x + "," + maxP.y + "," + maxP.z);
            totalArcLength = clen;
            logger.info("HELIX total arc length: " + totalArcLength);
        }

        public float getTAtArcLength(float arcLength) {
            if (arcLength < arcLengths[0])
                return 0f;
            for (int i = 1; i < arcLengths.length; i++) {
                if (arcLength < arcLengths[i] && arcLength > arcLengths[i-1]) {
                    return MAX_T * (((float)(i - 1))/(float)ARC_SAMPLES);
                }
            }
            return 1f;
        }
    }

    static public KaledoscopeModel createModel(int numButterflyRuns) {
        List<LXPoint> allPoints = new ArrayList<LXPoint>();

        allRuns = new ArrayList<Run>();
        allButterflyRuns = new ArrayList<Run>(numButterflyRuns);
        allFlowerRuns = new ArrayList<Run>();
        allStrands = new ArrayList<Strand>();
        allButterflies = new ArrayList<LUButterfly>();
        allFlowers = new ArrayList<LUFlower>();
        List<LXPoint> butterflyPoints = new ArrayList<LXPoint>();
        List<LXPoint> flowerPoints = new ArrayList<LXPoint>();
        List<AnchorTree> anchorTrees = new ArrayList<AnchorTree>();

        float runSpacing = 20f * 12f;
        for (int i = 0; i < 5; i++){
            float x = -5f * 12f;
            if (i % 2 == 0)
                x += 10 * 12f;
            anchorTrees.add(new AnchorTree(FireflyShow.anchorTreesPos.get(i*2),
                FireflyShow.anchorTreesPos.get(i*2 + 1), 12f, 5f));
        }

        for (int i = 0; i < numButterflyRuns; i++) {
            Run run = new Run(i, anchorTrees);
            allRuns.add(run);
            allButterflyRuns.add(run);
            allPoints.addAll(run.allPoints);
            butterflyPoints.addAll(run.allPoints);
        }

        int flowerRuns = FireflyShow.runsFlowers;
        for (int i = 0; i < flowerRuns; i++) {
            Run run = new Run(allRuns.size(), Run.RunType.FLOWER, anchorTrees.get(i));
            allRuns.add(run);
            allFlowerRuns.add(run);
            allPoints.addAll(run.allPoints);
            flowerPoints.addAll(run.allPoints);
        }

        return new KaledoscopeModel(allPoints);
    }

    public KaledoscopeModel(List<LXPoint> points) {
        super("kaledoscope", points);
    }

    static public class Point {
        public float x;
        public float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * This method needs to be called when we move the control points on the bezier curves.  We will need to
     * recompute the positions of every LXPoint on the run.  This is similar to when we construct them except that
     * here they already exist.
     * @param run
     */
    static public void recomputeRunBezier(Run run) {
        logger.info("Recomputing run");
        for (Strand strand : run.strands) {
            strand.run = run;
            strand.recomputeBeziers(run.beziers);
        }
    }
}
