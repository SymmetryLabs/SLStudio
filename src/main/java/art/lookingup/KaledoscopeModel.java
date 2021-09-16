package art.lookingup;

import art.lookingup.ui.FlowersConfig;
import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KaledoscopeModel extends SLModel {
    private static final Logger logger = Logger.getLogger(KaledoscopeModel.class.getName());

    public static float butterflySpacingInches = 12f;
    public static List<AnchorTree> anchorTrees;
    public static List<LUButterfly> allButterflies;
    public static List<LUFlower> allFlowers;
    public static List<Run> allRuns;
    public static List<Run> allButterflyRuns;
    public static List<Run> allFlowerRuns;
    public static List<Strand> allStrands;
    public static float butterflyYHeight = 120f;
    public static final int NUM_ANCHOR_TREES = 5;
    public static final int NUM_ANCHOR_TREES_FLOWERS = 4;
    public static final int BEZIERS_PER_RUN = 4;
    public static final int FLOWER_RUNS_PER_TREE = 2;
    public static final int FLOWER_STRANDS_PER_RUN = 1;

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


        /**
         * Create a strand of flowers.  Currently a single run of flowers consists of a single strand so strandRunIndex will
         * always be 0.
         * @param run
         * @param tree
         * @param whichRun
         * @param globalStrandId
         * @param strandType
         * @param strandRunIndex
         */
        public Strand(Run run, AnchorTree tree, int whichRun, int globalStrandId, StrandType strandType, int strandRunIndex) {
            this.strandId = globalStrandId;
            this.run = run;
            this.strandType = strandType;
            flowers = new ArrayList<LUFlower>();
            butterflies = new ArrayList<LUButterfly>();
            allPoints = new ArrayList<LXPoint>();
            addressablePoints  = new ArrayList<LXPoint>();
            this.strandRunIndex = strandRunIndex;

            // TODO(tracy): Allow for flower specific strand lengths versus relying on globalStrandId
            // Flower strand length configs should be like
            // Tree 1.0: 14
            // Tree 1.1: 13
            // Tree 2.0: 12
            // Tree 2.1: 15
            int configuredNumFlowers = FireflyShow.allStrandLengths.get(globalStrandId);
            logger.log(Level.INFO, "Generating flower strand id " + globalStrandId + " of length: " + configuredNumFlowers);

            for (int i = 0; i < configuredNumFlowers; i++) {
                /*
                float flowerSpacing = 12f;
                float flowerMaxHeight = 110f;
                int prevRunFlowers = run.flowers.size();
                // Flowers are wired from top to bottom since the wiring will be high up in the tree.  We need to
                // compute every 12 inches along a helix that started at the top of the run.
                float flowerRunArcDistance = (i + prevRunFlowers) * flowerSpacing;
                float t = tree.helix.getTAtArcLength(flowerRunArcDistance);
                Helix.Point3 pos = tree.helix.calculateHelicalPoint(t);
                float flowerX = pos.x + tree.x;
                float flowerY = flowerMaxHeight - pos.y;
                float flowerZ = pos.z + tree.z;
                // logger.info("FLOWER " + i + " pos: " + flowerX + "," + flowerY + "," + flowerZ + " hdist: " + flowerRunArcDistance + " t: " + t);
                LUFlower flower = new LUFlower(tree.id, i, i + prevRunFlowers, flowerX, flowerY, flowerZ);
                */
                LUFlower.FlowerConfig flowerConfig = FlowersConfig.getFlowerConfig(tree.id, whichRun, i);
                LUFlower flower = new LUFlower(tree, flowerConfig, run.runId);
                flowers.add(flower);
                allFlowers.add(flower);
                allPoints.addAll(flower.allPoints);
                addressablePoints.addAll(flower.addressablePoints);
            }
        }

        /**
         * Create a strand of butterflies.  The positions are determined by the passed in list of bezier curves and also
         * the butterfly's position in the entire run of butterflies.
         *
         * TODO(tracy): Strands are embedded in the model building process but they are just a output mapping feature.
         * The assignments of butterflies to strands should be independent of the model so that we can adjust the
         * strand lengths at runtime without rebuilding the model since we really just need to restart the networking
         * output.
         *
         * @param run
         * @param strandId
         * @param strandRunIndex
         * @param beziers
         */
        public Strand(Run run, int strandId, int strandRunIndex, List<Bezier> beziers) {
            this.strandId = strandId;
            strandType = StrandType.BUTTERFLY;
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();
            addressablePoints = new ArrayList<LXPoint>();

            this.strandRunIndex = strandRunIndex;
            // The number of configured butterflies on this strand.
            // TODO(tracy): We need to configure the number of butterflies on each run.
            // And then we build our strand lengths based on tree positions so that
            // they add up to the total number of butterflies.  We should then allow
            // for the strands to change lengths while constraining the total number
            // of butterflies.  i.e. only allow +/- on a strand such that it steals points
            // from or adds points to the next strand.  That also means we need to be
            // able to perform strand assignment outside of model building in order to
            // adjust strand lengths without restarting the program (just need to restart
            // the network.
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
                Bezier.Point bPos = bezier.calculateBezierPoint(thisCurveT);

                LUButterfly butterfly = new LUButterfly(i, i + prevStrandsButterflies, bPos.x, butterflyYHeight, bPos.y);
                butterflies.add(butterfly);
                allButterflies.add(butterfly);
                allPoints.addAll(butterfly.allPoints);
                addressablePoints.addAll(butterfly.allPoints);
            }
        }

        public void recomputeBeziers(List<Bezier> beziers) {
            for (int i = 0; i < butterflies.size(); i++) {
                Bezier.Point bPos = computeButterflyPosition(beziers, i);
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
        public Bezier.Point computeButterflyPosition(List<Bezier> beziers, int butterflyStrandIndex) {

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
            Bezier.Point bPos = bezier.calculateBezierPoint(thisCurveT);
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
        int runId;
        float cxOffset = 70f;
        float cyOffset = 20f;

        public enum RunType {
            BUTTERFLY,
            FLOWER
        }
        RunType runType;

        /**
         * A Run of flowers.  For each tree, there are two runs of flowers each one strand long.
         */
        public Run(int runId, RunType runType, AnchorTree tree, int whichRun) {
            this.runId = runId;
            this.runType = runType;
            strands = new ArrayList<Strand>();
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();

            // To make numStrands configurable do like this after adding UI
            // int numStrands = FireflyShow.butterflyRunsNumStrands.get(runId);
            for (int i = 0; i < FLOWER_STRANDS_PER_RUN; i++) {
                //Run run, AnchorTree tree, int treeRunNum, int globalStrandId, StrandType strandType, int strandRunIndex) {
                Strand strand = new Strand(this, tree, whichRun, allStrands.size(), Strand.StrandType.FLOWER, i);
                allPoints.addAll(strand.allPoints);
                flowers.addAll(strand.flowers);
                allStrands.add(strand);
                strands.add(strand);
            }
        }

        public Run(int runId, List<AnchorTree> trees) {
            this.runId = runId;
            this.runType = RunType.BUTTERFLY;
            int numStrands = FireflyShow.butterflyRunsNumStrands.get(runId);
            float treeMargin = 2f;

            // Run index == 0 needs to be to the left of tree.
            // Run index == 1 needs to be to the right of tree.
            float startX = trees.get(0).x - (trees.get(0).radius + treeMargin);
            if (runId == 1)
                startX = trees.get(0).x + (trees.get(0).radius + treeMargin);
            Bezier.Point bezierStart = new Bezier.Point(startX, trees.get(0).z);
            float endX = trees.get(1).x - (trees.get(1).radius + treeMargin);
            if (runId == 1)
                endX = trees.get(1).x + (trees.get(1).radius + treeMargin);
            Bezier.Point bezierEnd = new Bezier.Point(endX, trees.get(1).z);
            Bezier.Point bezierC1 = new Bezier.Point(bezierStart.x, bezierStart.y + cyOffset);
            Bezier.Point bezierC2 = new Bezier.Point(bezierEnd.x, bezierEnd.y - cyOffset);
            bezier1 = new Bezier(bezierStart, bezierC1, bezierC2, bezierEnd);


            Bezier.Point b2Start = new Bezier.Point(bezierEnd.x, bezierEnd.y);
            float endX2 = trees.get(2).x - (trees.get(2).radius + treeMargin);
            if (runId == 1)
                endX2 = trees.get(2).x + (trees.get(2).radius + treeMargin);
            Bezier.Point b2End = new Bezier.Point(endX2, trees.get(2).z);
            Bezier.Point b2C1 = new Bezier.Point(b2Start.x, b2Start.y + cyOffset);
            Bezier.Point b2C2 = new Bezier.Point(b2End.x, b2End.y - cyOffset);
            bezier2 = new Bezier(b2Start, b2C1, b2C2, b2End);

            Bezier.Point b3Start = new Bezier.Point(b2End.x, b2End.y);
            float endX3 = trees.get(3).x - (trees.get(3).radius + treeMargin);
            if (runId == 1)
                endX3 = trees.get(3).x + (trees.get(3).radius + treeMargin);
            Bezier.Point b3End = new Bezier.Point(endX3, trees.get(3).z);
            Bezier.Point b3C1 = new Bezier.Point(b3Start.x, b3Start.y + cyOffset);
            Bezier.Point b3C2 = new Bezier.Point(b3End.x, b3End.y - cyOffset);
            bezier3 = new Bezier(b3Start, b3C1, b3C2, b3End);

            Bezier.Point b4Start = new Bezier.Point(b3End.x, b3End.y);
            float endX4 = trees.get(4).x - (trees.get(4).radius + treeMargin);
            if (runId == 1)
                endX4 = trees.get(4).x + (trees.get(4).radius + treeMargin);
            Bezier.Point b4End = new Bezier.Point(endX4, trees.get(4).z);
            Bezier.Point b4C1 = new Bezier.Point(b4Start.x, b4Start.y + cyOffset);
            Bezier.Point b4C2 = new Bezier.Point(b4End.x, b4End.y - cyOffset);
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
        int id;
        public float x;
        public float z;
        public float radius;
        public float ringTopHeight;
        // Make it easy to address specific flowers via the anchor tree.
        public List<Run> flowerRuns;

        public AnchorTree(int id, float x, float z, float radius, float ringTopHeight) {
            this.id = id;
            this.x = x;
            this.z = z;
            this.ringTopHeight = ringTopHeight;
            logger.info("Anchor tree coordinates: " + x + "," + z);
            this.radius = radius;
            flowerRuns = new ArrayList<Run>();
        }
    }

    static public KaledoscopeModel createModel(int numButterflyRuns) {
        List<LXPoint> allPoints = new ArrayList<LXPoint>();

        anchorTrees = new ArrayList<AnchorTree>();
        allRuns = new ArrayList<Run>();
        allButterflyRuns = new ArrayList<Run>(numButterflyRuns);
        allFlowerRuns = new ArrayList<Run>();
        allStrands = new ArrayList<Strand>();
        allButterflies = new ArrayList<LUButterfly>();
        allFlowers = new ArrayList<LUFlower>();
        List<LXPoint> butterflyPoints = new ArrayList<LXPoint>();
        List<LXPoint> flowerPoints = new ArrayList<LXPoint>();


        for (int i = 0; i < NUM_ANCHOR_TREES; i++){
            float x = -5f * 12f;
            if (i % 2 == 0)
                x += 10 * 12f;
            // The 5th tree is just an anchor for a bezier curve and does not contain any flower rings.
            float anchorTreeRingTop = 9f * 12f;
            if (i < 4) {
               anchorTreeRingTop = FireflyShow.anchorTreesRingTops.get(i);
            }
            anchorTrees.add(new AnchorTree(i, FireflyShow.anchorTreesPos.get(i*2),
                FireflyShow.anchorTreesPos.get(i*2 + 1), FireflyShow.anchorTreesRadii.get(i),
                anchorTreeRingTop));
        }

        for (int i = 0; i < numButterflyRuns; i++) {
            Run run = new Run(i, anchorTrees);
            allRuns.add(run);
            allButterflyRuns.add(run);
            allPoints.addAll(run.allPoints);
            butterflyPoints.addAll(run.allPoints);
        }

        //int flowerRuns = FireflyShow.runsFlowers;
        // There are two runs of flowers per tree. Each run is just a single strand.
        for (int i = 0; i < NUM_ANCHOR_TREES_FLOWERS; i++) {
            List<Run> treeFlowerRuns = new ArrayList<Run>();
            for (int j = 0; j < FLOWER_RUNS_PER_TREE; j++) {
                Run run = new Run(allRuns.size(), Run.RunType.FLOWER, anchorTrees.get(i), j);
                allRuns.add(run);
                allFlowerRuns.add(run);
                treeFlowerRuns.add(run);
                allPoints.addAll(run.allPoints);
                flowerPoints.addAll(run.allPoints);
            }
            anchorTrees.get(i).flowerRuns = treeFlowerRuns;
        }

        return new KaledoscopeModel(allPoints);
    }

    public KaledoscopeModel(List<LXPoint> points) {
        super("kaledoscope", points);
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
