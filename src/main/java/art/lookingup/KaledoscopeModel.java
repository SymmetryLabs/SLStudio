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
    public static List<Strand> allStrands;
    public static int numStrandsPerRun;
    public static float butterflyYHeight = 120f;

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

        float x, y, z;

        public Strand(Run run, int strandId, StrandType strandType, float x, float y, float z, int strandRunIndex) {
            this.strandId = strandId;
            this.run = run;
            this.strandType = strandType;
            this.x = x;
            this.y = y;
            this.z = z;
            flowers = new ArrayList<LUFlower>();
            butterflies = new ArrayList<LUButterfly>();
            allPoints = new ArrayList<LXPoint>();
            addressablePoints  = new ArrayList<LXPoint>();

            int configuredNumFlowers = FireflyShow.allStrandLengths.get(strandId);
            logger.log(Level.INFO, "Generating flower strand id " + strandId + " of length: " + configuredNumFlowers);
            float flowerSpacing = 12f;
            for (int i = 0; i < configuredNumFlowers; i++) {
                int prevStrandsFlowers = run.flowers.size();
                // Flowers are wired from top to bottom since the wiring will be high up in the tree.
                LUFlower flower = new LUFlower(i, i + prevStrandsFlowers, x, y - i * flowerSpacing, z);
                flowers.add(flower);
                allFlowers.add(flower);
                allPoints.addAll(flower.allPoints);
                addressablePoints.addAll(flower.addressablePoints);
            }
        }

        public Strand(Run run, int strandId, float xpos, int strandRunIndex, List<Bezier> beziers) {
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
                Point bPos = bezier.getPointAtArcLength(thisCurveT);
                LUButterfly butterfly = new LUButterfly(i, i + prevStrandsButterflies, bPos.x, butterflyYHeight, bPos.y);
                butterflies.add(butterfly);
                allButterflies.add(butterfly);
                allPoints.addAll(butterfly.allPoints);
                addressablePoints.addAll(butterfly.allPoints);
            }
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
        Point start;
        Point c1;
        Point c2;
        Point end;
        float[] arcLengths;
        float totalArcLength;
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

        public Point getPointAtArcLength(float t) {
            return calculateBezierPoint(t);
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
        public List<Bezier> beziers;
        int runIndex;
        float cxOffset = 100f;
        float cyOffset = 30f;

        public enum RunType {
            BUTTERFLY,
            FLOWER
        }
        RunType runType;

        public Run(int runIndex, RunType runType, int numStrands, float x, float y, float z) {
            this.runIndex = runIndex;
            this.runType = runType;
            strands = new ArrayList<Strand>();
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();

            for (int i = 0; i < numStrands; i++) {
                Strand strand = new Strand(this, allStrands.size(), Strand.StrandType.FLOWER, x, y, z, i);
                allPoints.addAll(strand.allPoints);
                flowers.addAll(strand.flowers);
                allStrands.add(strand);
            }
        }

        public Run(int runIndex, float pos, int numStrands) {
            this.runIndex = runIndex;
            this.runType = RunType.BUTTERFLY;
            if (runIndex == 0)
                cxOffset = - cxOffset;
            if (runIndex == 2) {
                cyOffset = cyOffset * 5;
                cxOffset = cxOffset * 2;
            }

            Point bezierStart = new Point(pos, 0f);
            Point bezierEnd = new Point(pos, numStrands * 120f);
            Point bezierC1 = new Point(bezierStart.x + cxOffset, bezierStart.y + cyOffset);
            Point bezierC2 = new Point(bezierEnd.x + cxOffset, bezierEnd.y - cyOffset);
            bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


            Point b2Start = new Point(bezierEnd.x, bezierEnd.y);
            Point b2End = new Point(bezierEnd.x, numStrands * 240f);
            Point b2C1 = new Point(b2Start.x - cxOffset, b2Start.y + cyOffset);
            Point b2C2 = new Point(b2End.x - cxOffset, b2End.y - cyOffset);
            bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);
            strands = new ArrayList<Strand>();
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();
            beziers = new ArrayList<Bezier>();
            beziers.add(bezier1);
            beziers.add(bezier2);
            for (int i = 0; i < numStrands; i++) {
                Strand strand = new Strand(this, allStrands.size(), pos, i, beziers);
                allPoints.addAll(strand.allPoints);
                butterflies.addAll(strand.butterflies);
                allStrands.add(strand);
            }
        }
    }

    static public KaledoscopeModel createModel(int numRuns, int strandsPerRun, int butterfliesPerStrand) {
        List<LXPoint> allPoints = new ArrayList<LXPoint>();

        allRuns = new ArrayList<Run>(numRuns);
        allStrands = new ArrayList<Strand>();
        allButterflies = new ArrayList<LUButterfly>();
        allFlowers = new ArrayList<LUFlower>();
        numStrandsPerRun = strandsPerRun;
        List<LXPoint> butterflyPoints = new ArrayList<LXPoint>();
        List<LXPoint> flowerPoints = new ArrayList<LXPoint>();

        for (int i = 0; i < numRuns; i++) {
            Run run = new Run(i, i * lineSpacingInches, 2);
            allRuns.add(run);
            allPoints.addAll(run.allPoints);
            butterflyPoints.addAll(run.allPoints);
        }

        int flowerRuns = FireflyShow.runsFlowers;
        for (int i = 0; i < flowerRuns; i++) {
            float x = -5f * 12f;
            float runSpacing = 10f * 12f;
            if (i % 2 == 1)
                x += 10 * 12f;
            Run run = new Run(allRuns.size(), Run.RunType.FLOWER, 1, x, 8f * 12f, i * runSpacing + 12f);
            allRuns.add(run);
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
}
