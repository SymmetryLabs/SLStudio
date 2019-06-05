/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx.model;

import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An LXModel is a representation of a set of points in 3-d space. Each LXPoint
 * corresponds to a single point. Models are comprised of Fixtures. An LXFixture
 * specifies a set of points, the Model object takes some number of these and
 * wraps them up with a few useful additional fields, such as the center
 * position of all points and the min/max/range on each axis.
 */
public class LXModel implements LXFixture {

    public interface Listener {
        public void onModelUpdated(LXModel model);
    }

    /** An immutable list of all the points in this model */
    public final LXPoint[] points;

    /** A copy of all the points, converted to LXVectors */
    public LXVector[] vectors = null;

    private final List<LXPoint> pointList;

    /**
     * An immutable identifier for this model.
     */
    public final String modelId;

    /**
     * An immutable list of all the fixtures in this model
     */
    public final List<LXFixture> fixtures;

    /**
     * Number of points in the model
     */
    public final int size;

    /**
     * Center of the model in x space
     */
    public float cx;

    /**
     * Center of the model in y space
     */
    public float cy;

    /**
     * Center of the model in z space
     */
    public float cz;

    /**
     * Average x point
     */
    public float ax;

    /**
     * Average y point
     */
    public float ay;

    /**
     * Average z points
     */
    public float az;

    /**
     * Minimum x value
     */
    public float xMin;

    /**
     * Maximum x value
     */
    public float xMax;

    /**
     * Range of x values
     */
    public float xRange;

    /**
     * Minimum y value
     */
    public float yMin;

    /**
     * Maximum y value
     */
    public float yMax;

    /**
     * Range of y values
     */
    public float yRange;

    /**
     * Minimum z value
     */
    public float zMin;

    /**
     * Maximum z value
     */
    public float zMax;

    /**
     * Range of z values
     */
    public float zRange;

    /**
     * Smallest radius from origin
     */
    public float rMin;

    /**
     * Greatest radius from origin
     */
    public float rMax;

    /**
     * Range of radial values
     */
    public float rRange;

    private ModelMetrics metrics = new ModelMetrics();
    public ModelMetrics getMetrics() { return metrics; }

    /**
     * Constructs a null model with no points
     */
    public LXModel(String modelId) {
        this(modelId, new LXFixture[] {});
    }

    /**
     * Constructs a model from a list of points
     *
     * @param points Points
     */
    public LXModel(String modelId, List<LXPoint> points) {
        this(modelId, new BasicFixture(points));
    }

    /**
     * Constructs a model with one fixture
     *
     * @param fixture Fixture
     */
    public LXModel(String modelId, LXFixture fixture) {
        this(modelId, new LXFixture[] { fixture });
    }

    /**
     * Constructs a model with the given fixtures
     *
     * @param fixtures Fixtures
     */
    public LXModel(String modelId, LXFixture[] fixtures) {
        List<LXFixture> _fixtures = new ArrayList<LXFixture>();
        List<LXPoint> _points = new ArrayList<LXPoint>();
        for (LXFixture fixture : fixtures) {
            _fixtures.add(fixture);
            _points.addAll(fixture.getPoints());
        }

        this.size = _points.size();
        this.pointList = Collections.unmodifiableList(_points);
        this.points = _points.toArray(new LXPoint[0]);
        this.fixtures = Collections.unmodifiableList(_fixtures);
        this.modelId = modelId;
        average();
    }

    private final List<Listener> listeners = new ArrayList<Listener>();

    public final LXModel addListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Cannot add null modellistener");
        }
        if (this.listeners.contains(listener)) {
            throw new IllegalStateException("Cannot add duplicate listener to model " + listener);
        }
        this.listeners.add(listener);
        return this;
    }

    public final LXModel removeListener(Listener listener) {
        this.listeners.remove(listener);
        return this;
    }

    /**
     * Update the meta-values in this model. Re-normalizes the points relative to
     * this model and recomputes its averages
     *
     * @return this
     */
    public LXModel update() {
        return update(true, false);
    }

    /**
     * Update the averages and mins/maxes of the model.
     *
     * @param normalize If true, normalize the points relative to this model
     * @return this
     */
    public LXModel update(boolean normalize) {
        return update(normalize, false);
    }

    /**
     * Updates the averages and min/maxes of the model
     *
     * @param normalize If true, normalize the points relative to this model
     * @param recurse If true, compute averages for sub-models as well
     * @return
     */
    public LXModel update(boolean normalize, boolean recurse) {
        // Recursively update values of sub-models
        if (recurse) {
            for (LXFixture fixture : this.fixtures) {
                if (fixture instanceof LXModel) {
                    // NOTE: normals are relative to master model,
                    // flip to false for sub-models
                    ((LXModel) fixture).average();
                }
            }
        }
        average();
        if (normalize) {
            normalize();
        }
        bang();
        return this;
    }

    public LXModel bang() {
        // Notify the listeners of this model that it has changed
        for (Listener listener : this.listeners) {
            listener.onModelUpdated(this);
        }
        return this;
    }

    /**
     * Recompute the averages in this model
     *
     * @return
     */
    public LXModel average() {
        metrics.recompute(points);

        this.ax = metrics.getAverageX();
        this.ay = metrics.getAverageY();
        this.az = metrics.getAverageZ();
        this.cx = metrics.getCenterX();
        this.cy = metrics.getCenterY();
        this.cz = metrics.getCenterZ();
        this.xMin = metrics.getXMin();
        this.xMax = metrics.getXMax();
        this.xRange = metrics.getXRange();
        this.yMin = metrics.getYMin();
        this.yMax = metrics.getYMax();
        this.yRange = metrics.getYRange();
        this.zMin = metrics.getZMin();
        this.zMax = metrics.getZMax();
        this.zRange = metrics.getZRange();
        this.rMin = metrics.getRadialMin();
        this.rMax = metrics.getRadialMax();
        this.rRange = metrics.getRadialRange();

        return this;
    }

    /**
     * Sets the normalized values of all the points in this model (xn, yn, zn)
     * relative to this model's absolute bounds.
     */
    public void normalize() {
        for (LXPoint p : this.points) {
            p.normalize(this);
        }
    }

    public List<LXPoint> getPoints() {
        return this.pointList;
    }

    public LXVector[] getVectorArray() {
        if (vectors == null) {
            System.out.println("Copying model points to vectors (" + points.length + ")...");
            vectors = new LXVector[points.length];
            for (int i = 0; i < points.length; i++) {
                vectors[i] = new LXVector(points[i]);
            }
        }
        return vectors;
    }

    private final static class BasicFixture implements LXFixture {
        private final List<LXPoint> points;

        private BasicFixture(List<LXPoint> points) {
            this.points = points;
        }

        public List<LXPoint> getPoints() {
            return this.points;
        }
    }

}
