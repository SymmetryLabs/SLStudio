package art.lookingup;

import art.lookingup.ui.FlowersConfig;
import art.lookingup.ui.StrandLengths;
import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.model.LXPoint;

import java.util.*;
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
    public static final int NUM_ANCHOR_TREES = 6;
    public static final int NUM_ANCHOR_TREES_FLOWERS = 3;
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
        public StrandType strandType;
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

            int configuredNumFlowers = StrandLengths.getNumFlowers(tree.id, whichRun);
            logger.log(Level.INFO, "Generating flower strand id " + globalStrandId + " of length: " + configuredNumFlowers);

            for (int i = 0; i < configuredNumFlowers; i++) {
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
         * @param globalStrandId
         * @param strandRunIndex
         * @param beziers
         */
        public Strand(Run run, int globalStrandId, int strandRunIndex, List<Bezier> beziers) {
            this.strandId = globalStrandId;
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
            int configuredNumButterflies = StrandLengths.getNumButterflies(run.runId, strandRunIndex);
            //FireflyShow.allStrandLengths.get(strandId);

            for (int i = 0; i < configuredNumButterflies; i++) {
                // How many butterflies so far on this run.
                int prevStrandsButterflies = run.butterflies.size();
                int currentButterflyRunIndex= prevStrandsButterflies + i;
                float runStartOffsetInches = 0f * 12f;
                // Compute the feet from the beginning of the run, including any start offset.
                float currentButterflyArcDistance = runStartOffsetInches + currentButterflyRunIndex * butterflySpacingInches;
                int currentBezierIndex = getBezierSegmentIndexByDistance(currentButterflyArcDistance, beziers);
                float prevCurveDistance = previousCurveArcLengths(currentBezierIndex, beziers);
                float butterflyThisCurveDistance = currentButterflyArcDistance - prevCurveDistance;
                Bezier bezier = beziers.get(currentBezierIndex);
                float thisCurveT = bezier.getTAtArcLength(butterflyThisCurveDistance);
                Bezier.Point bPos = bezier.calculateBezierPoint(thisCurveT);

                LUButterfly butterfly = new LUButterfly(globalStrandId, i, i + prevStrandsButterflies, bPos.x, butterflyYHeight, bPos.y);
                butterflies.add(butterfly);
                allButterflies.add(butterfly);
                allPoints.addAll(butterfly.allPoints);
                // For each point on the butterfly, build an index of it's distance along the run.  This will help with
                // linear rendering algorithms.
                for (LXPoint butterflyPoint : butterfly.allPoints) {
                    run.ptsRunInches.put(butterflyPoint.index, currentButterflyArcDistance);
                }
                addressablePoints.addAll(butterfly.addressablePoints);
            }
        }

        public Strand(Run run, int globalStrandId, int strandRunIndex, List<Cable> cables, int whatever) {
            this.strandId = globalStrandId;
            strandType = StrandType.BUTTERFLY;
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();
            addressablePoints = new ArrayList<LXPoint>();

            this.strandRunIndex = strandRunIndex;
            // The number of configured butterflies on this strand.
            //int configuredNumButterflies = StrandLengths.getNumButterflies(run.runId, strandRunIndex);
            //FireflyShow.allStrandLengths.get(strandId);

            // TODO
            // We need to continue until thisButterflyCable.prevButterflyCableDistance >= cable.length on
            // all three cables.
            // How many butterflies so far on this run.
            for (int i = 0; cables.get(0).prevButterflyTotalCableDistance < (cables.get(0).length() - 1f); i++) {
                int prevStrandsButterflies = run.butterflies.size();
                int currentButterflyRunIndex = prevStrandsButterflies + i;
                int whichCable = getCableForButterflyRunIndex(currentButterflyRunIndex);
                Cable thisButterflyCable = cables.get(whichCable);
                float[] newButterflyPosition = thisButterflyCable.newPosition(6f);
                thisButterflyCable.setPrevButterflyPos(newButterflyPosition);
                thisButterflyCable.prevButterflyTotalCableDistance += 6f;

                LUButterfly butterfly = new LUButterfly(globalStrandId, i, i + prevStrandsButterflies,
                    newButterflyPosition[0], newButterflyPosition[1], newButterflyPosition[2]);
                butterflies.add(butterfly);
                allButterflies.add(butterfly);
                // For each point on the butterfly, build an index of it's distance along the cable.  This will help with
                // linear rendering algorithms.
                for (LXPoint butterflyPoint : butterfly.allPoints) {
                    run.ptsRunInches.put(butterflyPoint.index, thisButterflyCable.prevButterflyTotalCableDistance);
                }
                //addressablePoints.addAll(butterfly.addressablePoints);
                //allPoints.addAll(butterfly.allPoints);
                thisButterflyCable.points.addAll(butterfly.allPoints);
                thisButterflyCable.startTree.outPoints.addAll(butterfly.allPoints);
                thisButterflyCable.endTree.inPoints.addAll(butterfly.allPoints);
                thisButterflyCable.butterflies.add(butterfly);
            }

            // Some data lines run backwards, so we will add the points to the strand after building the list
            // of butterflies.  For backwards data lines, we will just reverse the list of butterflies.
            if (globalStrandId == 0 || globalStrandId == 2) {
                Collections.reverse(butterflies);
            }
            for (int bIndex = 0; bIndex < butterflies.size(); bIndex++) {
                allPoints.addAll(butterflies.get(bIndex).allPoints);
                addressablePoints.addAll(butterflies.get(bIndex).addressablePoints);
            }
        }

        /**
         * For a given butterfly number on the run, return the cable it should be on.
         * @param butterflyRunIndex
         * @return
         */
        public int getCableForButterflyRunIndex(int butterflyRunIndex) {
            // TODO load this from a config
            return butterflyRunIndex % 3;
        }

        public float getCableDistancePrevButterfly(int butterflyRunIndex) {
            // TODO load this from a config
            return 12f;
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
        public List<Bezier> beziers;
        public List<List<Cable>> allCables;
        int runId;
        public Map<Integer, Integer> ptsRunIndex;
        public Map<Integer, Float> ptsRunInches;

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
            ptsRunIndex = new HashMap<Integer, Integer>();
            ptsRunInches = new HashMap<Integer, Float>();

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
            int ptRunIndex = 0;
            for (LXPoint pt : allPoints) {
                ptsRunIndex.put(pt.index, ptRunIndex);
                ++ptRunIndex;
            }
        }

        public Run(int runId, List<AnchorTree> trees) {
            this.runId = runId;
            this.runType = RunType.BUTTERFLY;

            allCables = getAnchorTreeCables(trees);
            List<Cable> cableRun0 = allCables.get(0);
            List<Cable> cableRun1 = allCables.get(1);
            List<Cable> cableRun2 = allCables.get(2);

            strands = new ArrayList<Strand>();
            butterflies = new ArrayList<LUButterfly>();
            flowers = new ArrayList<LUFlower>();
            allPoints = new ArrayList<LXPoint>();
            ptsRunIndex = new HashMap<Integer, Integer>();
            ptsRunInches = new HashMap<Integer, Float>();

            for (int i = 0; i < cableRun0.size(); i++) {
                List<Cable> cablesThisStrand = new ArrayList<Cable>();
                cablesThisStrand.add(cableRun0.get(i));
                cablesThisStrand.add(cableRun1.get(i));
                cablesThisStrand.add(cableRun2.get(i));

                Strand strand = new Strand(this, allStrands.size(), i, cablesThisStrand, 0);
                if (strand.strandId == 0 || strand.strandId == 2) {
                    // The first and third strands run backwards.  We already reversed them when building the
                    // strands but we actually want all the butterflies for a run in proper run order so we
                    // copy the list
                    // TODO(tracy): Make strand creation orthogonal to model creation, i.e. independent.
                    List<LUButterfly> reversedButterflies = new ArrayList<LUButterfly>();
                    reversedButterflies.addAll(strand.butterflies);
                    Collections.reverse(reversedButterflies);
                    butterflies.addAll(reversedButterflies);
                    // Only the butterfly fixtures are reversed, the data lines on the butterflies are
                    // not reversed so we add those in the correct order.
                    for (LUButterfly butterfly : reversedButterflies) {
                        allPoints.addAll(butterfly.allPoints);
                    }
                } else {
                    allPoints.addAll(strand.allPoints);
                    butterflies.addAll(strand.butterflies);
                }
                allStrands.add(strand);
                strands.add(strand);
            }
            int ptRunIndex = 0;
            for (LXPoint pt : allPoints) {
                ptsRunIndex.put(pt.index, ptRunIndex);
                ++ptRunIndex;
            }
        }


        /**
         * Returns an array of lists of cable.  The first element of the array is a list of cable segments representing
         * a full length run of cable.  There are 3 runs of cables.
         * @param anchorTrees
         * @return
         */
        public List<List<Cable>> getAnchorTreeCables(List<AnchorTree> anchorTrees) {
            // All cables for the entire run.
            List<List<Cable>> allCableRuns = new ArrayList<List<Cable>>();
            for (int whichCableRun = 0; whichCableRun < 3; whichCableRun++) {
                List<Cable> cableRun = new ArrayList<Cable>();

                // TODO(tracy):
                for (int treeNum = 0; treeNum < anchorTrees.size() - 1; treeNum++) {
                    AnchorTree tree = anchorTrees.get(treeNum);
                    // Don't include trees that are not specified as butterfly anchors.
                    if (!tree.p.isButterflyAnchor)
                        continue;
                    float startX = tree.getCableAnchorX(whichCableRun);
                    float startY = tree.getCableHeight(whichCableRun);
                    float startZ = tree.p.z;
                    int nextOffset = 1;
                    AnchorTree nextAnchorTree = anchorTrees.get(treeNum + nextOffset);
                    while (!nextAnchorTree.p.isButterflyAnchor && (treeNum + nextOffset) < anchorTrees.size()) {
                        ++nextOffset;
                        nextAnchorTree = anchorTrees.get(treeNum + nextOffset);
                    }
                    float endX = nextAnchorTree.getCableAnchorX(whichCableRun);
                    float endY = tree.getCableHeight(whichCableRun);
                    float endZ = nextAnchorTree.p.z;
                    Cable cable = new Cable(startX, startY, startZ, endX, endY, endZ, whichCableRun);
                    tree.outCables[whichCableRun] = cable;
                    nextAnchorTree.inCables[whichCableRun] = cable;
                    cable.startTree = tree;
                    cable.endTree = nextAnchorTree;
                    cableRun.add(cable);
                }
                allCableRuns.add(cableRun);
            }

            return allCableRuns;
        }

        public float getButterfliesRunDistance() {
            return butterflies.size() * 12f;
        }

        /**
         * For a given LXPoint, return it's distance in inches along the run.  This groups all LEDs of a single
         * butterfly at the same distance.  It is used for some linear effects.
         * @param point
         * @return
         */
        public float getRunDistance(LXPoint point) {
            return ptsRunInches.get(point.index);
        }
    }

    /**
     * Aircraft cable supports.  There are three support cables.  Two top cables
     * and one lower cable.  Butterflies are hung from cables.
     */
    static public class Cable {
        // Measured dimensions
        // Determined by anchor tree.
        public float startX;
        public float startZ;
        public float startY;
        public float endX;
        public float endZ;
        public float endY;
        public float[] unitVector;
        public int whichCableRun;  // 0 left, 1 right, 2 lower centered.
        public AnchorTree startTree;
        public AnchorTree endTree;
        public List<LXPoint> points;
        public List<LUButterfly> butterflies;

        // Used during the construction of the butterfly runs.  Track the position of the last butterfly on this
        // cable so that for the next butterfly we can use the cable-distance between the two to compute the new
        // butterfly position.
        public float prevButterflyX;
        public float prevButterflyY;
        public float prevButterflyZ;
        public float prevButterflyTotalCableDistance;

        public Cable(float startX, float startY, float startZ, float endX, float endY, float endZ, int whichCableRun) {
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
            this.endX = endX;
            this.endY = endY;
            this.endZ = endZ;
            computeUnitVector();
            prevButterflyX = startX;
            prevButterflyY = startY;
            prevButterflyZ = startZ;
            this.whichCableRun = whichCableRun;
            points = new ArrayList<LXPoint>();
            butterflies = new ArrayList<LUButterfly>();
        }
        public float length() {
            float diffX = endX - startX;
            float diffY = endY - startY;
            float diffZ = endZ - startZ;
            return (float)Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        }

        // If we want to measure distances between butterflies on a single wire we need
        // the unit vector.  The position of the new butterfly is the position of previous
        // butterfly + distance * unitVector
        public void computeUnitVector() {
            float x = endX - startX;
            float y = endY - startY;
            float z = endZ - startZ;
            unitVector = new float[3];
            unitVector[0] = x / length();
            unitVector[1] = y / length();
            unitVector[2] = z / length();
        }

        public void setPrevButterflyPos(float[] pos) {
            prevButterflyX = pos[0];
            prevButterflyY = pos[1];
            prevButterflyZ = pos[2];
        }

        public float[] newPosition(float distance) {
            return newPosition(prevButterflyX, prevButterflyY, prevButterflyZ, distance);
        }

        public float[] newPosition(float prevX, float prevY, float prevZ, float distance) {
            float[] newPos = new float[3];
            newPos[0] = prevX + distance * unitVector[0];
            newPos[1] = prevY + distance * unitVector[1];
            newPos[2] = prevZ + distance * unitVector[2];
            return newPos;
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
            anchorTrees.add(new AnchorTree(i));
        }

        for (int i = 0; i < numButterflyRuns; i++) {
            Run run;
            if (FireflyShow.runsButterflies == 1) {
                run = new Run(i, anchorTrees);
            } else {
                logger.severe("Only 1 run of butterflies currently supported!");
                run = null;
            }
            allRuns.add(run);
            allButterflyRuns.add(run);
            allPoints.addAll(run.allPoints);
            butterflyPoints.addAll(run.allPoints);
        }
        logger.info("Total butterflies: " + allButterflyRuns.get(0).butterflies.size());

        for (int i = 0; i < NUM_ANCHOR_TREES; i++) {
            List<Run> treeFlowerRuns = new ArrayList<Run>();
            AnchorTree tree = anchorTrees.get(i);
            for (int j = 0; j < tree.numberFlowerRuns(); j++) {
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
     * Re-assign butterflies to strands.  This allows us to modify the strand lengths as appropriate for
     * the anchor trees at runtime without restarting SLStudio.  The entire number of butterflies must
     * be conserved since we can't change the number of LXPoints aka the LXModel without restarting.  This
     * should just change the pixlite output mapping.  The UI for configuring strand lengths of butterflies
     * should automatically adjust adjacent strand lengths in order to preserve this property.
     */
    static public void reassignButterflyStrands() {
        int totalButterfliesAssigned = 0;
        for (Run run : allButterflyRuns) {
            // Get all the strand lengths assigned to this run.
            int currentButterflyRunIndex = 0;
            for (int runStrandNum = 0; runStrandNum < 4; runStrandNum++) {
                int sLength = StrandLengths.getNumButterflies(run.runId, runStrandNum);
                if (sLength == 0)
                    continue;
                Strand strand = getButterflyStrandByAddress(run.runId, runStrandNum);
                // Reset the list of butterflies in the strand.  This list is used by KaledoscopeOutput to do the
                // Pixlite output mapping. Specifically strand.addressablePoints.  The difference between allPoints
                // and addressablePoints is that flowers have only 2 addressable LEDs but 6 total leds.  Also,
                // in order to be able to use a physical jumper to bypass dead butterfly or flower fixtures, we
                // need to keep them in the model, but not send ArtNet data for them.  When one is marked dead,
                // none of it's points will be added to addressablePoints.
                if (strand == null) continue;
                strand.butterflies = new ArrayList<LUButterfly>();
                strand.allPoints = new ArrayList<LXPoint>();
                strand.addressablePoints = new ArrayList<LXPoint>();
                for (int butterflyStrandNum = 0; butterflyStrandNum < sLength; butterflyStrandNum++) {
                    LUButterfly butterfly = run.butterflies.get(currentButterflyRunIndex + butterflyStrandNum);
                    butterfly.assignStrand(run.strands.get(runStrandNum), butterflyStrandNum);
                    strand.butterflies.add(butterfly);
                    strand.allPoints.addAll(butterfly.allPoints);
                    strand.addressablePoints.addAll(butterfly.addressablePoints);
                }
                currentButterflyRunIndex += sLength;
            }
            logger.info("Assigned " + currentButterflyRunIndex + " butterflies to strands for run " + run.runId);
            totalButterfliesAssigned += currentButterflyRunIndex;
        }
        logger.info("Assigned " + totalButterfliesAssigned + " total butterflies to strands for all runs");
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

    /**
     * To simplify the output mapping we want to be able to specify the flower strands like 0.1 for the second
     * run on the first tree.
     *
     * @param anchorTree The tree that the run is on, starts at 0.
     * @param runNum Which run on the tree.
     * @return
     */
    static public Strand getFlowerStrandByAddress(int anchorTree, int runNum) {
        if (anchorTree >= anchorTrees.size())
            return null;
        if (runNum >= anchorTrees.get(anchorTree).flowerRuns.size())
            return null;
        return anchorTrees.get(anchorTree).flowerRuns.get(runNum).strands.get(0);
    }

    /**
     * To simply the output mapping we want to be able to specify the butterfly strands like 0.3 for the fourth
     * strand on the first run of butterflies.
     *
     * @param runNum Which run of butterflies, starts at 0.
     * @param runStrandNum Which strand on the run, starts at 0.
     * @return The appropriate strand or null if the address doesn't make sense for this topology.
     */
    static public Strand getButterflyStrandByAddress(int runNum, int runStrandNum) {
        if (runNum >= allButterflyRuns.size())
            return null;
        Run run = allButterflyRuns.get(runNum);
        if (runStrandNum >= run.strands.size())
            return null;
        return allButterflyRuns.get(runNum).strands.get(runStrandNum);
    }
}
