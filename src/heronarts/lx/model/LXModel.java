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

    /**
     * An immutable list of all the points in this model
     */
    public final LXPoint[] points;

    private final List<LXPoint> pointList;

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
    public final float cx;

    /**
     * Center of the model in y space
     */
    public final float cy;

    /**
     * Center of the model in z space
     */
    public final float cz;

    /**
     * Average x point
     */
    public final float ax;

    /**
     * Average y point
     */
    public final float ay;

    /**
     * Average z points
     */
    public final float az;

    /**
     * Minimum x value
     */
    public final float xMin;

    /**
     * Maximum x value
     */
    public final float xMax;

    /**
     * Range of x values
     */
    public final float xRange;

    /**
     * Minimum y value
     */
    public final float yMin;

    /**
     * Maximum y value
     */
    public final float yMax;

    /**
     * Range of y values
     */
    public final float yRange;

    /**
     * Minimum z value
     */
    public final float zMin;

    /**
     * Maximum z value
     */
    public final float zMax;

    /**
     * Range of z values
     */
    public final float zRange;

    /**
     * Smallest radius from origin
     */
    public final float rMin;

    /**
     * Greatest radius from origin
     */
    public final float rMax;

    /**
     * Range of radial values
     */
    public final float rRange;

    /**
     * Constructs a null model with no points
     */
    public LXModel() {
        this(new LXFixture[] {});
    }

    /**
     * Constructs a model from a list of points
     *
     * @param points Points
     */
    public LXModel(List<LXPoint> points) {
        this(new BasicFixture(points));
    }

    /**
     * Constructs a model with one fixture
     *
     * @param fixture Fixture
     */
    public LXModel(LXFixture fixture) {
        this(new LXFixture[] { fixture });
    }

    /**
     * Constructs a model with the given fixtures
     *
     * @param fixtures Fixtures
     */
    public LXModel(LXFixture[] fixtures) {
        List<LXFixture> _fixtures = new ArrayList<LXFixture>();
        List<LXPoint> _points = new ArrayList<LXPoint>();
        for (LXFixture fixture : fixtures) {
            _fixtures.add(fixture);
            for (LXPoint point : fixture.getPoints()) {
                _points.add(point);
            }
        }

        this.size = _points.size();
        this.pointList = Collections.unmodifiableList(_points);
        this.points = _points.toArray(new LXPoint[0]);
        this.fixtures = Collections.unmodifiableList(_fixtures);

        float _ax = 0, _ay = 0, _az = 0;
        float _xMin = 0, _xMax = 0, _yMin = 0, _yMax = 0, _zMin = 0, _zMax = 0, _rMin = 0, _rMax = 0;

        boolean firstPoint = true;
        for (LXPoint p : this.points) {
            _ax += p.x;
            _ay += p.y;
            _az += p.z;
            if (firstPoint) {
                _xMin = _xMax = p.x;
                _yMin = _yMax = p.y;
                _zMin = _zMax = p.z;
                _rMin = _rMax = p.r;
            } else {
                if (p.x < _xMin)
                    _xMin = p.x;
                if (p.x > _xMax)
                    _xMax = p.x;
                if (p.y < _yMin)
                    _yMin = p.y;
                if (p.y > _yMax)
                    _yMax = p.y;
                if (p.z < _zMin)
                    _zMin = p.z;
                if (p.z > _zMax)
                    _zMax = p.z;
                if (p.r < _rMin)
                    _rMin = p.r;
                if (p.r > _rMax)
                    _rMax = p.r;
            }
            firstPoint = false;
        }
        this.ax = _ax / Math.max(1, this.points.length);
        this.ay = _ay / Math.max(1, this.points.length);
        this.az = _az / Math.max(1, this.points.length);
        this.xMin = _xMin;
        this.xMax = _xMax;
        this.xRange = _xMax - _xMin;
        this.yMin = _yMin;
        this.yMax = _yMax;
        this.yRange = _yMax - _yMin;
        this.zMin = _zMin;
        this.zMax = _zMax;
        this.zRange = _zMax - _zMin;
        this.rMin = _rMin;
        this.rMax = _rMax;
        this.rRange = _rMax - _rMin;
        this.cx = xMin + xRange / 2.f;
        this.cy = yMin + yRange / 2.f;
        this.cz = zMin + zRange / 2.f;
    }

    public List<LXPoint> getPoints() {
        return this.pointList;
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

    public void computeNormals() {
        for (LXPoint p : this.points) {
            p.computeNormals(this);
        }
    }

}
