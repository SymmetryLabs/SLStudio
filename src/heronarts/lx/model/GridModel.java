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
     * All the rows in this model
     */
    public final List<LXModel> rows;

    /**
     * All the columns in this model
     */
    public final List<LXModel> columns;

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
     * Constructs a uniformly spaced grid model of the given size with all pixels
     * apart by a unit of 1.
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

        List<LXModel> _rows = new ArrayList<LXModel>();
        for (int y = 0; y < height; ++y) {
            List<LXPoint> row = new ArrayList<LXPoint>();
            for (int x = 0; x < width; ++x) {
                row.add(points.get(x + y * this.width));
            }
            _rows.add(new LXModel(row));
        }
        this.rows = Collections.unmodifiableList(_rows);

        List<LXModel> _columns = new ArrayList<LXModel>();
        for (int x = 0; x < width; ++x) {
            List<LXPoint> column = new ArrayList<LXPoint>();
            for (int y = 0; y < height; ++y) {
                column.add(points.get(x + y * this.width));
            }
            _columns.add(new LXModel(column));
        }
        this.columns = Collections.unmodifiableList(_columns);
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(int width, int height, float xSpacing, float ySpacing) {
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    this.points.add(new LXPoint(x * xSpacing, y * ySpacing));
                }
            }
        }
    }

}
