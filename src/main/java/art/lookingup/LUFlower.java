package art.lookingup;

import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a flower fixture.  A flower has 1 central LED and 5 petal LEDs but the petals
 * all share an address so there are only 2 addressable LEDs.  We will create LXPoints for
 * all 6 LEDs, but only output the center LED and one petal value.
 */
public class LUFlower {

    public LXPoint center;
    public LXPoint[] petals;
    public List<LXPoint> addressablePoints;
    public List<LXPoint> allPoints;
    public float x;
    public float y;
    public float z;
    public int strandIndex;
    public int runIndex;
    static final float RADIUS = 1.5f;
    static final int NUM_PETALS = 5;


    public LUFlower(int strandIndex, int runIndex, float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.strandIndex = strandIndex;
        this.runIndex = runIndex;

        buildPoints();
    }

    protected void buildPoints() {
        center = new LXPoint(x, y, z);
        petals = new LXPoint[NUM_PETALS];
        float petalDegrees = 360 / NUM_PETALS;

        for (int i = 0; i < NUM_PETALS; i++) {
            float px = RADIUS * (float) Math.cos(Math.toRadians(i * petalDegrees));
            float py = RADIUS * (float) Math.sin(Math.toRadians(i * petalDegrees));
            petals[i] = new LXPoint(x + px, y + py, z);
        }
        addressablePoints = new ArrayList<LXPoint>();
        addressablePoints.add(center);
        addressablePoints.add(petals[0]);
        allPoints = new ArrayList<LXPoint>();
        allPoints.addAll(addressablePoints);
        allPoints.add(petals[1]);
        allPoints.add(petals[2]);
        allPoints.add(petals[3]);
        allPoints.add(petals[4]);
    }
}
