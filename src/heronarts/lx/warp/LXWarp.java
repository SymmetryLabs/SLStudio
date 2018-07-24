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

public abstract class LXWarp extends LXModelComponent implements LXComponent.Renamable, LXOscComponent, LXUtils.IndexedElement {
    public final BooleanParameter enabled = new BooleanParameter("Enabled", false)
            .setDescription("Whether the warp is enabled");

    private int index = -1;

    protected LXWarp inputSource = null;
    protected List<LXVector> inputVectors = null;  // externally provided, treated as read-only
    protected List<LXVector> outputVectors = new ArrayList<>();  // solely owned and written by this LXWarp
    protected boolean inputVectorsChanged = false;
    protected boolean outputVectorsChanged = false;
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

    @Override public void onParameterChanged(LXParameter parameter) {
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
     * Sets the vector list to be used as input, and keeps track of which warp
     * produced this vector list as output.  The "changed" flag should indicate
     * whether the preceding warp indicated that it changed its output.  This
     * LXWarp object will treat the vector list as read-only, owned by the caller.
     */
    public void setInputVectors(LXWarp source, List<LXVector> vectors, boolean changed) {
        if (source != inputSource || vectors != inputVectors || changed) {
            if (vectors != inputVectors) {
                inputVectors = vectors;
            }
            inputSource = source;
            inputVectorsChanged = true;
        }
    }

    /**
     * Returns the output of this warp.  This vector list is owned by the LXWarp
     * object, and callers should treat this list as read-only.
     */
    public List<LXVector> getOutputVectors() {
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
     * Applies the warp to the coordinates in inputVectors and updates or rewrites
     * the outputVectors list.  The inputVectorsChanged flag indicates whether
     * inputVectors has changed since the last call to run(); a typical implementation
     * would recompute outputVectors when inputVectorsChanged is true OR any of the
     * warp's parameters has changed.  This method should treat inputVectors as
     * read-only; it is also the sole writer to outputVectors, and it should return
     * a flag indicating whether it made any changes to outputVectors.
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
