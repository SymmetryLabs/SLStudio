/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LXModel implements LXFixture {

    /**
     * An immutable list of all the points in this model
     */
    public final List<LXPoint> points;
    
    /**
     * An immutable list of all the fixtures in this model
     */
    public final List<LXFixture> fixtures;
    
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
        List<LXPoint> _points = new ArrayList<LXPoint>();
        List<LXFixture> _fixtures = new ArrayList<LXFixture>();
        for (LXFixture fixture : fixtures) {
            _fixtures.add(fixture);
            for (LXPoint point : fixture.getPoints()) {
                _points.add(point);
            }
        }
        
        this.points = Collections.unmodifiableList(_points);
        this.fixtures = Collections.unmodifiableList(_fixtures);
        
        float ax = 0, ay = 0, az = 0;
        float _xMin = Float.MAX_VALUE;
        float _xMax = Float.MIN_VALUE;
        float _yMin = Float.MAX_VALUE;
        float _yMax = Float.MIN_VALUE;
        float _zMin = Float.MAX_VALUE;
        float _zMax = Float.MIN_VALUE;
        
        for (LXPoint p : this.points) {
            ax += p.x;
            _xMin = Math.min(_xMin, p.x);
            _xMax = Math.max(_xMax, p.x);
            ay += p.y;
            _yMin = Math.min(_yMin, p.x);
            _yMax = Math.max(_yMax, p.x);
            az += p.z;
            _zMin = Math.min(_zMin, p.x);
            _zMax = Math.max(_zMax, p.x);
            
        }
        this.cx = ax / this.points.size();
        this.cy = ay / this.points.size();
        this.cz = az / this.points.size();
        this.xMin = _xMin;
        this.xMax = _xMax;
        this.xRange = _xMax - _xMin;
        this.yMin = _yMin;
        this.yMax = _yMax;
        this.yRange = _yMax - _yMin;
        this.zMin = _zMin;
        this.zMax = _zMax;
        this.zRange = _zMax - _zMin;
        
        LXPoint.counter = 0;
    }
    
    public List<LXPoint> getPoints() {
        return this.points;
    }
    
}
