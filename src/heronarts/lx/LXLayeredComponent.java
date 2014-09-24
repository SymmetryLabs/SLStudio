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

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
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

    /**
     * Sets the color of point i
     *
     * @param i Point index
     * @param c color
     * @return this
     */
    protected final LXLayeredComponent setColor(int i, int c) {
        this.colors[i] = c;
        return this;
    }

    /**
     * Blend the color at index i with its existing value
     *
     * @param i Index
     * @param c New color
     * @param blendMode blending mode
     *
     * @return this
     */
    protected final LXLayeredComponent blendColor(int i, int c, LXColor.Blend blendMode) {
        this.colors[i] = LXColor.blend(this.colors[i], c, blendMode);
        return this;
    }

    protected final LXLayeredComponent blendColor(LXFixture f, int c, LXColor.Blend blendMode) {
        for (LXPoint p : f.getPoints()) {
            this.colors[p.index] = LXColor.blend(this.colors[p.index], c, blendMode);
        }
        return this;
    }

    /**
     * Adds to the color of point i, using blendColor with ADD
     *
     * @param i Point index
     * @param c color
     * @return this
     */
    protected final LXLayeredComponent addColor(int i, int c) {
        this.colors[i] = LXColor.add(this.colors[i], c);
        return this;
    }

    /**
     * Adds to the color of point (x,y) in a default GridModel, using blendColor
     *
     * @param x x-index
     * @param y y-index
     * @param c color
     * @return this
     */
    protected final LXLayeredComponent addColor(int x, int y, int c) {
        return addColor(x + y * this.lx.width, c);
    }

    /**
     * Adds the color to the fixture
     *
     * @param f Fixture
     * @param c New color
     * @return this
     */
    protected final LXLayeredComponent addColor(LXFixture f, int c) {
        for (LXPoint p : f.getPoints()) {
            this.colors[p.index] = LXColor.add(this.colors[p.index], c);
        }
        return this;
    }

    /**
     * Sets the color of point (x,y) in a default GridModel
     *
     * @param x x-index
     * @param y y-index
     * @param c color
     * @return this
     */
    protected final LXLayeredComponent setColor(int x, int y, int c) {
        this.colors[x + y * this.lx.width] = c;
        return this;
    }

    /**
     * Gets the color at point (x,y) in a GridModel
     *
     * @param x x-index
     * @param y y-index
     * @return Color value
     */
    protected final int getColor(int x, int y) {
        return this.colors[x + y * this.lx.width];
    }

    /**
     * Sets all points to one color
     *
     * @param c Color
     * @return this
     */
    protected final LXLayeredComponent setColors(int c) {
        for (int i = 0; i < colors.length; ++i) {
            this.colors[i] = c;
        }
        return this;
    }

    /**
     * Sets the color of all points in a fixture
     *
     * @param f Fixture
     * @param c color
     * @return this
     */
    protected final LXLayeredComponent setColor(LXFixture f, int c) {
        for (LXPoint p : f.getPoints()) {
            this.colors[p.index] = c;
        }
        return this;
    }

    /**
     * Clears all colors
     *
     * @return this
     */
    protected final LXLayeredComponent clearColors() {
        this.setColors(0);
        return this;
    }

}
