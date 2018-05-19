/**
 * Copyright 2016- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.blend;

import heronarts.lx.LX;
import heronarts.lx.LXBuffer;
import heronarts.lx.LXModulatorComponent;
import heronarts.lx.PolyBuffer;
import heronarts.lx.PolyBufferProvider;

import static heronarts.lx.PolyBuffer.Space.RGB8;

/**
 * An LXBlend is a loop-based implementation of a compositing algorithm.
 * Two color buffers are blended together using some logic, typically
 * a standard alpha-compositing technique. However, more complex blend
 * modes may be authored, taking into account position information from
 * the model, for instance.
 */
public abstract class LXBlend extends LXModulatorComponent {

    protected static final int ALPHA_SHIFT = 24;
    protected static final int R_SHIFT = 16;
    protected static final int G_SHIFT = 8;
    protected static final int R_MASK = 0x00ff0000;
    protected static final int G_MASK = 0x0000ff00;
    protected static final int B_MASK = 0x000000ff;
    protected static final int RB_MASK = R_MASK | B_MASK;

    protected static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    protected static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    protected static long min(long a, long b) {
        return (a < b) ? a : b;
    }

    protected static long max(long a, long b) {
        return (a > b) ? a : b;
    }

    private String name;

    protected LXBlend(LX lx) {
        super(lx);
        String simple = this.getClass().getSimpleName();
        if (simple.endsWith("Blend")) {
            simple = simple.substring(0, simple.length() - "Blend".length());
        }
        this.name = simple;
    }

    /**
     * Sets name of this blend mode
     *
     * @param name UI name of blend
     * @return this
     */
    public LXBlend setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the name of this blend, to be shown in UI
     *
     * @return Blend name
     */
    public String getName() {
        return this.name;
    }

    /** Gets the name of this blend. */
    @Override
    public String getLabel() {
        return getName();
    }

    /** Name of the blend */
    @Override
    public String toString() {
        return getName();
    }

    @Deprecated
    public void blend(int[] dst, int[] src, double alpha, LXBuffer buffer) {
        blend(dst, src, alpha, buffer.getArray());
    }

    /**
     * Old-style subclasses override this method to implement the blend.
     * New-style subclasses should override the PolyBuffer-based blend()
     * method below, instead.
     *
     * @param base Base source (when amount = 0, the result equals the base)
     * @param overlay Overlay source (to be blended on top of the base)
     * @param alpha Amount of blending (from 0 to 1)
     * @param dest Destination array, which may be the same as overlay or base
     */
    @Deprecated
    public /* abstract */ void blend(int[] base, int[] overlay, double alpha, int[] dest) { }

    /**
     * Takes the base buffer, blends the overlay buffer onto it by a
     * specified amount, and writes the result into the destination buffer.
     * Subclasses should override this method.
     *
     * @param base Base source (when amount = 0, the result equals the base)
     * @param overlay Overlay source (to be blended on top of the base)
     * @param alpha Amount of blending (from 0 to 1)
     * @param dest Destination buffer, which may be the same as overlay or base
     * @param preferredSpace A hint as to which color space to use (the implementation
     *     is free to use any space, though doing so may sacrifice quality or efficiency)
     */
    public /* abstract */ void blend(PolyBuffer base, PolyBuffer overlay, double alpha, PolyBuffer dest, PolyBuffer.Space preferredSpace) {
        // For compatibility, this invokes the method that previous subclasses were
        // supposed to implement, and then marks the destination as modified.
        blend((int[]) base.getArray(RGB8), (int[]) overlay.getArray(RGB8), alpha, (int[]) dest.getArray(RGB8));
        dest.markModified(RGB8);

        // New subclasses should override and replace this method with one that
        // reads color arrays from base.getArray(space) and overlay.getArray(space),
        // writes the result into dest.getArray(space), and finally marks the
        // destination buffer modified with a call to dest.markModified(space).
    }

    public void blend(PolyBufferProvider base, PolyBufferProvider overlay, double alpha,
                                        PolyBufferProvider dest, PolyBuffer.Space preferredSpace) {
        blend(base.getPolyBuffer(), overlay.getPolyBuffer(), alpha, dest.getPolyBuffer(), preferredSpace);
    }
}
