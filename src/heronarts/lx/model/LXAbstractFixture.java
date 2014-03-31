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
import java.util.List;

public abstract class LXAbstractFixture implements LXFixture {
    protected final List<LXPoint> points = new ArrayList<LXPoint>();

    protected LXAbstractFixture() {
    }

    public List<LXPoint> getPoints() {
        return this.points;
    }
}
