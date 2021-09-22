package art.lookingup;

import java.util.ArrayList;
import java.util.List;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;

/**
 * Represents a flower fixture.  A flower has 1 central LED and 5 petal LEDs but the petals
 * all share an address so there are only 2 addressable LEDs.  We will create LXPoints for
 * all 6 LEDs, but only output the center LED and one petal value.
 */
public class LUFlower implements LXFixture {

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
    public FlowerConfig flowerConfig;
    public boolean dead = false;

    public LUFlower(AnchorTree tree, LUFlower.FlowerConfig flowerConfig, int globalRunNum) {
        this.x = tree.p.x + tree.p.radius * (float) Math.cos(Math.toRadians(flowerConfig.azimuth));
        this.y = tree.p.fw1Top - flowerConfig.verticalDisplacement;
        if (flowerConfig.treeRunNum == 1) {
            this.y = tree.p.fw2Top - flowerConfig.verticalDisplacement;
        }
        this.z = tree.p.z + tree.p.fw1Radius * (float) Math.sin(Math.toRadians(flowerConfig.azimuth));
        this.anchorTree = tree.id;
        this.globalRunNum = globalRunNum;
        this.treeRunNum = flowerConfig.treeRunNum;
        this.flowerConfig = flowerConfig;

        buildPoints();
    }

    public List<LXPoint> getPoints() {
        return allPoints;
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
        if (!dead) {
            addressablePoints.add(center);
            addressablePoints.add(petals[0]);
        }
        allPoints = new ArrayList<LXPoint>();
        if (!dead) allPoints.addAll(addressablePoints);
        allPoints.add(petals[1]);
        allPoints.add(petals[2]);
        allPoints.add(petals[3]);
        allPoints.add(petals[4]);
    }

    public void markDead() {
        dead = true;
        addressablePoints = new ArrayList<LXPoint>();
    }


    /**
     * Convenience function to set the color of all points in a butterfly.
     * @param color
     */
    public void setColor(int[] buffer, int color) {
        for (LXPoint p : allPoints) {
            buffer[p.index] = color;
        }
    }


    /**
     * Update flower position with a new FlowerConfig.
     */
    public void updatePosition(FlowerConfig fc) {
        AnchorTree tree = KaledoscopeModel.anchorTrees.get(anchorTree);
        float x = tree.p.x + tree.p.radius * (float) Math.cos(Math.toRadians(fc.azimuth));
        float y = tree.p.fw1Top - fc.verticalDisplacement;
        if (fc.treeRunNum == 1) {
            y = tree.p.fw2Top - fc.verticalDisplacement;
        }
        float z = tree.p.z + tree.p.fw1Radius * (float) Math.sin(Math.toRadians(fc.azimuth));
        updatePosition3D(x, y, z);
        flowerConfig = fc;
    }

    /**
     * The FlowerPos pattern can update flower positions.
     * @param x
     * @param y
     * @param z
     */
    public void updatePosition3D(float x, float y, float z) {
        float deltaX = x - this.x;
        float deltaY = y - this.y;
        float deltaZ = z - this.z;
        this.x = x;
        this.y = y;
        this.z = z;
        for (LXPoint point : allPoints) {
            point.x += deltaX;
            point.z += deltaZ;
            point.y += deltaY;
        }
    }


    static public class FlowerConfig {
        public float azimuth;
        public float verticalDisplacement;
        public int treeNum;
        public int treeRunNum;
        public int indexOnRun;
    }
}
