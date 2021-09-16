package art.lookingup;

import heronarts.lx.model.LXPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 16 LED Butterfly representation.  There are two 8 LED strips side-by-side.  For reference, we will refer to the view
 * of the butterfly from the belly side.  The first led is the on the top left.  For wiring purposes the LEDs are wired
 * from top left to bottom left to bottom right and then back up towards the top right.  The butterflies are then
 * wired into a "strand" of butterflies per output.  In physical space, there are maybe three or four long "runs" of
 * butterflies.  Each "run" can potentially be made up of several "strands" as might be necessary for FPS requirements.
 * So it may be possible that a single "run" which is suspended by steel wires may have multiple outputs.  In that case,
 * the long range data line for the successive "strands" on a "run" would need to be carried along the steel cable.
 * It would also require mounting a Pixlite Long Range Receiver along the cable somehow or located at intermediate
 * cable support trees/posts.
 *
 * To simplify patterns we want to be able to ask for LEDs in clockwise, counterclockwise (which is the actual wiring
 * order), and by row.  We should also implement some form of distance to each LED function to simplify spatial patterns
 * if we can get that far with the mapping.  It might also help with mapping.
 *
 */
public class LUButterfly {
    private static final Logger logger = Logger.getLogger(KaledoscopeModel.class.getName());

    public static float ledSpacing = 0.3f;
    public static float stripSpacing = 0.6f;
    public List<LXPoint> right;
    public List<LXPoint> left;
    public List<LXPoint> allPoints;
    public List<LXPoint> addressablePoints;
    public List<LXPoint> pointsClockwise;
    public List<LXPoint> pointsCounterClockwise;
    public List<LXPoint> pointsByRow;
    public float x;
    public float y;
    public float z;
    public int strandIndex;
    public int strandId;
    public int runIndex;
    public boolean dead;

    public LUButterfly(int strandId, int strandIndex, int runIndex, float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strandId = strandId;
        this.strandIndex = strandIndex;
        this.runIndex = runIndex;

        buildPoints();
    }

    /**
     * Allow for the butterfly to be reassigned to a new strand.
     *
     * @param strand
     * @param strandIndex
     */
    public void assignStrand(KaledoscopeModel.Strand strand, int strandIndex) {
        this.strandId = strand.strandId;
        this.strandIndex = strandIndex;
    }

    /**
     * Update the position of the butterfly.  We will need update the position of all points.
     * NOTE: The bezier is only in a 2D plane, so pos.y corresponds to our 'z' coordinate.
     * @param pos
     */
    public void updatePosition(Bezier.Point pos) {
        float deltaX = pos.x - x;
        float deltaZ = pos.y - z;
        for (LXPoint point : allPoints) {
            point.x += deltaX;
            point.z += deltaZ;
        }
        x = pos.x;
        z = pos.y;
    }

    public void buildPoints() {
        allPoints = new ArrayList<LXPoint>();
        addressablePoints = new ArrayList<LXPoint>();
        left = new ArrayList<LXPoint>(8);
        right = new ArrayList<LXPoint>(8);
        pointsClockwise = new ArrayList<LXPoint>();
        pointsCounterClockwise = new ArrayList<LXPoint>();
        pointsByRow = new ArrayList<LXPoint>();

        for (int i = 0; i < 8; i++) {
            left.add(new LXPoint(x , y, z + i * ledSpacing));
        }
        for (int i = 0; i < 8; i++) {
            right.add(new LXPoint(x + stripSpacing, y, z + (7 - i) * ledSpacing));
        }
        pointsClockwise.addAll(left);
        pointsClockwise.addAll(right);

        for (int i = left.size() - 1; i >= 0; i--)
            pointsCounterClockwise.add(left.get(i));
        for (int i = right.size() - 1; i >= 0; i--)
            pointsCounterClockwise.add(right.get(i));

        for (int i = 0; i < left.size(); i++) {
            pointsByRow.add(left.get(i));
            pointsByRow.add(right.get(7-i));
        }

        allPoints.addAll(left);
        allPoints.addAll(right);

        // Allow for the butterfly to be marked 'dead'.  This means that it exists in the model but that we
        // have skipped over it in the physical install with just a jumper wire because it physically died.
        if (!dead) {
            addressablePoints.addAll(allPoints);
        }
    }
}
