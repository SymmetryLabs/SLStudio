package art.lookingup;

import art.lookingup.ui.ButterfliesConfig;
import art.lookingup.ui.DeadConfig;
import art.lookingup.ui.FlowersConfig;
import art.lookingup.ui.StrandLengths;
import com.symmetrylabs.shows.firefly.FireflyShow;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
    public static final int BEZIERS_PER_RUN = 4;
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
            logger.log(Level.INFO, "Generating flower strand id " + tree.id + "." + whichRun + " of length: " + configuredNumFlowers);

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
         * NOTE(tracy): This is commented out because we are currently not using Beziers and to reduce the confusion
         * unused code for this build might be commented out in case somebody needs to fix something in an emergency.
         *
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
         *
         *
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
        */

        public Strand(Run run, int globalStrandId, int strandRunIndex, List<Cable> cables, float[] prevCablesLengths) {
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
            int butterflyRunIndex = run.butterflies.size();

            List<int[]> allButterflyConfigs = ButterfliesConfig.getAllButterflyConfigs();
            List<int[]> butterfliesThisTree = ButterfliesConfig.getButterflyConfigs(allButterflyConfigs, cables.get(0).startTree.id);

            // How many inches nearest some obstacle to stop. Could be either computed length of cable or the
            // beginning of the next cable.

            float stopDistance = 8f;
            Random random = new Random(6);
            // NOTE(tracy): GENERATE BUTTERFLY POSITIONS.  Use this commented out for loop.
            //for (int i = 0; !allCablesWithinAnInch(cables, stopDistance) ; i++) {

            for (int i = 0; i < butterfliesThisTree.size(); i++) {
                int currentButterflyRunIndex = butterflyRunIndex + i;
                // TODO(tracy): We should finish within 4 inches of a tree or the next cable segment's start point.
                // Probably not going to fine tune that because this will all need to be done manually.
                /* GENERATE BUTTERFLY POSITIONS */
                /*
                int vertical = ThreadLocalRandom.current().nextInt(0, 7);
                int whichCable = (i % 3); // ButterfliesConfig.getCableForButterflyRunIndex(currentButterflyRunIndex);
                // Try to finish off all cables within 4 inches of the tree.
                if (cableWithinAnInch(cables.get(whichCable), stopDistance)) {
                    whichCable = (whichCable + 1) % 3;
                    if (cableWithinAnInch(cables.get(whichCable), stopDistance)) {
                        // This one should work or else we wouldn't have even entered the loop.
                        whichCable = (whichCable + 1) % 3;
                    }
                }
                // GENERATE BUTTERFLY POSITIONS
                float incrementDistance = 8f; //

                if (i == 0 || i == 2) {
                    // For the outside wires, push the start points out.
                    incrementDistance += cables.get(0).startTree.p.radius/2f;
                }
                Cable thisButterflyCable = cables.get(whichCable);
                */

                /* MANUAL BUTTERFLY POSITIONS */
                /* */
                int whichCable = ButterfliesConfig.getCableForButterflyRunIndex(currentButterflyRunIndex);
                float incrementDistance = ButterfliesConfig.getCableDistancePrevButterfly(currentButterflyRunIndex);
                int vertical = ButterfliesConfig.getVertical(currentButterflyRunIndex);
                Cable thisButterflyCable = cables.get(whichCable);


                float[] newButterflyPosition = thisButterflyCable.newPosition(incrementDistance);
                thisButterflyCable.setPrevButterflyPos(newButterflyPosition);
                thisButterflyCable.prevButterflyTotalCableDistance += incrementDistance;

                LUButterfly butterfly = new LUButterfly(thisButterflyCable, globalStrandId, i, currentButterflyRunIndex,
                    newButterflyPosition[0], newButterflyPosition[1] - vertical, newButterflyPosition[2]);
                butterflies.add(butterfly);
                allButterflies.add(butterfly);
                // For each point on the butterfly, build an index of it's distance along the cable.  This will help with
                // linear rendering algorithms.
                float butterflyRunInches = thisButterflyCable.prevButterflyTotalCableDistance + prevCablesLengths[whichCable];
                // When computing the distance along the cable run, if it is the middle cable, account for the radius of
                // the starting tree so that points along that cable are not ahead of the points along the cables mounted
                // to the sides of threes.
                if (whichCable == 1)
                    butterflyRunInches += thisButterflyCable.startTree.p.radius;
                for (LXPoint butterflyPoint : butterfly.allPoints) {
                    run.ptsRunInches.put(butterflyPoint.index, butterflyRunInches);
                }
                thisButterflyCable.points.addAll(butterfly.allPoints);
                thisButterflyCable.startTree.outPoints.addAll(butterfly.allPoints);
                thisButterflyCable.endTree.inPoints.addAll(butterfly.allPoints);
                thisButterflyCable.butterflies.add(butterfly);
                // GENERATE BUTTERFLY Positions.
                /*
                ButterfliesConfig.setButterflyConfig(butterfly.runIndex, butterfly.cable.startTree.id, butterfly.cable.whichCableRun,
                    (int)incrementDistance, vertical);
                */
            }

            // Some data lines run backwards, so we will add the points to the strand after building the list
            // of butterflies.  For backwards data lines, we will just reverse the list of butterflies.
            if (globalStrandId == 0) { // STRAND 2 BACKWARDS || globalStrandId == 2) {
                Collections.reverse(butterflies);
            }
            for (int bIndex = 0; bIndex < butterflies.size(); bIndex++) {
                allPoints.addAll(butterflies.get(bIndex).allPoints);
                addressablePoints.addAll(butterflies.get(bIndex).addressablePoints);
            }
        }

        public boolean anyCableWithinAnInch(List<Cable> cables) {
            //cables.get(0).prevButterflyTotalCableDistance
            for (Cable cable : cables) {
                if (cable.length() - cable.prevButterflyTotalCableDistance < 4f)
                    return true;
            }
            return false;
        }

        static public boolean allCablesWithinAnInch(List<Cable> cables, float inches) {
            if (cableWithinAnInch(cables.get(0), inches))
                if (cableWithinAnInch(cables.get(1), inches))
                    if (cableWithinAnInch(cables.get(2), inches))
                        return true;
            return false;
        }

        static public boolean cableWithinAnInch(Cable cable, float inches) {
            return (cable.len - cable.prevButterflyTotalCableDistance < inches || cable.prevButterflyWithinNextCable(inches));
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

            float[] currentCableRunLengths = new float[3];

            for (int i = 0; i < cableRun0.size(); i++) {
                List<Cable> cablesThisStrand = new ArrayList<Cable>();
                cablesThisStrand.add(cableRun0.get(i));
                cablesThisStrand.add(cableRun1.get(i));
                cablesThisStrand.add(cableRun2.get(i));
                Strand strand = new Strand(this, allStrands.size(), i, cablesThisStrand, currentCableRunLengths);
                if (strand.strandId == 0) { // STRAND 2 BACKWARDS || strand.strandId == 2) {
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
                    // STRAND 2 BACKWARDS
                    // StrandID 2 was going to be backwards, instead it is effectively added to the end of strand 1
                    // but in a forward direction.  This might change back so we will just do a fixup here instead
                    // of a larger invasive change to divorce the tree/strand relationship.
                    if (strand.strandId == 2) {
                        Strand prevStrand = allStrands.get(1);
                        prevStrand.allPoints.addAll(strand.allPoints);
                        prevStrand.addressablePoints.addAll(strand.addressablePoints);
                        prevStrand.butterflies.addAll(strand.butterflies);
                        strand.allPoints.clear();
                        strand.addressablePoints.clear();
                        strand.butterflies.clear();
                    }
                }
                // This is an approximation.  Just base everything on one cable so we don't have to deal with
                // tree radii.
                currentCableRunLengths[0] += cableRun0.get(i).length();
                currentCableRunLengths[1] += cableRun0.get(i).length();
                currentCableRunLengths[2] += cableRun0.get(i).length();
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
         * If we change any of the butterfly position configs, we should run this to recompute the spatial coordinates
         * of all butterflies.
         */
        public void updateButterflyPositions(LX lx) {
            List<int[]> allButterflyConfigs = ButterfliesConfig.getAllButterflyConfigs();
            float[] prevCableRunLengths = new float[3];
            List<Cable> cableRun0 = allCables.get(0);
            List<Cable> cableRun1 = allCables.get(1);
            List<Cable> cableRun2 = allCables.get(2);
            int butterflyRunIndex = 0;
            //
            for (int strandNum = 0; strandNum < cableRun0.size(); strandNum++) {
                List<Cable> cablesThisStrand = new ArrayList<Cable>();
                cablesThisStrand.add(cableRun0.get(strandNum));
                cablesThisStrand.add(cableRun1.get(strandNum));
                cablesThisStrand.add(cableRun2.get(strandNum));


                List<int[]> butterfliesThisTree = ButterfliesConfig.getButterflyConfigs(allButterflyConfigs, cablesThisStrand.get(0).startTree.id);

                // We have to reset these values because we increment them as we add butterflies.  We basically move along
                // the unit vector of the cable.
                cablesThisStrand.get(0).resetPrevButterflyToStart();
                cablesThisStrand.get(1).resetPrevButterflyToStart();
                cablesThisStrand.get(2).resetPrevButterflyToStart();

                for (int i = 0; i < butterfliesThisTree.size(); i++) {
                    int whichCable = ButterfliesConfig.getCableForButterflyRunIndex(butterflyRunIndex);
                    float incrementDistance = ButterfliesConfig.getCableDistancePrevButterfly(butterflyRunIndex);
                    Cable thisButterflyCable = cablesThisStrand.get(whichCable);
                    float[] newButterflyPosition = thisButterflyCable.newPosition(incrementDistance);
                    thisButterflyCable.setPrevButterflyPos(newButterflyPosition);
                    thisButterflyCable.prevButterflyTotalCableDistance += incrementDistance;

                    LUButterfly butterfly = allButterflies.get(butterflyRunIndex);
                    butterfly.updatePosition3D(newButterflyPosition[0], newButterflyPosition[1], newButterflyPosition[2]);

                    // For each point on the butterfly, build an index of it's distance along the cable.  This will help with
                    // linear rendering algorithms.
                    float butterflyRunInches = thisButterflyCable.prevButterflyTotalCableDistance + prevCableRunLengths[whichCable];
                    if (whichCable == 1)
                        butterflyRunInches += thisButterflyCable.startTree.p.radius;
                    for (LXPoint butterflyPoint : butterfly.allPoints) {
                        allButterflyRuns.get(0).ptsRunInches.put(butterflyPoint.index, butterflyRunInches);
                    }
                    butterflyRunIndex++;
                }

                // This is an approximation.  Just base everything on one cable so we don't have to deal with
                // tree radii.
                prevCableRunLengths[0] += cableRun0.get(strandNum).length();
                prevCableRunLengths[1] += cableRun0.get(strandNum).length();
                prevCableRunLengths[2] += cableRun0.get(strandNum).length();
            }
            // Tell the model to update.
            lx.model.update(true, true);
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
                Cable prevCable = null;
                // TODO(tracy):
                for (int treeNum = 0; treeNum < anchorTrees.size() - 1; treeNum++) {
                    AnchorTree tree = anchorTrees.get(treeNum);
                    // Don't include trees that are not specified as butterfly anchors.
                    if (!tree.p.isButterflyAnchor)
                        continue;
                    float startX = tree.getCableAnchorX(whichCableRun);
                    float startY = tree.getCableHeight(whichCableRun);
                    float startZ = tree.getCableAnchorStartZ(whichCableRun);
                    int nextOffset = 1;
                    AnchorTree nextAnchorTree = anchorTrees.get(treeNum + nextOffset);
                    while (!nextAnchorTree.p.isButterflyAnchor && (treeNum + nextOffset) < anchorTrees.size()) {
                        ++nextOffset;
                        nextAnchorTree = anchorTrees.get(treeNum + nextOffset);
                    }
                    float endX = nextAnchorTree.getCableAnchorX(whichCableRun);
                    float endY = tree.getCableHeight(whichCableRun);
                    float endZ = nextAnchorTree.getCableAnchorEndZ(whichCableRun);
                    // Now we need to compute the angle between the anchor trees and rotate all points around the
                    // Y axis by that amount so that are cable start and end points are more accurate.
                    // Translate the first tree to the origin and then translate the second tree the same.
                    float secondTreeX = nextAnchorTree.p.x;
                    float secondTreeZ = nextAnchorTree.p.z;
                    float treeX = tree.p.x;
                    float treeZ = tree.p.z;
                    secondTreeX = secondTreeX - treeX;
                    secondTreeZ = secondTreeZ - treeZ;
                    // Now take the angle from secondTreeX, secondTreeZ to origin.
                    float radians = (float)Math.atan2(secondTreeZ, secondTreeX);
                    float degrees = (float)Math.toDegrees(radians);
                    startX = startX - tree.p.x;
                    startZ = startZ - tree.p.z;
                    float []rotatedStart = rotateYAxis(90f - degrees, startX, startZ);
                    rotatedStart[0] += tree.p.x;
                    rotatedStart[1] += tree.p.z;
                    endZ = endZ - nextAnchorTree.p.z;
                    endX = endX - nextAnchorTree.p.x;
                    float []rotatedEnd = rotateYAxis(90f - degrees, endX, endZ);
                    rotatedEnd[0] += nextAnchorTree.p.x;
                    rotatedEnd[1] += nextAnchorTree.p.z;
                    Cable cable = new Cable(rotatedStart[0], startY, rotatedStart[1], rotatedEnd[0], endY, rotatedEnd[1], whichCableRun);
                    tree.outCables[whichCableRun] = cable;
                    nextAnchorTree.inCables[whichCableRun] = cable;
                    cable.startTree = tree;
                    cable.endTree = nextAnchorTree;
                    cable.prevCable = prevCable;
                    cableRun.add(cable);
                    prevCable = cable;
                }
                for (int i = 0; i < cableRun.size() - 1; i++)
                    cableRun.get(i).nextCable = cableRun.get(i+1);
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

        public float[] rotateYAxis(float degrees, float x, float z) {
            float xOrig = x;
            x = (float) (z * Math.sin(Math.toRadians(degrees)) + x * Math.cos(Math.toRadians(degrees)));
            z = (float) (-xOrig * Math.sin(Math.toRadians(degrees)) + z * Math.cos(Math.toRadians(degrees)));
            float[] result = new float[2];
            result[0] = x;
            result[1] = z;
            return result;
        }


        public float getTotalCableLength() {
            float total = 0f;
            for (int i = 0; i < allCables.get(0).size(); i++) {
                for (Cable cable : allCables.get(0)) {
                    total += cable.length();
                }
            }
            return total;
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
        public float len;
        Cable nextCable;
        Cable prevCable;

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
            len = length();
        }

        public void resetPrevButterflyToStart() {
            prevButterflyX = startX;
            prevButterflyY = startY;
            prevButterflyZ = startZ;
            prevButterflyTotalCableDistance = 0f;
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

        public boolean prevButterflyWithinNextCable(float inches) {
            if (nextCable == null) return false;
            // NOTE(tracy)
            float[] nextCablePos = nextCable.newPosition(nextCable.startX, nextCable.startY, nextCable.startZ, endTree.p.radius);
            float deltaX = prevButterflyX - nextCablePos[0];
            float deltaY = prevButterflyY - nextCablePos[1];
            float deltaZ = prevButterflyZ - nextCablePos[2];
            float dist = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
            if (dist < inches)
                return true;
            return false;
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

        // This will just pre-load all the butterfly configs.  Typically, they will be loaded/initialized
        // with a default whenever requested during model building, but we need to make sure they are all
        // initialized before we bind it to the UI component later.
        ButterfliesConfig.getAllButterflyConfigs();
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
                Run run = new Run(allRuns.size(), Run.RunType.FLOWER, tree, j);
                allRuns.add(run);
                allFlowerRuns.add(run);
                treeFlowerRuns.add(run);
                allPoints.addAll(run.allPoints);
                flowerPoints.addAll(run.allPoints);
            }
            anchorTrees.get(i).flowerRuns = treeFlowerRuns;
        }
        int totalFlowers = 0;
        for (Run run : allFlowerRuns) {
            totalFlowers += run.flowers.size();
        }
        logger.info("Total flowers: " + totalFlowers);

        DeadConfig.init();
        List<String> deadButterflies = DeadConfig.deadButterflyAddresses();
        markDeadButterflies(deadButterflies);
        List<String> deadFlowers = DeadConfig.deadFlowerAddresses();
        markDeadFlowers(deadFlowers);
        KaledoscopeModel.updateDeadOnStrands();
        /**
         * Uncomment this to overwrite your butterfly configs with the generated positions.  Also need to uncomment
         * code in Strand that generates butterfly configurations generatively versus from reading the config.
         **/

        // GENERATE BUTTERFLIES
        // The model creation will update the config when generating butterflies, we just save them here.
        /* */
        //ButterfliesConfig.saveUpdatedButterflyConfigs();


        return new KaledoscopeModel(allPoints);
    }

    public KaledoscopeModel(List<LXPoint> points) {
        super("kaledoscope", points);
    }

    /**
     * Rebuild strands.  We need to do this when we mark something as dead.
     */
    static public void updateDeadOnStrands() {
        for (Run run : allButterflyRuns) {
            for (Strand strand : run.strands) {
                strand.addressablePoints = new ArrayList<LXPoint>();
                for (LUButterfly butterfly: strand.butterflies) {
                    strand.addressablePoints.addAll(butterfly.addressablePoints);
                }
            }
        }
        for (Run run : allFlowerRuns) {
            for (Strand strand : run.strands) {
                strand.addressablePoints = new ArrayList<LXPoint>();
                for (LUFlower flower : strand.flowers) {
                    strand.addressablePoints.addAll(flower.addressablePoints);
                }
            }
        }
    }

    /**
     * NOTE(tracy): This is out of date with new single run 3 cable model.
     *
     * Re-assign butterflies to strands.  This allows us to modify the strand lengths as appropriate for
     * the anchor trees at runtime without restarting SLStudio.  The entire number of butterflies must
     * be conserved since we can't change the number of LXPoints aka the LXModel without restarting.  This
     * should just change the pixlite output mapping.  The UI for configuring strand lengths of butterflies
     * should automatically adjust adjacent strand lengths in order to preserve this property.
     */
    /*
    static public void reassignButterflyStrandsDeprecated() {
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
    */

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
     * Returns a flower given an address.  The address form is anchorTree.runNum.flowerIndex and zero based.
     * So 1.1.3 is the fourth flower on the second run of the second tree.  If the tree or runNum is -1, return
     * null.  This can be used to delete it from the list of dead flowers.  A Restart will be required though.
     * @param address
     * @return
     */
    static public LUFlower getFlowerByAddress(String address) {
        String[] aParts = address.split("\\.");
        return getFlowerByAddress(Integer.parseInt(aParts[0]), Integer.parseInt(aParts[1]), Integer.parseInt(aParts[2]));
    }

    static public LUFlower getFlowerByAddress(int anchorTree, int runNum, int flowerIndex) {
        if (anchorTree < 0 || anchorTree >= KaledoscopeModel.anchorTrees.size())
            return null;
        if (runNum < 0 || runNum >= KaledoscopeModel.anchorTrees.get(anchorTree).flowerRuns.size())
            return null;
        Strand strand = getFlowerStrandByAddress(anchorTree, runNum);
        if (flowerIndex < strand.flowers.size()) {
            return strand.flowers.get(flowerIndex);
        }
        return null;
    }

    /**
     * Returns a butterfly by address.  Since we only have a single run at this point, the butterfly address is
     * Strand#.ButterflyIndex and zero based.  So 3.18 is the 19th butterfly on the fourth strand.
     * @param address
     * @return
     */
    static public LUButterfly getButterflyByAddress(String address) {
        String[] aParts = address.split("\\.");
        if (aParts.length < 2) return null;
        return getButterflyByAddress(Integer.parseInt(aParts[0]), Integer.parseInt(aParts[1]));
    }

    static public LUButterfly getButterflyByAddress(int strandNum, int butterflyIndex) {
        if (strandNum < 0 || strandNum >= allButterflyRuns.get(0).strands.size())
            return null;
        return allButterflyRuns.get(0).strands.get(strandNum).butterflies.get(butterflyIndex);
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

    static public void markDeadButterflies(List<String> butterflyAddresses) {
        for (String address : butterflyAddresses) {
            LUButterfly butterfly = getButterflyByAddress(address);
            if (butterfly != null) {
                butterfly.markDead();
            }
        }
    }

    static public void markDeadFlowers(List<String> flowerAddresses) {
        for (String address : flowerAddresses) {
            LUFlower flower = getFlowerByAddress(address);
            if (flower != null) {
                flower.markDead();
            }
        }
    }

    static public void updateButterflyPositions(LX lx) {
        allButterflyRuns.get(0).updateButterflyPositions(lx);
    }
}
