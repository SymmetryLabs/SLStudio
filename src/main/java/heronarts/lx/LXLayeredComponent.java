/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.lx;

import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXPalette;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.RGB8;
import static heronarts.lx.PolyBuffer.Space.SRGB8;

/**
 * Base class for system components that have a color buffer and run in the
 * engine, with common attributes such as parameters, modulators, and layers.
 * Patterns, transitions, and effects are all LXLayeredComponents.  Subclasses
 * do their work mainly by implementing onLoop() to write into the color buffer.
 *
 * The color buffer is actually a PolyBuffer, which manages a set of buffers,
 * one for each color space.  Subclasses should implement onLoop() so that it
 * writes to just one of the color spaces in the PolyBuffer; the data will be
 * automatically converted to other color spaces as needed.
 *
 * LXLayeredComponent subclasses can be marked as LXLayeredComponent.Buffered
 * (which means they own their own buffers), or not (which means they operate
 * on external buffers passed in via setBuffer()).
 *
 * For subclasses marked Buffered:
 *     Internal API:
 *         The implementation of onLoop() should fetch the array for a color
 *         space with getArray(space), write colors into the array, and
 *         finally mark it modified with markModified(space).
 *     External API:
 *         getArray(space) gets the array for the requested color space,
 *         converting from the space in which the colors were written, if
 *         different.  setPolyBuffer() is illegal to call.
 *
 * For subclasses not marked Buffered:
 *     Internal API:
 *         Same as above.
 *     External API:
 *         getArray() is the same as above.  setPolyBuffer(buffer) takes
 *         a PolyBuffer and makes it the buffer that onLoop() will read from
 *         and write into.
 */
public abstract class LXLayeredComponent extends LXModelComponent implements LXLoopTask, PolyBufferProvider {
    /** Marker interface for subclasses that want to own their own buffers. */
    public interface Buffered {}

    public final Timer timer = constructTimer();
    protected final LX lx;

    /** The PolyBuffer contains the color buffers for each of the color spaces. */
    protected PolyBuffer polyBuffer = null;

    /** The requested color space.  See setPreferredSpace(). */
    protected PolyBuffer.Space preferredSpace = SRGB8;

    private final List<LXLayer> mutableLayers = new ArrayList<LXLayer>();
    protected final List<LXLayer> layers = Collections.unmodifiableList(mutableLayers);

    protected final LXPalette palette;

    protected LXLayeredComponent(LX lx) {
        super(lx);
        this.lx = lx;
        palette = lx.palette;
        polyBuffer = new PolyBuffer(lx);
    }

    protected LXLayeredComponent(LX lx, LXDeviceComponent component) {
        this(lx);
        setBuffer(component);
    }

    /** Gets this component's combined color buffer. */
    public PolyBuffer getPolyBuffer() {
        return polyBuffer;
    }

    /** Sets the buffer of another component as the buffer to read from and write to. */
    protected LXLayeredComponent setBuffer(LXDeviceComponent component) {
        setPolyBuffer(component.polyBuffer);
        return this;
    }

    /** Sets an external buffer as the buffer to read from and write to. */
    protected LXLayeredComponent setPolyBuffer(PolyBuffer externalBuffer) {
        if (this instanceof Buffered) {
            throw new UnsupportedOperationException("Cannot set an external buffer in a Buffered LXLayeredComponent");
        }
        polyBuffer = externalBuffer;
        return this;
    }

    /** Convenience method for subclasses. */
    protected Object getArray(PolyBuffer.Space space) {
        return polyBuffer.getArray(space);
    }

    /** Convenience method for subclasses. */
    protected void markModified(PolyBuffer.Space space) {
        polyBuffer.markModified(space);
    }

    /**
     * Sets the color space in which this layer is requested to operate.
     * Implementations of onLoop(), run(), etc. remain free to use any space,
     * though ignoring this request may sacrifice quality or efficiency.
     * @param space An optimization hint as to the color space to use.
     */
    public void setPreferredSpace(PolyBuffer.Space space) {
        preferredSpace = space;
        for (LXLayer layer : mutableLayers) {
            layer.setPreferredSpace(space);
        }
    }

    /**
     * Constructor that optionally sets an external 8-bit color buffer as the
     * buffer to read from and write to.  Maintained for compatibility.
     */
    @Deprecated
    protected LXLayeredComponent(LX lx, /* nullable */ LXBuffer externalBuffer) {
        this(lx);
        if (externalBuffer != null) {
            setBuffer(externalBuffer);
        }
    }

    /** Gets the 8-bit color buffer.  Maintained for compatibility. */
    @Deprecated
    protected LXBuffer getBuffer() {
        return (LXBuffer) polyBuffer.getBuffer(SRGB8);
    }

    /** Gets the 8-bit color buffer's array.  Maintained for compatibility. */
    @Deprecated
    public int[] getColors() {
        return (int[]) getArray(SRGB8);
 }

