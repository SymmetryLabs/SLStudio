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
    protected LXVector[] vectors = null;
    protected LXVector[] warpedVectors = null;
    protected boolean vectorsDirty = false;
    protected boolean warpedVectorsUpdated = false;

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
     * Takes an array of input vectors (which can be null to indicate "no change
     * since the last call"), applies the warp to it, and returns the array of
     * warped vectors (which can be null to indicate "no change since last call").
     */
    public final LXVector[] applyWarp(double deltaMs, LXVector[] inputVectors) {
        if (isEnabled()) {
            if (inputVectors != null) {
                vectors = inputVectors;
                vectorsDirty = true;
            }
            if (vectors == null) {
                vectors = model.getVectors();
                vectorsDirty = true;
            }
            if (warpedVectors == null) {
                System.out.println("Copying warp vectors to warpedVectors (LXVector[" + vectors.length + "])...");
                warpedVectors = new LXVector[vectors.length];
                for (int i = 0; i < vectors.length; i++) {
                    warpedVectors[i] = new LXVector(vectors[i]);
                }
            }
        }
        warpedVectorsUpdated = run(deltaMs, vectorsDirty);
        vectorsDirty = false;
        return warpedVectorsUpdated ? warpedVectors : null;
    }

    public LXVector[] getWarpedVectors() {
        return warpedVectors;
    }

    /**
     * Applies the warp to the coordinates in this.vectors and writes the results
     * to this.warpedVectors.  The dirty flag indicates whether this.vectors has
     * changed since the last call to run(); a typical implementation would recompute
     * warpedVectors when the dirty flag is true OR any of the warp's parameters have
     * changed.  This method should treat vectors as read-only and should return a
     * flag indicating whether any changes were written to warpedVectors.
     */
    public abstract boolean run(double deltaMs, boolean dirty);

    public String getOscAddress() {
        LXBus bus = getBus();
        if (bus != null) {
            return bus.getOscAddress() + "/warp/" + (index + 1);
        }
        return null;
    }
}
