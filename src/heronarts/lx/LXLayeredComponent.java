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
import java.util.Collections;
import java.util.List;

/**
 * Base class for system components that run in the engine, which have common
 * attributes, such as parameters, modulators, and layers. For instance,
 * patterns, transitions, and effects are all LXComponents.
 */
public abstract class LXLayeredComponent extends LXModelComponent implements LXLoopTask {

    protected final LX lx;

    private LXBuffer buffer = null;

    protected int[] colors = null;

    private final List<LXLayer> internalLayers = new ArrayList<LXLayer>();
    protected final List<LXLayer> layers = Collections.unmodifiableList(internalLayers);

    protected final LXPalette palette;

    protected LXLayeredComponent(LX lx) {
        this(lx, (LXBuffer) null);
    }

    protected LXLayeredComponent(LX lx, LXBufferedComponent component) {
        this(lx, component.getBuffer());
    }

    protected LXLayeredComponent(LX lx, LXBuffer buffer) {
        super(lx);
        this.lx = lx;
        this.palette = lx.palette;
        if (buffer != null) {
            this.buffer = buffer;
            this.colors = buffer.getArray();
        }
    }

    protected LXBuffer getBuffer() {
        return this.buffer;
    }

    protected LXLayeredComponent setBuffer(LXBufferedComponent component) {
        return setBuffer(component.getBuffer());
    }

    protected LXLayeredComponent setBuffer(LXBuffer buffer) {
        this.buffer = buffer;
        this.colors = buffer.getArray();
        return this;
    }

    @Override
    public void loop(double deltaMs) {
        long loopStart = System.nanoTime();

        // This protects against subclasses from inappropriately nuking the colors buffer
        // reference. Even if a doofus assigns colors to something else, we'll reset it
        // here on each pass of the loop. Better than subclasses having to call getColors()
        // all the time.
        this.colors = this.buffer.getArray();

        super.loop(deltaMs);
        onLoop(deltaMs);
        for (LXLayer layer : this.internalLayers) {
            layer.setBuffer(this.buffer);

            // TODO(mcslee): is this best here or should it be in addLayer?
            layer.setModel(this.model);

            layer.loop(deltaMs);
        }
        afterLayers(deltaMs);

        this.timer.loopNanos = System.nanoTime() - loopStart;
    }

    protected /* abstract */ void onLoop(double deltaMs) {}

    protected /* abstract */ void afterLayers(double deltaMs) {}

    protected final LXLayer addLayer(LXLayer layer) {
        if (this.internalLayers.contains(layer)) {
            throw new IllegalStateException("Cannot add same layer twice: " + this + " " + layer);
        }
        layer.setParent(this);
        this.internalLayers.add(layer);
        return layer;
    }

    protected final LXLayer removeLayer(LXLayer layer) {
        this.internalLayers.remove(layer);
        layer.dispose();
        return layer;
    }

    public final List<LXLayer> getLayers() {
        return this.layers;
    }

    @Override
    public void dispose() {
        for (LXLayer layer : this.internalLayers) {
            layer.dispose();
        }
        this.internalLayers.clear();
        super.dispose();
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
        return setColors(0);
    }

}
