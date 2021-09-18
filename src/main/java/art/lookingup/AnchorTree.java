package art.lookingup;

import art.lookingup.ui.AnchorTreeConfig;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
public class AnchorTree {
    private static final Logger logger = Logger.getLogger(KaledoscopeModel.class.getName());
    int id;
    // Make it easy to address specific flowers via the anchor tree.
    public List<KaledoscopeModel.Run> flowerRuns;
    public AnchorTreeParams p;
    public KaledoscopeModel.Cable[] inCables;
    public KaledoscopeModel.Cable[] outCables;
    public List<LXPoint> inPoints;
    public List<LXPoint> outPoints;

    /**
     * Load the anchor tree config params from AnchorTreeConfig.
     * @param id
     */
    public AnchorTree(int id) {
        p = AnchorTreeConfig.getAnchorTree(id);
        inPoints = new ArrayList<LXPoint>();
        outPoints = new ArrayList<LXPoint>();
        inCables = new KaledoscopeModel.Cable[3];
        outCables = new KaledoscopeModel.Cable[3];
    }

    public float getCableAnchorX(int cableRun) {
        if (cableRun == 0)
            return p.x - p.radius;
        else if (cableRun == 1)
            return p.x;
        else
            return p.x + p.radius;
    }

    public float getCableHeight(int cableRun) {
        if (cableRun == 0)
            return p.c1Y;
        else if (cableRun == 1)
            return p.c2Y;
        else
            return p.c3Y;
    }

    static public class AnchorTreeParams {
        public float x;
        public float z;
        public float radius;
        public boolean isButterflyAnchor;
        public float c1Y;
        public float c2Y;
        public float c3Y;
        public float fw1Top;
        public float fw1Radius;
        public float fw2Top;
        public float fw2Radius;
    }

    /**
     * Returns the number of flower runs.  The second flower run is disabled by setting the fw2_top to 0.
     * @return
     */
    public int numberFlowerRuns() {
        if (p.fw2Top < 1f)
            return 1;
        else
            return 2;
    }
}
