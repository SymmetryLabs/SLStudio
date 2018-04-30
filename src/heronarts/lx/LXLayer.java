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

/**
 * A layer is a component that has a run method and operates on some other
 * buffer component. The layer does not actually own the color buffer. An
 * effect is an example of a layer, or patterns may compose themselves from
 * multiple layers.
 */
public abstract class LXLayer extends LXLayeredComponent {
    /** The requested color space.  See setPreferredSpace(). */
    protected PolyBuffer.Space preferredSpace = PolyBuffer.Space.RGB8;

    // An alias for the 8-bit color buffer array, for compatibility with old-style
    // implementations of run(deltaMs) that directly read from and write
    // into the "colors" array.  Newer subclasses should instead implement
    // run(deltaMs, preferredSpace) and use polyBuffer.getArray(space).
    protected int[] colors = null;

    protected LXLayer(LX lx) {
        super(lx);
    }

    protected LXLayer(LX lx, LXDeviceComponent component) {
        super(lx, component);
    }

    @Override
    public String getLabel() {
        return "Layer";
    }

    /**
     * Sets the color space in which this layer is requested to operate.
     * This is a request for the run() method to operate in a particular
     * color space, for efficiency.  The run() method is not required to honor
     * this request; this merely provides the information that using a
     * different color space will necessitate an additional conversion.
     * @param space
     */
    public void setPreferredSpace(PolyBuffer.Space space) {
        preferredSpace = space;
    }

    @Override
    protected final void onLoop(double deltaMs) {
        run(deltaMs, preferredSpace);
    }

    /**
     * Old-style subclasses override this method to run the layer
     * by reading and writing the "colors" array.  New-style subclasses
     * should override the other run() method instead; see below.
     *
     * @param deltaMs Milliseconds elapsed since last frame
     */
    public /* abstract */ void run(double deltaMs) { }

    /**
     * Runs the layer.  Subclasses should override this method.
     *
     * @param deltaMs Milliseconds elapsed since last frame
     * @param preferredSpace A hint as to which color space to operate in for
     *     the greatest efficiency (writing the pattern in a different color
     *     space will still work, but will necessitate color space conversion)
     */
    protected void run(double deltaMs, PolyBuffer.Space preferredSpace) {
        // For compatibility, this invokes the method that previous subclasses
        // were supposed to implement.  Implementations of run(deltaMs) are
        // assumed to operate only on the "colors" array, and are not expected
        // to have marked the buffer, so we mark the buffer modified here.
        colors = polyBuffer.getArray();
        run(deltaMs);
        polyBuffer.markModified();

        // New subclasses should override and replace this method with one that
        // obtains a color array using polyBuffer.getArray(space), writes into
        // that buffer, and then calls polyBuffer.markModified(space).
    }
}
