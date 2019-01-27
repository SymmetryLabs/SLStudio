package heronarts.lx;

import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An LXDeviceComponent whose parent is an LXBus.   The concrete subclasses
 * of this class are Patterns and Effects.
 */
public abstract class LXBusComponent extends LXDeviceComponent {
    List<LXVector> vectorList = null;

    protected LXBusComponent(LX lx) {
        super(lx);
    }

    protected LXComponent setParent(LXComponent parent) {
        if (!(parent instanceof LXBus)) {
            throw new IllegalArgumentException(
                    getClass().getSimpleName() + " must have an LXBus (not " +
                            parent.getClass().getSimpleName() + " ) as its parent");
        }
        super.setParent(parent);
        return this;
    }

    public LXBus getBus() {
        return (LXBus) getParent();
    }

    public LXBusComponent setBus(LXBus bus) {
        setParent(bus);
        return this;
    }

    /** This method is invoked whenever getVectorList() or getVectors() changes. */
    public /* abstract */ void onVectorsChanged() { }

    /**
     * Gets the LXVectors for this bus, after all warps on this bus have been applied.
     * Returns an array of nullable LXVectors of the same length as model.points.
     */
    protected LXVector[] getVectorArray() {
        return LXBus.getVectorArray(getBus(), model);
    }

    /**
     * Gets the LXVectors for this bus, after all warps on this bus have been applied.
     * Returns a list of non-null LXVectors (i.e. getVectorArray() without nulls).
     *
     * Use getVectorList() only when you need to know the size of the list, randomly
     * access its elements, or run a parallelStream() over the list.  Otherwise,
     * use getVectors(), which saves memory and iterates over the vectors on demand.
     */
    protected List<LXVector> getVectorList() {
        return LXBus.getVectorList(getBus(), model);
    }

    /**
     * Gets the LXVectors for this bus, after all warps on this bus have been applied.
     * Returns an iterable of non-null LXVectors (i.e. getVectorArray() without nulls).
     */
    protected Iterable<LXVector> getVectors() {
        return LXBus.getVectors(getBus(), model);
    }

    /**
     * Gets the LXVectors for the given points, after all warps on this bus have been
     * applied.  Returns an iterable of non-null LXVectors.
     */
    protected Iterable<LXVector> getVectors(List<LXPoint> points) {
        return LXBus.getVectors(getBus(), model, points);
    }

    /**
     * Gets the LXVectors for the given points, after all warps on this bus have been
     * applied.  Returns an iterable of non-null LXVectors.
     */
    protected Iterable<LXVector> getVectors(LXPoint[] points) {
        return LXBus.getVectors(getBus(), model, points);
    }

    /**
     * Gets the LXVectors whose index is from start inclusive to stop exclusive,
     * after all warps on this bus have been applied.  Returns an iterable of
     * non-null LXVectors.
     */
    protected Iterable<LXVector> getVectors(int start, int stop) {
        return LXBus.getVectors(getBus(), model, start, stop);
    }
}
