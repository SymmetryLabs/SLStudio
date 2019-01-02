package heronarts.lx.model;

import java.util.List;
import java.util.ArrayList;

import heronarts.lx.transform.LXVector;

public class ModelMetrics {
    /**
     * Number of points in the model
     */
    private int size;
    public int getSize() { return size; }

    /**
     * Center of the model in x space
     */
    private float cx;
    public float getCenterX() { return cx; }

    /**
     * Center of the model in y space
     */
    private float cy;
    public float getCenterY() { return cy; }

    /**
     * Center of the model in z space
     */
    private float cz;
    public float getCenterZ() { return cz; }

    /**
     * Average x point
     */
    private float ax;
    public float getAverageX() { return ax; }

    /**
     * Average y point
     */
    private float ay;
    public float getAverageY() { return ay; }

    /**
     * Average z points
     */
    private float az;
    public float getAverageZ() { return az; }

    /**
     * Minimum x value
     */
    private float xMin;
    public float getXMin() { return xMin; }

    /**
     * Maximum x value
     */
    private float xMax;
    public float getXMax() { return xMax; }

    /**
     * Range of x values
     */
    private float xRange;
    public float getXRange() { return xRange; }

    /**
     * Minimum y value
     */
    private float yMin;
    public float getYMin() { return yMin; }

    /**
     * Maximum y value
     */
    private float yMax;
    public float getYMax() { return yMax; }

    /**
     * Range of y values
     */
    private float yRange;
    public float getYRange() { return yRange; }

    /**
     * Minimum z value
     */
    private float zMin;
    public float getZMin() { return zMin; }

    /**
     * Maximum z value
     */
    private float zMax;
    public float getZMax() { return zMax; }

    /**
     * Range of z values
     */
    private float zRange;
    public float getZRange() { return zRange; }

    /**
     * Smallest radius from origin
     */
    private float rMin;
    public float getRadialMin() { return rMin; }

    /**
     * Greatest radius from origin
     */
    private float rMax;
    public float getRadialMax() { return rMax; }

    /**
     * Range of radial values
     */
    private float rRange;
    public float getRadialRange() { return rRange; }

    public ModelMetrics() {
    }

    public ModelMetrics(LXPoint[] points) {
        recompute(points);
    }
    public ModelMetrics(List<LXVector> vectors) {
        recompute(vectors);
    }

    public ModelMetrics recompute(LXPoint[] points) {
        List<LXVector> vectors = new ArrayList<>();
        for (LXPoint p : points) {
            vectors.add(new LXVector(p));
        }
        return recompute(vectors);
    }

    public ModelMetrics recompute(List<LXVector> vectors) {
        return recompute(vectors.toArray(new LXVector[0]));
    }

    public ModelMetrics recompute(LXVector[] vectors) {
        float ax = 0, ay = 0, az = 0;
        float xMin = 0, xMax = 0, yMin = 0, yMax = 0, zMin = 0, zMax = 0, rMin = 0, rMax = 0;
        int size = 0;

        boolean firstPoint = true;
        for (LXVector vec : vectors) {
            if (vec == null) {
                continue;
            }

            ++size;

            ax += vec.x;
            ay += vec.y;
            az += vec.z;

            float r = (float)Math.sqrt(vec.x * vec.x + vec.y * vec.y + vec.z * vec.z);

            if (firstPoint) {
                xMin = xMax = vec.x;
                yMin = yMax = vec.y;
                zMin = zMax = vec.z;
                rMin = rMax = r;
            } else {
                if (vec.x < xMin)
                    xMin = vec.x;
                if (vec.x > xMax)
                    xMax = vec.x;
                if (vec.y < yMin)
                    yMin = vec.y;
                if (vec.y > yMax)
                    yMax = vec.y;
                if (vec.z < zMin)
                    zMin = vec.z;
                if (vec.z > zMax)
                    zMax = vec.z;
                if (r < rMin)
                    rMin = r;
                if (r > rMax)
                    rMax = r;
            }
            firstPoint = false;
        }

        this.size = size;
        this.ax = ax / Math.max(1, size);
        this.ay = ay / Math.max(1, size);
        this.az = az / Math.max(1, size);
        this.xMin = xMin;
        this.xMax = xMax;
        this.xRange = xMax - xMin;
        this.yMin = yMin;
        this.yMax = yMax;
        this.yRange = yMax - yMin;
        this.zMin = zMin;
        this.zMax = zMax;
        this.zRange = zMax - zMin;
        this.rMin = rMin;
        this.rMax = rMax;
        this.rRange = rMax - rMin;
        this.cx = xMin + xRange / 2.f;
        this.cy = yMin + yRange / 2.f;
        this.cz = zMin + zRange / 2.f;

        return this;
    }
}