    /**
     * Sets an external 8-bit color buffer as the buffer to read from and write to.
     * Maintained for compatibility.
     */
    @Deprecated
    protected LXLayeredComponent setBuffer(LXBuffer externalBuffer) {
        if (this instanceof Buffered) {
            throw new UnsupportedOperationException("Cannot set an external buffer in a Buffered LXLayeredComponent");
        }
        polyBuffer.setBuffer(externalBuffer);
        return this;
    }

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        super.loop(deltaMs);
        onLoop(deltaMs);

        for (LXLayer layer : this.mutableLayers) {
            layer.setPolyBuffer(polyBuffer);

            // TODO(mcslee): is this best here or should it be in addLayer?
            layer.setModel(this.model);

            layer.loop(deltaMs);
        }
        afterLayers(deltaMs);

        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    protected /* abstract */ void onLoop(double deltaMs) {
            // Implementations should call markModified() if they modify the color buffer.
    }

    protected /* abstract */ void afterLayers(double deltaMs) {
            // Implementations should call markModified() if they modify the color buffer.
    }

    protected final LXLayer addLayer(LXLayer layer) {
        if (this.mutableLayers.contains(layer)) {
            throw new IllegalStateException("Cannot add same layer twice: " + this + " " + layer);
        }
        layer.setParent(this);
        this.mutableLayers.add(layer);
        return layer;
    }

    protected final LXLayer removeLayer(LXLayer layer) {
        this.mutableLayers.remove(layer);
        layer.dispose();
        return layer;
    }

    public final List<LXLayer> getLayers() {
        return this.layers;
    }

    @Override
    public void dispose() {
        for (LXLayer layer : this.mutableLayers) {
            layer.dispose();
        }
        this.mutableLayers.clear();
        super.dispose();
    }

    /** Clears the colour buffer. */
    protected void clear() {
        int[] colors = (int[]) getArray(SRGB8);
        Arrays.fill(colors, 0);
        markModified(SRGB8);
    }

    protected void setColors(PolyBuffer.Space space, Object color) {
        if (space == RGB8 || space == SRGB8) {
            Arrays.fill((int[]) getArray(space), (int) color);
        }
        if (space == RGB16) {
            Arrays.fill((long[]) getArray(space), (long) color);
        }
        markModified(space);
    }

    /**
     * Sets the color of point i
     *
     * @param i Point index
     * @param c color
     * @return this
     */
    @Deprecated
    protected final LXLayeredComponent setColor(int i, int c) {
        getColors()[i] = c;
        markModified(SRGB8);
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
    @Deprecated
    protected final LXLayeredComponent blendColor(int i, int c, LXColor.Blend blendMode) {
        int[] colors = getColors();
        colors[i] = LXColor.blend(colors[i], c, blendMode);
        markModified(SRGB8);
        return this;
    }

    @Deprecated
    protected final LXLayeredComponent blendColor(LXFixture f, int c, LXColor.Blend blendMode) {
        int[] colors = getColors();
        for (LXPoint p : f.getPoints()) {
            colors[p.index] = LXColor.blend(colors[p.index], c, blendMode);
        }
        markModified(SRGB8);
        return this;
    }

    /**
     * Adds to the color of point i, using blendColor with ADD
     *
     * @param i Point index
     * @param c color
     * @return this
     */
    @Deprecated
    protected final LXLayeredComponent addColor(int i, int c) {
        int[] colors = getColors();
        colors[i] = LXColor.add(colors[i], c);
        markModified(SRGB8);
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
    @Deprecated
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
    @Deprecated
    protected final LXLayeredComponent addColor(LXFixture f, int c) {
        int[] colors = getColors();
        for (LXPoint p : f.getPoints()) {
            colors[p.index] = LXColor.add(colors[p.index], c);
        }
        markModified(SRGB8);
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
    @Deprecated
    protected final LXLayeredComponent setColor(int x, int y, int c) {
        getColors()[x + y * this.lx.width] = c;
        markModified(SRGB8);
        return this;
    }

    /**
     * Gets the color at point (x,y) in a GridModel
     *
     * @param x x-index
     * @param y y-index
     * @return Color value
     */
    @Deprecated
    protected final int getColor(int x, int y) {
        return getColors()[x + y * this.lx.width];
    }

    /**
     * Sets all points to one color
     *
     * @param c Color
     * @return this
     */
    @Deprecated
    protected final LXLayeredComponent setColors(int c) {
        int[] colors = getColors();
        for (int i = 0; i < colors.length; ++i) {
            colors[i] = c;
        }
        markModified(SRGB8);
        return this;
    }

    /**
     * Sets the color of all points in a fixture
     *
     * @param f Fixture
     * @param c color
     * @return this
     */
    @Deprecated
    protected final LXLayeredComponent setColor(LXFixture f, int c) {
        int[] colors = getColors();
        for (LXPoint p : f.getPoints()) {
            colors[p.index] = c;
        }
        markModified(SRGB8);
        return this;
    }

    /**
     * Clears all colors
     *
     * @return this
     */
    @Deprecated
    protected final LXLayeredComponent clearColors() {
        return setColors(0);
    }
}
