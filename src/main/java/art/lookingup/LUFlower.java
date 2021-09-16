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
    public int anchorTree;
    public int globalRunNum;
    public int treeRunNum;
    static final float RADIUS = 1.5f;
    static final int NUM_PETALS = 5;
    FlowerConfig flowerConfig;
    public boolean dead = false;

    public LUFlower(KaledoscopeModel.AnchorTree tree, LUFlower.FlowerConfig flowerConfig, int globalRunNum) {
        this.x = tree.x + tree.radius * (float) Math.cos(Math.toRadians(flowerConfig.azimuth));
        float ringSpacing = 24f;
        float topRingHeight = tree.ringTopHeight;
        this.y = topRingHeight - flowerConfig.ringNum * ringSpacing + flowerConfig.verticalDisplacement;
        this.z = tree.z + tree.radius * (float) Math.sin(Math.toRadians(flowerConfig.azimuth));
        this.anchorTree = tree.id;
        this.globalRunNum = globalRunNum;
        this.treeRunNum = flowerConfig.treeRunNum;
        this.flowerConfig = flowerConfig;

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
        if (!dead) allPoints.addAll(addressablePoints);
        allPoints.add(petals[1]);
        allPoints.add(petals[2]);
        allPoints.add(petals[3]);
        allPoints.add(petals[4]);
    }

    static public class FlowerConfig {
        public int ringNum;
        public float azimuth;
        public float verticalDisplacement;
        public int treeNum;
        public int treeRunNum;
        public int indexOnRun;
    }
}
