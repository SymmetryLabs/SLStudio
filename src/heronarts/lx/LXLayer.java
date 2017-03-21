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
 * A layer is a components that has a run method and operates on some other
 * buffer component. The layer does not actually own the color buffer. An
 * effect is an example of a layer, or patterns may compose themselves from
 * multiple layers.
 */
public abstract class LXLayer extends LXLayeredComponent {

    protected LXLayer(LX lx) {
        super(lx);
    }

    protected LXLayer(LX lx, LXBufferedComponent buffer) {
        super(lx, buffer);
    }

    @Override
    public String getLabel() {
        return "Layer";
    }

    @Override
    protected final void onLoop(double deltaMs) {
        run(deltaMs);
    }

    /**
     * Run this layer.
     *
     * @param deltaMs Milliseconds elapsed since last frame
     */
    public abstract void run(double deltaMs);

}
