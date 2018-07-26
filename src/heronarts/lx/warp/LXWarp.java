package heronarts.lx.warp;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXComponent;
import heronarts.lx.LXModelComponent;
import heronarts.lx.LXUtils;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

/**
 * A "warp" is an operation that transforms the coordinates of points in the model.
 * Each warp has an input array of LXVectors and an output array of LXVectors.
 * A warp is free to set output elements to null, which excludes points from
 * rendering; it must also accommodate the possibility that input elements are null.
 * It should behave as a read-only observer of its input array, and is the sole
 * owner and writer of its output array.
 *
 * From the warp's perspective, the input and output are always arrays as big as
 * model.points, where the index of each vector in the array matches the vector's
 * own index field.  However, because it's more convenient for patterns and effects
 * to not have to do null checks, patterns and effects will access these vectors by
 * calling getVectors() to get an Iterable over just the non-null LXVectors, or
 * getVectorList() to get a List of just the non-null LXVectors (see LXBus).
 */
public abstract class LXWarp extends LXModelComponent implements LXComponent.Renamable, LXOscComponent, LXUtils.IndexedElement {
    public final BooleanParameter enabled = new BooleanParameter("Enabled", false)
            .setDescription("Whether the warp is enabled");

    private int index = -1;

    protected LXWarp inputSource = null;
    protected LXVector[] inputVectors = null;  // externally provided, treated as read-only
    protected LXVector[] outputVectors = null;  // solely owned and written by this LXWarp
    protected boolean inputVectorsChanged = false;
    protected boolean parameterChangeDetected = false;

    protected LXWarp(LX lx) {
        super(lx);
        label.setDescription("The name of this warp");
        label.setValue(getClass().getSimpleName().replaceAll("Warp$", ""));
        addParameter("enabled", enabled);

        enabled.addListener(parameter -> {
            if (enabled.isOn()) {
                onEnable();
            } else {
                onDisable();
            }
        });
    }

    public final boolean isEnabled() {
        return this.enabled.isOn();
    }

    @Override
    public void onParameterChanged(LXParameter parameter) {
        super.onParameterChanged(parameter);
        parameterChangeDetected = true;
    }

    /** A useful way for LXWarp subclasses to check if they need to recompute the warp. */
    protected boolean getAndClearParameterChangeDetectedFlag() {
        boolean result = parameterChangeDetected;
        parameterChangeDetected = false;
        return result;
    }

    public final void setIndex(int index) {
        this.index = index;
    }

    public final int getIndex() {
        return index;
    }

    public LXBus getBus() {
        return (LXBus) getParent();
    }

    public void setBus(LXBus bus) {
        setParent(bus);
    }

    protected /* abstract */ void onEnable() { }

    protected /* abstract */ void onDisable() { }

    public void dispose() {
        onDisable();
        super.dispose();
    }

    /**
     * Sets the vector array to be used as input, and keeps track of which warp
     * produced this vector array as output.  The "changed" flag should indicate
     * whether the preceding warp indicated that it changed its output.  This
     * LXWarp object will treat the vector array as read-only, owned by the caller.
     */
    public void setInputVectors(LXWarp source, LXVector[] vectors, boolean changed) {
        if (vectors.length != model.size) {
            throw new IllegalArgumentException(
                    "LXWarp vector array must have the same length as model.points");
        }
        if (source != inputSource || vectors != inputVectors || changed) {
            if (vectors != inputVectors) {
                inputVectors = vectors;
                outputVectors = new LXVector[inputVectors.length];
                for (int i = 0; i < inputVectors.length; i++) {
                    outputVectors[i] = new LXVector(inputVectors[i]);
                }
            }
            inputSource = source;
            inputVectorsChanged = true;
        }
    }

    /**
     * Returns the output of this warp.  This vector array is owned by the LXWarp
     * object, and callers should treat this array as read-only.
     */
    public LXVector[] getOutputVectors() {
        return outputVectors;
    }

    /**
     * Performs the warp, operating on the input vectors that were passed in with
     * setInputVectors(), and producing output retrievable by getOutputVectors().
     */
    public final boolean applyWarp(double deltaMs) {
        boolean outputVectorsChanged = false;
        if (isEnabled()) {
            outputVectorsChanged = run(deltaMs, inputVectorsChanged);
            inputVectorsChanged = false;
        }
        return outputVectorsChanged;
    }

    /**
     * Applies the warp to the coordinates in inputVectors and updates outputVectors
     * (both of which have the same length as model.points).  The inputVectorsChanged
     * flag indicates whether inputVectors has changed since the last call to run();
     * a typical implementation would recompute outputVectors when inputVectorsChanged
     * is true OR getAndClearParameterChangeDetectedFlag returns true.  This method
     * should treat inputVectors as read-only; it is also the sole writer to
     * outputVectors, and it should return a flag indicating whether it changed anything.
     */
    protected abstract boolean run(double deltaMs, boolean inputVectorsChanged);

    public String getOscAddress() {
        LXBus bus = getBus();
        if (bus != null) {
            return bus.getOscAddress() + "/warp/" + (index + 1);
        }
        return null;
    }
}
