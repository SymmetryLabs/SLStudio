package heronarts.lx.warp;

import heronarts.lx.LX;
import heronarts.lx.LXBus;
import heronarts.lx.LXComponent;
import heronarts.lx.LXModelComponent;
import heronarts.lx.LXUtils;
import heronarts.lx.osc.LXOscComponent;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXVector;

public abstract class LXWarp extends LXModelComponent implements LXComponent.Renamable, LXOscComponent, LXUtils.IndexedElement {
    public final BooleanParameter enabled = new BooleanParameter("Enabled", false)
            .setDescription("Whether the warp is enabled");

    private int index = -1;

    protected LXWarp inputSource = null;
    protected LXVector[] inputVectors = null;  // externally provided, treated as read-only
    protected LXVector[] outputVectors = null;  // solely owned and written by this LXWarp
    protected boolean inputVectorsChanged = false;
    protected boolean outputVectorsChanged = false;

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

    /**
     * Sets the vector array to be used as input.  This array is assumed to be owned
     * by the caller, and this LXWarp object will treat the array as read-only.
     */
    public void setInputVectors(LXWarp source, LXVector[] vectors, boolean changed) {
        if (source != inputSource || vectors != inputVectors || changed) {
            if (vectors != inputVectors) {
                inputVectors = vectors;
                System.out.println("Copying inputVectors to outputVectors (LXVector[" + this.inputVectors.length + "])...");
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
     * Applies the warp to the coordinates in inputVectors and writes the results
     * to outputVectors.  The inputVectorsChanged flag indicates whether inputVectors
     * has changed since the last call to run(); a typical implementation would
     * recompute outputVectors when inputVectorsChanged is true OR any of the warp's
     * parameters has changed.  This method should treat inputVectors as read-only;
     * it is also the sole writer to outputVectors, and it should return a flag
     * indicating whether any changes were written to outputVectors.
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
