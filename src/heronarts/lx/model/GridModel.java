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

/**
 * Model of points in a simple grid.
 */
public class GridModel extends LXModel {
    
    /**
     * Width of the grid
     */
    public final int width;
    
    /**
     * Height of the grid
     */
    public final int height;
    
    /**
     * Spacing on the x-axis
     */
    public final float xSpacing;
    
    /**
     * Spacing on the y-axis
     */
    public final float ySpacing;
    
    /**
     * Constructs a uniformly spaced grid model of the given size with
     * all pixels apart by a unit of 1.
     * 
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public GridModel(int width, int height) {
        this(width, height, 1, 1);
    }
    
    /**
     * Constructs a grid model with specified x and y spacing
     * 
     * @param width
     * @param height
     * @param xSpacing
     * @param ySpacing
     */
    public GridModel(int width, int height, float xSpacing, float ySpacing) {
        super(new Fixture(width, height, xSpacing, ySpacing));
        this.width = width;
        this.height = height;
        this.xSpacing = xSpacing;
        this.ySpacing = ySpacing;
    }
        
    private static class Fixture extends LXAbstractFixture {
        private Fixture(int width, int height, float xSpacing, float ySpacing) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    this.points.add(new LXPoint(x*xSpacing, y*ySpacing));
                }
            }
        }
    }

}
