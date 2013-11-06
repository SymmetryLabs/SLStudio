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

public class Grid implements LXFixture {
    
    /**
     * Points in the grid.
     */
    public final List<LXPoint> points;
    
    /**
     * Width of the grid
     */
    public final int width;
    
    /**
     * Height of the grid
     */
    public final int height;
    
    /**
     * Center position on the x-axis
     */
    public final float cx;
    
    /**
     * Center position on the y-axis
     */
    public final float cy;
    
    /**
     * Constructs a uniformly spaced grid model of the given size with
     * all pixels apart by a unit of 1.
     * 
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cx = (width-1)/2.f;
        this.cy = (height-1)/2.f;
    
        List<LXPoint> _points = new ArrayList<LXPoint>(); 
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                _points.add(new LXPoint(x, y));
            }
        }
        this.points = Collections.unmodifiableList(_points);
    }
    
    public List<LXPoint> getPoints() {
        return this.points;
    }

}
