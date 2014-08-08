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

package heronarts.lx;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for system components that run in the engine, which have common
 * attributes, such as parameters, modulators, and layers. For instance,
 * patterns, transitions, and effects are all LXComponents.
 */
public abstract class LXLayeredComponent extends LXComponent implements LXLoopTask {

    protected final LX lx;

    private LXBuffer buffer = null;

    protected int[] colors = null;

    protected final List<LXLayer> layers = new ArrayList<LXLayer>();

    protected LXLayeredComponent(LX lx) {
        this(lx, (LXBuffer) null);
    }

    protected LXLayeredComponent(LX lx, LXBufferedComponent component) {
        this(lx, component.getBuffer());
    }

    protected LXLayeredComponent(LX lx, LXBuffer buffer) {
        this.lx = lx;
        if (buffer != null) {
            setBuffer(buffer);
        }
    }

    LXBuffer getBuffer() {
        return this.buffer;
    }

    LXLayeredComponent setBuffer(LXBufferedComponent component) {
        return setBuffer(component.getBuffer());
    }

    LXLayeredComponent setBuffer(LXBuffer buffer) {
        this.buffer = buffer;
        this.colors = buffer.getArray();
        return this;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // This protects against subclasses from inappropriately nuking the colors buffer
        this.colors = this.buffer.getArray();

        super.loop(deltaMs);
        onLoop(deltaMs);
        for (LXLayer layer : this.layers) {
            layer.setBuffer(this.buffer);
            layer.loop(deltaMs);
        }
        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    protected /* abstract */ void onLoop(double deltaMs) {}

    protected final LXLayer addLayer(LXLayer layer) {
        this.layers.add(layer);
        return layer;
    }

    protected final LXLayer removeLayer(LXLayer layer) {
        this.layers.remove(layer);
        return layer;
    }

    public final List<LXLayer> getLayers() {
        return this.layers;
    }

}
