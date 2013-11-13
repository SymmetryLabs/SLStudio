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

package heronarts.lx.transform;

import heronarts.lx.model.LXPoint;

import processing.core.PVector;

/**
 * A mutable version of an LXPoint, which has had a transformation applied
 * to it, and may have other transformations applied to it. 
 */
public class LXVector extends PVector {

    private static final long serialVersionUID = 1L;
    
    /**
     * Index of the LXPoint this corresponds to
     */
    public final int index;
    
    /**
     * Construct a mutable vector based on an LXPoint
     * 
     * @param point Point with index reference
     */
    public LXVector(LXPoint point) {
        super(point.x, point.y, point.z);
        this.index = point.index;
    }
}
